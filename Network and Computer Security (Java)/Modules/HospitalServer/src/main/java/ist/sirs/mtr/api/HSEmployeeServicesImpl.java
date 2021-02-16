package ist.sirs.mtr.api;

import com.google.protobuf.ByteString;
import com.google.protobuf.Timestamp;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import ist.sirs.mtr.crypto.Crypto;
import ist.sirs.mtr.db.DBPatientDetails;
import ist.sirs.mtr.db.DBPatientRecord;
import ist.sirs.mtr.db.DBTResAuth;
import ist.sirs.mtr.db.HSDatabase;
import ist.sirs.mtr.error.AssertError;
import ist.sirs.mtr.exception.HSRuntimeException;
import ist.sirs.mtr.proto.hse.ErrorMessage;
import ist.sirs.mtr.proto.hse.HSEmployee.*;
import ist.sirs.mtr.proto.hse.HSEmployeeServicesGrpc.HSEmployeeServicesImplBase;
import ist.sirs.mtr.session.Session;
import ist.sirs.mtr.session.SessionsManager;
import ist.sirs.mtr.throttle.ThrottleManager;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

public class HSEmployeeServicesImpl extends HSEmployeeServicesImplBase {

  private final HSDatabase db;
  private final PEPFrontend pepFrontend;

  public HSEmployeeServicesImpl(HSDatabase db, PEPFrontend pepFrontend) {
    this.db = db;
    this.pepFrontend = pepFrontend;
  }

  /*==========
  | Services |
  ==========*/

  @Override
  public void login(LoginRequest req, StreamObserver<LoginReply> resObs) {
    // Validating Employee Credentials
    if (ThrottleManager.getThrottle(req.getUname()))
      throw new HSRuntimeException(Status.RESOURCE_EXHAUSTED, ErrorMessage.INVALID_CREDENTIALS.lbl);
    String role = db.isEmployeeGetRole(req.getUname(), req.getPwd());
    if (role == null) {
      ThrottleManager.unsuccessful(req.getUname());
      throw new HSRuntimeException(Status.INVALID_ARGUMENT, ErrorMessage.INVALID_CREDENTIALS.lbl);
    }
    // Employee Login Successful
    ThrottleManager.successful(req.getUname());
    String tok = SessionsManager.newSession(req.getUname(), role);
    // Building Employee Response
    resObs.onNext(LoginReply.newBuilder().setAccTok(tok).setRole(strToRole(role)).build());
    resObs.onCompleted();
  }

  @Override
  public void read(ReadRequest req, StreamObserver<ReadReply> resObs) {
    // Getting Employee Authentication
    Session session = getSession(req.getAccTok());
    // Enforcing Policy Authoring Authorization
    pepFrontend.checkDecision(session.getRole(), req.getRecType().toString(), "Read");
    // Executing Employee Request
    List<DBPatientRecord> recCont =
        db.read(req.getPid(), req.getRecTypeValue(), toLocalDateTime(req.getRecTs()));
    // Building Employee Response
    ReadReply.Builder res = ReadReply.newBuilder();
    for (DBPatientRecord rec : recCont) {
      res.addRecs(
          PatientRecord.newBuilder()
              .setRecId(rec.getRecId())
              .setRecCont(rec.getRecCont())
              .setRecTs(toTimestamp(rec.getRecTs())));
    }
    resObs.onNext(res.build());
    resObs.onCompleted();
  }

  @Override
  public void write(WriteRequest req, StreamObserver<WriteReply> resObs) {
    // Getting Employee Authentication
    Session session = getSession(req.getAccTok());
    // Enforcing Policy Authoring Authorization
    pepFrontend.checkDecision(session.getRole(), req.getRecType().toString(), "Write");
    // Executing Employee Request
    byte[] labSig = null;
    long ts = System.currentTimeMillis();
    if (req.getRecType() == PatientRecordType.TestResultsRecords) {
      // Internal Partner Lab
      byte[] hash = Crypto.hash(String.format("%d%s%d", req.getPid(), req.getRecCont(), ts));
      labSig = Crypto.cipherBytesRSAPriv("HS", hash);
    }
    db.write(req.getPid(), req.getRecTypeValue(), req.getRecCont(), "HS", labSig, ts);
    // Building Employee Response
    resObs.onNext(WriteReply.getDefaultInstance());
    resObs.onCompleted();
  }

  @Override
  public void tResAuth(TResAuthRequest req, StreamObserver<TResAuthReply> resObs) {
    Session session = getSession(req.getAccTok());
    // Enforcing Policy Authoring Authorization
    pepFrontend.checkDecision(
        session.getRole(), "TestResultsRecords", "TestResultAuthenticityCheck");
    // Executing Employee Request
    DBTResAuth resAuth = db.tResAuth(req.getPid(), req.getRecId());
    // Building Employee Response
    resObs.onNext(
        TResAuthReply.newBuilder()
            .setLabSig(ByteString.copyFrom(resAuth.getLabSig()))
            .setLabCrt(ByteString.copyFrom(resAuth.getLabCrt()))
            .build());
    resObs.onCompleted();
  }

