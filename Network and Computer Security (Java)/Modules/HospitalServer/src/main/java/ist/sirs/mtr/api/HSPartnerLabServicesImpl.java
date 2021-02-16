package ist.sirs.mtr.api;

import com.google.protobuf.ByteString;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import ist.sirs.mtr.crypto.Crypto;
import ist.sirs.mtr.crypto.CryptoManager;
import ist.sirs.mtr.db.DBPatientDetails;
import ist.sirs.mtr.db.DBTResult;
import ist.sirs.mtr.db.HSDatabase;
import ist.sirs.mtr.exception.HSRuntimeException;
import ist.sirs.mtr.exception.HSStatusRuntimeException;
import ist.sirs.mtr.proto.hspl.ErrorMessage;
import ist.sirs.mtr.proto.hspl.HSPartnerLabContract.*;
import ist.sirs.mtr.proto.hspl.HSPartnerLabServicesGrpc.HSPartnerLabServicesImplBase;
import ist.sirs.mtr.session.Session;
import ist.sirs.mtr.session.SessionsManager;
import ist.sirs.mtr.throttle.ThrottleManager;

import javax.crypto.interfaces.DHPublicKey;
import javax.crypto.spec.DHParameterSpec;
import javax.crypto.spec.IvParameterSpec;
import java.math.BigInteger;
import java.security.KeyPair;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static io.grpc.netty.shaded.io.netty.util.internal.StringUtil.EMPTY_STRING;

public class HSPartnerLabServicesImpl extends HSPartnerLabServicesImplBase {

  private static final int TEST_RESULT_TYPE = 0;
  private final HSDatabase db;
  private final PEPFrontend pepFrontend;

  public HSPartnerLabServicesImpl(HSDatabase db, PEPFrontend pepFrontend) {
    this.db = db;
    this.pepFrontend = pepFrontend;
  }

  /*==========================
  | Custom Protocol Services |
  ==========================*/

  @Override
  public void hello(HelloRequest req, StreamObserver<HelloReply> resObs) {
    try {
      doHello(req, resObs);
    } catch (HSRuntimeException e) {
      throw new HSStatusRuntimeException(e);
    } catch (Exception e) {
      throw new HSStatusRuntimeException(new HSRuntimeException(Status.INTERNAL, e.getMessage()));
    }
  }

  public void doHello(HelloRequest req, StreamObserver<HelloReply> resObs) {
    String tok = SessionsManager.newSession(null, "PartnerLab");
    // Adding PL Certificate
    if (!Crypto.addCrt(tok, req.getCrt().toByteArray()) || !Crypto.checkCrtValidity(tok, "CA"))
      throw new HSRuntimeException(
          Status.INVALID_ARGUMENT, ErrorMessage.INVALID_CRYPTO_ARGUMENTS.lbl);
    // Building PL Response
    resObs.onNext(
        HelloReply.newBuilder()
            .setCrt(ByteString.copyFrom(Crypto.getCrtBytes("HS")))
            .setAccToken(tok)
            .setSecure(generateRSASecureMessage(tok))
            .build());
    resObs.onCompleted();
  }

  @Override
  public void dH(DHRequest req, StreamObserver<DHReply> resObs) {
    try {
      doDH(req, resObs);
    } catch (HSRuntimeException e) {
      throw new HSStatusRuntimeException(e);
    } catch (Exception e) {
      throw new HSStatusRuntimeException(new HSRuntimeException(Status.INTERNAL, e.getMessage()));
    }
  }

  public void doDH(DHRequest req, StreamObserver<DHReply> resObs) {
    // Getting PL Authentication In Progress
    String tok = req.getAccTok();
    getSession(tok, false);
    hasRSACrt(tok);
    // Checking PL Message Freshness
    checkFreshness(req.getSecure());
    // Getting PL Arguments
    BigInteger P =
        Crypto.convertBytesToBigInt(
            Crypto.decipherBytesRSAPriv("HS", req.getEncryptedP().toByteArray()));
    BigInteger G =
        Crypto.convertBytesToBigInt(
            Crypto.decipherBytesRSAPriv("HS", req.getEncryptedG().toByteArray()));
    BigInteger yPL =
        Crypto.convertBytesToBigInt(
            Crypto.decipherBytesRSAPriv("HS", req.getEncryptedYPL().toByteArray()));
    // Checking PL Message Integrity
    String msg = String.format("%s%d%d%d", tok, P, G, yPL);
    checkRSAIntegrity(tok, req.getSecure(), msg);
    // Executing Diffie-Hellman
    DHParameterSpec dhParamSpec = new DHParameterSpec(P, G);
    KeyPair kPair = Crypto.doDHFirstPhase(dhParamSpec);
    if (kPair == null || !Crypto.doDHSecondPhase(tok, dhParamSpec, kPair, yPL))
      throw new HSRuntimeException(
          Status.INVALID_ARGUMENT, ErrorMessage.INVALID_CRYPTO_ARGUMENTS.lbl);
    BigInteger yHS = ((DHPublicKey) kPair.getPublic()).getY();
    // Building PL Response
    resObs.onNext(
        DHReply.newBuilder()
            .setEncryptedYHS(ByteString.copyFrom(Crypto.cipherBytesRSAPub(tok, yHS.toByteArray())))
            .setSecure(generateRSASecureMessage(String.valueOf(yHS)))
            .build());
    resObs.onCompleted();
  }

