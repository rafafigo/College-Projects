package ist.sirs.mtr.api;

import com.google.protobuf.ByteString;
import io.grpc.*;
import io.grpc.protobuf.ProtoUtils;
import ist.sirs.mtr.crypto.Crypto;
import ist.sirs.mtr.crypto.CryptoManager;
import ist.sirs.mtr.exception.PLRuntimeException;
import ist.sirs.mtr.proto.hspl.ErrorMessage;
import ist.sirs.mtr.proto.hspl.HSPartnerLabContract.*;
import ist.sirs.mtr.proto.hspl.HSPartnerLabServicesGrpc;
import ist.sirs.mtr.tres.TRes;

import javax.crypto.interfaces.DHPublicKey;
import javax.crypto.spec.DHParameterSpec;
import javax.crypto.spec.IvParameterSpec;
import java.math.BigInteger;
import java.security.KeyPair;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static io.grpc.netty.shaded.io.netty.util.internal.StringUtil.EMPTY_STRING;

public class PartnerLabFrontend {

  private static final Metadata.Key<SecureMessage> secureMessageKey =
      ProtoUtils.keyForProto(SecureMessage.getDefaultInstance());
  private final ManagedChannel channel;
  private final HSPartnerLabServicesGrpc.HSPartnerLabServicesBlockingStub stub;
  private String token;

  public PartnerLabFrontend(String host, int port) {
    this.channel = ManagedChannelBuilder.forAddress(host, port).usePlaintext().build();
    this.stub = HSPartnerLabServicesGrpc.newBlockingStub(this.channel);
  }

  /*==========================
  | Custom Protocol Frontend |
  ==========================*/

  public String login(String uname, String pwd) {
    try {
      return doLogin(uname, pwd);
    } catch (StatusRuntimeException e) {
      return getErrorMessage(e);
    } catch (PLRuntimeException e) {
      return e.getMessage();
    }
  }

  public String doLogin(String uname, String pwd) {
    HelloReply resHello =
        stub.hello(
            HelloRequest.newBuilder()
                .setCrt(ByteString.copyFrom(Crypto.getCrtBytes("PL")))
                .build());
    // Checking HS Message Freshness
    checkFreshness(resHello.getSecure());
    // Adding HS Certificate
    Crypto.addCrt("HS", resHello.getCrt().toByteArray());
    if (!checkCrtValidity()) throw new PLRuntimeException(Message.INVALID_CERTIFICATE.lbl);
    token = resHello.getAccToken();
    // Checking HS Message Integrity
    checkRSAIntegrity(resHello.getSecure(), resHello.getAccToken());
    // Executing Diffie-Hellman First Phase
    KeyPair kPair = Crypto.doDHFirstPhase(null);
    if (kPair == null) throw new PLRuntimeException(ErrorMessage.INVALID_CRYPTO_ARGUMENTS.lbl);
    // Sending Diffie-Hellman Request
    DHPublicKey dhPubKey = (DHPublicKey) kPair.getPublic();
    DHParameterSpec dhParamSpec = dhPubKey.getParams();
    BigInteger P = dhParamSpec.getP();
    BigInteger G = dhParamSpec.getG();
    BigInteger yPL = dhPubKey.getY();
    String msg = String.format("%s%d%d%d", token, P, G, yPL);
    DHReply resDH =
        stub.dH(
            DHRequest.newBuilder()
                .setAccTok(token)
                .setEncryptedP(ByteString.copyFrom(Crypto.cipherBytesRSAPub("HS", P.toByteArray())))
                .setEncryptedG(ByteString.copyFrom(Crypto.cipherBytesRSAPub("HS", G.toByteArray())))
                .setEncryptedYPL(
                    ByteString.copyFrom(Crypto.cipherBytesRSAPub("HS", yPL.toByteArray())))
                .setSecure(generateRSASecureMessage(msg))
                .build());
    // Checking HS Message Freshness
    checkFreshness(resDH.getSecure());
    BigInteger yHS =
        Crypto.convertBytesToBigInt(
            Crypto.decipherBytesRSAPriv("PL", resDH.getEncryptedYHS().toByteArray()));
    // Checking HS Message Integrity
    checkRSAIntegrity(resDH.getSecure(), String.valueOf(yHS));
    if (!Crypto.doDHSecondPhase("PL", dhParamSpec, kPair, yHS))
      throw new PLRuntimeException(ErrorMessage.INVALID_CRYPTO_ARGUMENTS.lbl);
    // Executing Login
    IvParameterSpec iv = new IvParameterSpec(Crypto.generateRandomIV());
    msg = String.format("%s%s%s", token, uname, pwd);
    LoginReply resLogin =
        stub.login(
            LoginRequest.newBuilder()
                .setAccTok(token)
                .setEncryptedUname(
                    ByteString.copyFrom(Crypto.cipherBytesAES("PL", iv, uname.getBytes())))
                .setEncryptedPwd(
                    ByteString.copyFrom(Crypto.cipherBytesAES("PL", iv, pwd.getBytes())))
                .setSecure(generateAESSecureMessage(msg, iv))
                .build());
    // Checking HS Message Freshness
    checkFreshness(resLogin.getSecure());
    // Checking HS Message Integrity
    checkAESIntegrity(
        new IvParameterSpec(resLogin.getSecure().getIv().toByteArray()),
        resLogin.getSecure(),
        EMPTY_STRING);
    return Message.LOGIN_SUCCESS.lbl;
  }