  @Override
  public void logout(LogoutRequest req, StreamObserver<LogoutReply> resObs) {
    // Getting Employee Authentication
    getSession(req.getAccTok());
    // Removing Employee Session
    SessionsManager.delSession(req.getAccTok());
    // Building Employee Response
    resObs.onNext(LogoutReply.getDefaultInstance());
    resObs.onCompleted();
  }

  @Override
  public void createUser(CreateUserRequest req, StreamObserver<CreateUserReply> resObs) {
    // Getting Employee Authentication
    Session session = getSession(req.getAccTok());
    // Enforcing Policy Authoring Authorization
    pepFrontend.checkDecision(session.getRole(), "EmployeeCredentials", "Write");
    // Executing Employee Request
    db.writeUserCredentials(req.getUname(), req.getPwd(), req.getRole().toString());
    // Building Employee Response
    resObs.onNext(CreateUserReply.getDefaultInstance());
    resObs.onCompleted();
  }

  @Override
  public void createPatient(CreatePatientRequest req, StreamObserver<CreatePatientReply> resObs) {
    // Getting Employee Authentication
    Session session = getSession(req.getAccTok());
    // Enforcing Policy Authoring Authorization
    pepFrontend.checkDecision(session.getRole(), "PatientDetails", "Write");
    // Executing Employee Request
    DBPatientDetails pDetails = db.writePatientDetails(req.getNif(), req.getName());
    // Building Employee Response
    Patient patient =
        Patient.newBuilder()
            .setPid(pDetails.getPid())
            .setNif(pDetails.getNif())
            .setName(pDetails.getName())
            .build();
    resObs.onNext(CreatePatientReply.newBuilder().setPat(patient).build());
    resObs.onCompleted();
  }

  @Override
  public void patientDetails(
      PatientDetailsRequest req, StreamObserver<PatientDetailsReply> resObs) {
    // Getting Employee Authentication
    Session session = getSession(req.getAccTok());
    // Enforcing Policy Authoring Authorization
    pepFrontend.checkDecision(session.getRole(), "PatientDetails", "Read");
    // Executing Employee Request
    List<DBPatientDetails> pDetailsList =
        db.readPatientDetails(req.getPat().getPid(), req.getPat().getNif(), req.getPat().getName());
    // Building Employee Response
    PatientDetailsReply.Builder resPDetails = PatientDetailsReply.newBuilder();
    for (DBPatientDetails pDetails : pDetailsList) {
      resPDetails.addPats(
          Patient.newBuilder()
              .setPid(pDetails.getPid())
              .setNif(pDetails.getNif())
              .setName(pDetails.getName())
              .build());
    }
    resObs.onNext(resPDetails.build());
    resObs.onCompleted();
  }

  @Override
  public void changeMode(ChangeModeRequest req, StreamObserver<ChangeModeReply> resObs) {
    // Getting Employee Authentication
    Session session = getSession(req.getAccTok());
    // Enforcing Policy Authoring Authorization
    pepFrontend.checkDecision(session.getRole(), "Mode", "Write");
    // Executing Employee Request
    pepFrontend.setEnvironmentValue(req.getModeValue());
    // Building Employee Response
    resObs.onNext(ChangeModeReply.getDefaultInstance());
    resObs.onCompleted();
  }

  @Override
  public void checkMode(CheckModeRequest req, StreamObserver<CheckModeReply> resObs) {
    // Getting Employee Authentication
    Session session = getSession(req.getAccTok());
    // Enforcing Policy Authoring Authorization
    pepFrontend.checkDecision(session.getRole(), "Mode", "Read");
    // Executing & Building Employee Request
    resObs.onNext(
        CheckModeReply.newBuilder().setModeValue(pepFrontend.getEnvironmentValue()).build());
    resObs.onCompleted();
  }

  /*==========================
  | Service Impl Auxiliaries |
  ==========================*/

  private Session getSession(String tok) {
    Session session = SessionsManager.getSession(tok, true);
    if (session == null)
      throw new HSRuntimeException(Status.UNAUTHENTICATED, ErrorMessage.INVALID_TOKEN.lbl);
    return session;
  }

  private LocalDateTime toLocalDateTime(Timestamp ts) {
    return LocalDateTime.ofEpochSecond(ts.getSeconds(), ts.getNanos(), ZoneOffset.UTC);
  }

  private Timestamp toTimestamp(LocalDateTime ts) {
    Instant ins = ts.toInstant(ZoneOffset.UTC);
    return Timestamp.newBuilder().setSeconds(ins.getEpochSecond()).setNanos(ins.getNano()).build();
  }

  private Role strToRole(String role) {
    switch (role) {
      case "HospitalManager":
        return Role.HospitalManager;
      case "LaboratoryTechnician":
        return Role.LaboratoryTechnician;
      case "WardClerk":
        return Role.WardClerk;
      case "Porter":
        return Role.Porter;
      case "PatientServicesAssistant":
        return Role.PatientServicesAssistant;
      case "ClinicalAssistant":
        return Role.ClinicalAssistant;
      case "Nurse":
        return Role.Nurse;
      case "Doctor":
        return Role.Doctor;
      case "Employee":
        return Role.Employee;
      case "PartnerLab":
        return Role.PartnerLab;
      default:
        throw new AssertError(PEPFrontend.class.getSimpleName(), "strToRole");
    }
  }
}