  @Override
  public void login(LoginRequest req, StreamObserver<LoginReply> resObs) {
    try {
      doLogin(req, resObs);
    } catch (HSRuntimeException e) {
      throw new HSStatusRuntimeException(e);
    } catch (Exception e) {
      throw new HSStatusRuntimeException(new HSRuntimeException(Status.INTERNAL, e.getMessage()));
    }
  }

  public void doLogin(LoginRequest req, StreamObserver<LoginReply> resObs) {
    // Getting PL Authentication In Progress
    String tok = req.getAccTok();
    Session session = getSession(tok, false);
    hasAESKey(tok);
    // Checking PL Message Freshness
    IvParameterSpec iv = new IvParameterSpec(req.getSecure().getIv().toByteArray());
    checkFreshness(req.getSecure());
    // Getting PL Arguments
    String labUname =
        Crypto.convertBytesToString(
            Crypto.decipherBytesAES(tok, iv, req.getEncryptedUname().toByteArray()));
    String labPwd =
        Crypto.convertBytesToString(
            Crypto.decipherBytesAES(tok, iv, req.getEncryptedPwd().toByteArray()));
    // Checking PL Message Integrity
    String msg = String.format("%s%s%s", tok, labUname, labPwd);
    checkAESIntegrity(tok, iv, req.getSecure(), msg);
    // Validating PL Credentials
    if (ThrottleManager.getThrottle(labUname))
      throw new HSRuntimeException(Status.RESOURCE_EXHAUSTED, ErrorMessage.TOO_MANY_ATTEMPTS.lbl);
    if (!db.isLab(labUname, labPwd)) {
      ThrottleManager.unsuccessful(labUname);
      throw new HSRuntimeException(Status.INVALID_ARGUMENT, ErrorMessage.INVALID_CREDENTIALS.lbl);
    }
    // PL Login Successful
    ThrottleManager.successful(labUname);
    session.activate(labUname);
    // Adding PL Certificate
    db.insertLabCrt(labUname, Crypto.getCrtBytes(tok));
    // Building PL Response
    resObs.onNext(
        LoginReply.newBuilder()
            .setSecure(
                generateAESSecureMessage(
                    tok, EMPTY_STRING, new IvParameterSpec(Crypto.generateRandomIV())))
            .build());
    resObs.onCompleted();
  }

  @Override
  public void submitTResults(
      SubmitTResultsRequest req, StreamObserver<SubmitTResultsReply> resObs) {
    try {
      doSubmitTResults(req, resObs);
    } catch (HSRuntimeException e) {
      throw new HSStatusRuntimeException(e);
    } catch (Exception e) {
      throw new HSStatusRuntimeException(new HSRuntimeException(Status.INTERNAL, e.getMessage()));
    }
  }

  public void doSubmitTResults(
      SubmitTResultsRequest req, StreamObserver<SubmitTResultsReply> resObs) {
    // Getting PL Authentication
    String tok = req.getAccTok();
    Session session = getSession(tok, true);
    // Checking PL Message Freshness
    IvParameterSpec iv = new IvParameterSpec(req.getSecure().getIv().toByteArray());
    checkFreshness(req.getSecure());
    // Getting PL Arguments
    List<DBTResult> tResultList =
        req.getEncryptedTResultsList().stream()
            .map(tResult -> decipherTResult(tok, iv, tResult))
            .collect(Collectors.toList());
    StringBuilder msgBuilder = new StringBuilder();
    tResultList.forEach(msgBuilder::append);
    // Checking PL Message Integrity
    String msg = String.format("%s%s", tok, msgBuilder.toString());
    checkAESIntegrity(tok, iv, req.getSecure(), msg);
    // Enforcing Policy Authoring Authorization
    pepFrontend.checkDecision(session.getRole(), "TestResultsRecords", "Write");
    // Executing PL Request
    for (DBTResult tResult : tResultList) {
      db.write(
          tResult.getPid(),
          TEST_RESULT_TYPE,
          tResult.getContent(),
          session.getUname(),
          tResult.getSignature(),
          tResult.getTs());
    }
    // Building PL Response
    resObs.onNext(
        SubmitTResultsReply.newBuilder()
            .setSecure(
                generateAESSecureMessage(
                    tok, EMPTY_STRING, new IvParameterSpec(Crypto.generateRandomIV())))
            .build());
    resObs.onCompleted();
  }