  public String submitTResults(List<TRes> tResList) {
    try {
      return doSubmitTResults(tResList);
    } catch (StatusRuntimeException e) {
      return getErrorMessage(e);
    } catch (PLRuntimeException e) {
      return e.getMessage();
    }
  }

  public String doSubmitTResults(List<TRes> tResList) {
    if (token == null) throw new PLRuntimeException(Message.TOKEN_NOT_AVAILABLE.lbl);
    IvParameterSpec iv = new IvParameterSpec(Crypto.generateRandomIV());
    // Building Request
    SubmitTResultsRequest.Builder req = SubmitTResultsRequest.newBuilder().setAccTok(token);
    StringBuilder msgBuilder = new StringBuilder();
    tResList.forEach(tRes -> req.addEncryptedTResults(cipherTResult(iv, tRes, msgBuilder)));
    String msg = String.format("%s%s", token, msgBuilder.toString());
    req.setSecure(generateAESSecureMessage(msg, iv));
    // Submitting Test Results
    SubmitTResultsReply res = stub.submitTResults(req.build());
    // Checking HS Message Freshness
    checkFreshness(res.getSecure());
    // Checking HS Message Integrity
    checkAESIntegrity(
        new IvParameterSpec(res.getSecure().getIv().toByteArray()), res.getSecure(), EMPTY_STRING);
    return Message.TEST_RES_SUBMITTED.lbl;
  }

  public String patientDetails(int pid, int nif, String name) {
    try {
      return doPatientDetails(pid, nif, name);
    } catch (StatusRuntimeException e) {
      return getErrorMessage(e);
    } catch (PLRuntimeException e) {
      return e.getMessage();
    }
  }

  public String doPatientDetails(int pid, int nif, String name) {
    if (token == null) throw new PLRuntimeException(Message.TOKEN_NOT_AVAILABLE.lbl);
    IvParameterSpec iv = new IvParameterSpec(Crypto.generateRandomIV());
    // Requesting Patient Details
    String msg = String.format("%s%d%d%s", token, pid, nif, name);
    PatientDetailsReply res =
        stub.patientDetails(
            PatientDetailsRequest.newBuilder()
                .setAccTok(token)
                .setEncryptedPat(cipherPatient(iv, pid, nif, name))
                .setSecure(generateAESSecureMessage(msg, iv))
                .build());
    // Checking HS Message Freshness
    iv = new IvParameterSpec(res.getSecure().getIv().toByteArray());
    checkFreshness(res.getSecure());
    // Parsing HS Response
    StringBuilder retBuilder = new StringBuilder();
    StringBuilder msgBuilder = new StringBuilder();
    for (Patient patient : res.getPatsList()) decipherPatient(iv, patient, retBuilder, msgBuilder);
    // Checking HS Message Integrity
    checkAESIntegrity(iv, res.getSecure(), msgBuilder.toString());
    return retBuilder.toString();
  }