  @Override
  public void patientDetails(
      PatientDetailsRequest req, StreamObserver<PatientDetailsReply> resObs) {
    try {
      doPatientDetails(req, resObs);
    } catch (HSRuntimeException e) {
      throw new HSStatusRuntimeException(e);
    } catch (Exception e) {
      throw new HSStatusRuntimeException(new HSRuntimeException(Status.INTERNAL, e.getMessage()));
    }
  }

  public void doPatientDetails(
      PatientDetailsRequest req, StreamObserver<PatientDetailsReply> resObs) {
    // Getting PL Authentication
    String tok = req.getAccTok();
    Session session = getSession(tok, true);
    // Checking PL Message Freshness
    IvParameterSpec iv = new IvParameterSpec(req.getSecure().getIv().toByteArray());
    checkFreshness(req.getSecure());
    // Getting PL Arguments
    DBPatientDetails patient = decipherPatient(tok, iv, req.getEncryptedPat());
    // Checking PL Message Integrity
    String msg = String.format("%s%s", tok, patient);
    checkAESIntegrity(tok, iv, req.getSecure(), msg);
    // Enforcing Policy Authoring Authorization
    pepFrontend.checkDecision(session.getRole(), "PatientDetails", "Read");
    // Executing PL Request
    iv = new IvParameterSpec(Crypto.generateRandomIV());
    List<DBPatientDetails> pDetailsList =
        db.readPatientDetails(patient.getPid(), patient.getNif(), patient.getName());
    // Building PL Response
    PatientDetailsReply.Builder resPDetails = PatientDetailsReply.newBuilder();
    StringBuilder sb = new StringBuilder();
    for (DBPatientDetails pDetails : pDetailsList) {
      resPDetails.addPats(cipherPatient(tok, iv, pDetails));
      sb.append(pDetails);
    }
    resObs.onNext(resPDetails.setSecure(generateAESSecureMessage(tok, sb.toString(), iv)).build());
    resObs.onCompleted();
  }

  @Override
  public void logout(LogoutRequest req, StreamObserver<LogoutReply> resObs) {
    try {
      doLogout(req, resObs);
    } catch (HSRuntimeException e) {
      throw new HSStatusRuntimeException(e);
    } catch (Exception e) {
      throw new HSStatusRuntimeException(new HSRuntimeException(Status.INTERNAL, e.getMessage()));
    }
  }

  public void doLogout(LogoutRequest req, StreamObserver<LogoutReply> resObs) {
    // Getting PL Authentication
    String tok = req.getAccTok();
    getSession(tok, true);
    // Checking PL Message Freshness
    IvParameterSpec iv = new IvParameterSpec(req.getSecure().getIv().toByteArray());
    checkFreshness(req.getSecure());
    // Checking PL Message Integrity
    checkAESIntegrity(tok, iv, req.getSecure(), tok);
    // Building PL Response
    resObs.onNext(
        LogoutReply.newBuilder()
            .setSecure(
                generateAESSecureMessage(
                    tok, EMPTY_STRING, new IvParameterSpec(Crypto.generateRandomIV())))
            .build());
    // Removing PL Session
    SessionsManager.delSession(tok);
    resObs.onCompleted();
  }

  /*==========================
  | Service Impl Auxiliaries |
  ==========================*/

  private Session getSession(String tok, boolean isActive) {
    Session session = SessionsManager.getSession(tok, isActive);
    if (session == null)
      throw new HSRuntimeException(Status.UNAUTHENTICATED, ErrorMessage.INVALID_TOKEN.lbl);
    return session;
  }

  private void checkFreshness(SecureMessage secure) {
    if (!CryptoManager.isFresh(
        Map.entry(Crypto.convertBytesToBigInt(secure.getNonce().toByteArray()), secure.getTs())))
      throw new HSRuntimeException(Status.INVALID_ARGUMENT, ErrorMessage.INVALID_FRESHNESS.lbl);
  }

  private void checkRSAIntegrity(String tok, SecureMessage secure, String msg) {
    BigInteger nonce = Crypto.convertBytesToBigInt(secure.getNonce().toByteArray());
    String plainMsg = msg + secure.getTs() + nonce;
    if (!Arrays.equals(
        Crypto.decipherBytesRSAPub(tok, secure.getIntegrity().toByteArray()),
        Crypto.hash(plainMsg)))
      throw new HSRuntimeException(Status.INVALID_ARGUMENT, ErrorMessage.INVALID_INTEGRITY.lbl);
  }