  public String logout() {
    try {
      return doLogout();
    } catch (StatusRuntimeException e) {
      return getErrorMessage(e);
    } catch (PLRuntimeException e) {
      return e.getMessage();
    }
  }

  public String doLogout() {
    if (token == null) throw new PLRuntimeException(Message.TOKEN_NOT_AVAILABLE.lbl);
    IvParameterSpec iv = new IvParameterSpec(Crypto.generateRandomIV());
    // Executing Logout
    LogoutReply res =
        stub.logout(
            LogoutRequest.newBuilder()
                .setAccTok(token)
                .setSecure(generateAESSecureMessage(token, iv))
                .build());
    // Checking HS Message Freshness
    checkFreshness(res.getSecure());
    // Checking HS Message Integrity
    checkAESIntegrity(
        new IvParameterSpec(res.getSecure().getIv().toByteArray()), res.getSecure(), EMPTY_STRING);
    cleanSession();
    return Message.LOGOUT_SUCCESS.lbl;
  }

  /*=================
  | Outside Methods |
  =================*/

  public void cleanSession() {
    token = null;
  }

  public void shutdown() {
    channel.shutdown();
  }

  /*=================================
  | PartnerLab Frontend Auxiliaries |
  =================================*/

  private void checkFreshness(SecureMessage secure) {
    if (!CryptoManager.isFresh(
        Map.entry(Crypto.convertBytesToBigInt(secure.getNonce().toByteArray()), secure.getTs())))
      throw new PLRuntimeException(ErrorMessage.INVALID_FRESHNESS.lbl);
  }

  private void checkRSAIntegrity(SecureMessage secure, String msg) {
    BigInteger nonce = Crypto.convertBytesToBigInt(secure.getNonce().toByteArray());
    String plainMsg = String.format("%s%d%d", msg, secure.getTs(), nonce);
    if (!Arrays.equals(
        Crypto.decipherBytesRSAPub("HS", secure.getIntegrity().toByteArray()),
        Crypto.hash(plainMsg))) throw new PLRuntimeException(ErrorMessage.INVALID_INTEGRITY.lbl);
  }

  private void checkAESIntegrity(IvParameterSpec iv, SecureMessage secure, String msg) {
    BigInteger nonce = Crypto.convertBytesToBigInt(secure.getNonce().toByteArray());
    String plainMsg =
        String.format("%s%d%d%s", msg, secure.getTs(), nonce, Arrays.toString(iv.getIV()));
    if (!Arrays.equals(
        Crypto.decipherBytesAES("PL", iv, secure.getIntegrity().toByteArray()),
        Crypto.hmac("PL", plainMsg.getBytes())))
      throw new PLRuntimeException(ErrorMessage.INVALID_INTEGRITY.lbl);
  }

  private boolean checkCrtValidity() {
    return Crypto.checkCrtValidity("HS", "CA")
        && Crypto.getCrt("HS").getSubjectDN().getName().matches("(.*)O=MTR-HS(.*)");
  }

  /*====================
  | Crypto Auxiliaries |
  ====================*/

  private SecureMessage generateRSASecureMessage(String msg) {
    long ts = System.currentTimeMillis();
    BigInteger nonce = Crypto.generateRandomBigInt();
    String plainMsg = String.format("%s%d%d", msg, ts, nonce);
    return SecureMessage.newBuilder()
        .setTs(ts)
        .setNonce(ByteString.copyFrom(nonce.toByteArray()))
        .setIntegrity(ByteString.copyFrom(Crypto.cipherBytesRSAPriv("PL", Crypto.hash(plainMsg))))
        .build();
  }