  private void checkAESIntegrity(String tok, IvParameterSpec iv, SecureMessage secure, String msg) {
    BigInteger nonce = Crypto.convertBytesToBigInt(secure.getNonce().toByteArray());
    String plainMsg =
        String.format("%s%d%d%s", msg, secure.getTs(), nonce, Arrays.toString(iv.getIV()));
    if (!Arrays.equals(
        Crypto.decipherBytesAES(tok, iv, secure.getIntegrity().toByteArray()),
        Crypto.hmac(tok, plainMsg.getBytes())))
      throw new HSRuntimeException(Status.INVALID_ARGUMENT, ErrorMessage.INVALID_INTEGRITY.lbl);
  }

  private void hasRSACrt(String tok) {
    if (!Crypto.hasRSACrt(tok))
      throw new HSRuntimeException(Status.UNAUTHENTICATED, ErrorMessage.MISSING_HELLO.lbl);
  }

  private void hasAESKey(String tok) {
    if (!Crypto.hasAESKey(tok))
      throw new HSRuntimeException(Status.UNAUTHENTICATED, ErrorMessage.MISSING_DH.lbl);
  }

  /*====================
  | Crypto Auxiliaries |
  ====================*/

  public static SecureMessage generateRSASecureMessage(String msg) {
    long ts = System.currentTimeMillis();
    BigInteger nonce = Crypto.generateRandomBigInt();
    String plainMsg = String.format("%s%d%d", msg, ts, nonce);
    return SecureMessage.newBuilder()
        .setTs(ts)
        .setNonce(ByteString.copyFrom(nonce.toByteArray()))
        .setIntegrity(ByteString.copyFrom(Crypto.cipherBytesRSAPriv("HS", Crypto.hash(plainMsg))))
        .build();
  }

  public static SecureMessage generateAESSecureMessage(String tok, String msg, IvParameterSpec iv) {
    long ts = System.currentTimeMillis();
    BigInteger nonce = Crypto.generateRandomBigInt();
    String plainMsg = String.format("%s%d%d%s", msg, ts, nonce, Arrays.toString(iv.getIV()));
    return SecureMessage.newBuilder()
        .setTs(ts)
        .setNonce(ByteString.copyFrom(nonce.toByteArray()))
        .setIntegrity(
            ByteString.copyFrom(
                Crypto.cipherBytesAES(tok, iv, Crypto.hmac(tok, plainMsg.getBytes()))))
        .setIv(ByteString.copyFrom(iv.getIV()))
        .build();
  }

  private DBTResult decipherTResult(String tok, IvParameterSpec iv, TResult tResult) {
    int pid =
        Crypto.convertBytesToInt(
            Crypto.decipherBytesAES(tok, iv, tResult.getEncryptedPid().toByteArray()));
    long ts =
        Crypto.convertBytesToLong(
            Crypto.decipherBytesAES(tok, iv, tResult.getEncryptedTs().toByteArray()));
    String content =
        Crypto.convertBytesToString(
            Crypto.decipherBytesAES(tok, iv, tResult.getEncryptedCont().toByteArray()));
    byte[] signature =
        Crypto.decipherBytesAES(tok, iv, tResult.getEncryptedSignature().toByteArray());
    return new DBTResult(pid, ts, content, signature);
  }

  private DBPatientDetails decipherPatient(String tok, IvParameterSpec iv, Patient patient) {
    int pid =
        Crypto.convertBytesToInt(
            Crypto.decipherBytesAES(tok, iv, patient.getEncryptedPid().toByteArray()));
    int nif =
        Crypto.convertBytesToInt(
            Crypto.decipherBytesAES(tok, iv, patient.getEncryptedNif().toByteArray()));
    String name =
        Crypto.convertBytesToString(
            Crypto.decipherBytesAES(tok, iv, patient.getEncryptedName().toByteArray()));
    return new DBPatientDetails(pid, nif, name);
  }

  private Patient cipherPatient(String tok, IvParameterSpec iv, DBPatientDetails patient) {
    return Patient.newBuilder()
        .setEncryptedPid(
            ByteString.copyFrom(
                Crypto.cipherBytesAES(tok, iv, Crypto.convertIntToBytes(patient.getPid()))))
        .setEncryptedNif(
            ByteString.copyFrom(
                Crypto.cipherBytesAES(tok, iv, Crypto.convertIntToBytes(patient.getNif()))))
        .setEncryptedName(
            ByteString.copyFrom(Crypto.cipherBytesAES(tok, iv, patient.getName().getBytes())))
        .build();
  }
}