  private SecureMessage generateAESSecureMessage(String msg, IvParameterSpec iv) {
    long ts = System.currentTimeMillis();
    BigInteger nonce = Crypto.generateRandomBigInt();
    String plainMsg = String.format("%s%d%d%s", msg, ts, nonce, Arrays.toString(iv.getIV()));
    return SecureMessage.newBuilder()
        .setTs(ts)
        .setNonce(ByteString.copyFrom(nonce.toByteArray()))
        .setIntegrity(
            ByteString.copyFrom(
                Crypto.cipherBytesAES("PL", iv, Crypto.hmac("PL", plainMsg.getBytes()))))
        .setIv(ByteString.copyFrom(iv.getIV()))
        .build();
  }

  private TResult cipherTResult(IvParameterSpec iv, TRes tRes, StringBuilder msgBuilder) {
    int pid = tRes.getPid();
    long ts = tRes.getTs();
    String content = tRes.getContent();
    byte[] signature = tRes.getSignature();
    msgBuilder.append(pid).append(ts).append(content).append(Arrays.toString(signature));

    return TResult.newBuilder()
        .setEncryptedPid(
            ByteString.copyFrom(Crypto.cipherBytesAES("PL", iv, Crypto.convertIntToBytes(pid))))
        .setEncryptedTs(
            ByteString.copyFrom(Crypto.cipherBytesAES("PL", iv, Crypto.convertLongToBytes(ts))))
        .setEncryptedCont(ByteString.copyFrom(Crypto.cipherBytesAES("PL", iv, content.getBytes())))
        .setEncryptedSignature(ByteString.copyFrom(Crypto.cipherBytesAES("PL", iv, signature)))
        .build();
  }

  private void decipherPatient(
      IvParameterSpec iv, Patient patient, StringBuilder retBuilder, StringBuilder msgBuilder) {
    int pid =
        Crypto.convertBytesToInt(
            Crypto.decipherBytesAES("PL", iv, patient.getEncryptedPid().toByteArray()));
    int nif =
        Crypto.convertBytesToInt(
            Crypto.decipherBytesAES("PL", iv, patient.getEncryptedNif().toByteArray()));
    String name =
        Crypto.convertBytesToString(
            Crypto.decipherBytesAES("PL", iv, patient.getEncryptedName().toByteArray()));
    retBuilder.append(
        String.format("Patient Id: %d, Patient Nif: %d, Patient Name: %s%n", pid, nif, name));
    msgBuilder.append(pid).append(nif).append(name);
  }

  private Patient cipherPatient(IvParameterSpec iv, int pid, int nif, String name) {
    return Patient.newBuilder()
        .setEncryptedPid(
            ByteString.copyFrom(Crypto.cipherBytesAES("PL", iv, Crypto.convertIntToBytes(pid))))
        .setEncryptedNif(
            ByteString.copyFrom(Crypto.cipherBytesAES("PL", iv, Crypto.convertIntToBytes(nif))))
        .setEncryptedName(ByteString.copyFrom(Crypto.cipherBytesAES("PL", iv, name.getBytes())))
        .build();
  }

  /*===================
  | Exception Handler |
  ===================*/

  private String getErrorMessage(StatusRuntimeException exception) {
    Metadata metadata = exception.getTrailers();
    if (metadata == null || !metadata.containsKey(secureMessageKey))
      return ErrorMessage.INVALID_INTEGRITY.lbl;
    SecureMessage secMessage = metadata.get(secureMessageKey);
    if (secMessage == null) return ErrorMessage.INVALID_INTEGRITY.lbl;
    try {
      checkFreshness(secMessage);
      Status status = exception.getStatus();
      String msg = String.format("%d%s", status.getCode().value(), status.getDescription());
      checkRSAIntegrity(secMessage, msg);
    } catch (PLRuntimeException ePL) {
      return ePL.getMessage();
    }
    if (!(exception.getMessage().equals(ErrorMessage.NO_FIELD_SPECIFIED.lbl)
        || exception.getMessage().equals(ErrorMessage.PID_NOT_FOUND.lbl)
        || exception.getMessage().equals(ErrorMessage.PERMISSION_DENIED.lbl))) cleanSession();
    return exception.getMessage();
  }
}
