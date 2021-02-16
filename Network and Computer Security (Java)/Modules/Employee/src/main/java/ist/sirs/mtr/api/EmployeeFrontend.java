package ist.sirs.mtr.api;

import com.google.protobuf.Timestamp;
import io.grpc.ManagedChannel;
import io.grpc.StatusRuntimeException;
import io.grpc.netty.shaded.io.grpc.netty.GrpcSslContexts;
import io.grpc.netty.shaded.io.grpc.netty.NettyChannelBuilder;
import ist.sirs.mtr.error.AssertError;
import ist.sirs.mtr.proto.hse.ErrorMessage;
import ist.sirs.mtr.proto.hse.HSEmployee.*;
import ist.sirs.mtr.proto.hse.HSEmployeeServicesGrpc;
import ist.sirs.mtr.tres.TResManager;

import javax.net.ssl.SSLException;
import java.io.File;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class EmployeeFrontend {

  private static final DateTimeFormatter formatter =
      DateTimeFormatter.ofPattern("ccc, dd MMM yyyy, HH:mm").withLocale(Locale.UK);
  private final ManagedChannel channel;
  private final HSEmployeeServicesGrpc.HSEmployeeServicesBlockingStub stub;
  private String token;

  public EmployeeFrontend(String host, int port, String caCrtPath) {
    try {
      channel =
          NettyChannelBuilder.forAddress(host, port)
              .sslContext(GrpcSslContexts.forClient().trustManager(new File(caCrtPath)).build())
              .build();
    } catch (SSLException e) {
      throw new AssertError(EmployeeFrontend.class.getSimpleName(), "Constructor", e);
    }
    stub = HSEmployeeServicesGrpc.newBlockingStub(channel);
  }

  public String login(String uname, String pwd) {
    LoginRequest req = LoginRequest.newBuilder().setUname(uname).setPwd(pwd).build();
    String role;

    try {
      LoginReply res = stub.login(req);
      token = res.getAccTok();
      role = res.getRole().toString();

    } catch (StatusRuntimeException e) {
      return e.getMessage();
    }
    return String.format("%s%nRole: %s", Message.LOGIN_SUCCESS.lbl, role);
  }

  public String read(int pid, int recType, LocalDateTime dateTime) {
    if (token == null) return Message.TOKEN_NOT_AVAILABLE.lbl;
    ReadRequest req =
        ReadRequest.newBuilder()
            .setAccTok(token)
            .setPid(pid)
            .setRecTypeValue(recType)
            .setRecTs(toTimestamp(dateTime))
            .build();

    try {
      ReadReply res = stub.read(req);
      return parseRecords(pid, recType, res);
    } catch (StatusRuntimeException e) {
      return e.getMessage();
    }
  }

  public String patientDetails(int pid, int nif, String name) {
    if (token == null) return Message.TOKEN_NOT_AVAILABLE.lbl;
    PatientDetailsRequest req =
        PatientDetailsRequest.newBuilder()
            .setAccTok(token)
            .setPat(Patient.newBuilder().setPid(pid).setNif(nif).setName(name).build())
            .build();
    try {
      PatientDetailsReply res = stub.patientDetails(req);
      return parsePatients(res);
    } catch (StatusRuntimeException e) {
      return e.getMessage();
    }
  }

  public String checkMode() {
    if (token == null) return Message.TOKEN_NOT_AVAILABLE.lbl;
    CheckModeRequest req = CheckModeRequest.newBuilder().setAccTok(token).build();
    try {
      CheckModeReply res = stub.checkMode(req);
      return String.format("Current Mode: %s", res.getMode().toString());
    } catch (StatusRuntimeException e) {
      return e.getMessage();
    }
  }

  public String write(int pid, int recType, String recCont) {
    if (token == null) return Message.TOKEN_NOT_AVAILABLE.lbl;
    WriteRequest req =
        WriteRequest.newBuilder()
            .setAccTok(token)
            .setPid(pid)
            .setRecTypeValue(recType)
            .setRecCont(recCont)
            .build();

    try {
      stub.write(req);
    } catch (StatusRuntimeException e) {
      return e.getMessage();
    }
    return Message.WRITE_SUCCESS.lbl;
  }

  public String createUser(String uname, String pwd, int role) {
    if (token == null) return Message.TOKEN_NOT_AVAILABLE.lbl;
    CreateUserRequest req =
        CreateUserRequest.newBuilder()
            .setAccTok(token)
            .setUname(uname)
            .setPwd(pwd)
            .setRoleValue(role)
            .build();
    try {
      stub.createUser(req);
    } catch (StatusRuntimeException e) {
      return e.getMessage();
    }
    return Message.CREATE_USER_SUCCESS.lbl;
  }

  public String createPatient(int nif, String name) {
    if (token == null) return Message.TOKEN_NOT_AVAILABLE.lbl;
    CreatePatientRequest req =
        CreatePatientRequest.newBuilder().setAccTok(token).setNif(nif).setName(name).build();
    try {
      CreatePatientReply res = stub.createPatient(req);
      return parsePatient(res.getPat());
    } catch (StatusRuntimeException e) {
      return e.getMessage();
    }
  }

  public String changeMode(int mode) {
    if (token == null) return Message.TOKEN_NOT_AVAILABLE.lbl;
    ChangeModeRequest req =
        ChangeModeRequest.newBuilder().setAccTok(token).setModeValue(mode).build();
    try {
      stub.changeMode(req);
    } catch (StatusRuntimeException e) {
      return e.getMessage();
    }
    return Message.CHANCE_MODE_SUCCESS.lbl;
  }

  public String tResAuth(int pid, int recId) {
    if (token == null) return Message.TOKEN_NOT_AVAILABLE.lbl;
    TResAuthRequest req =
        TResAuthRequest.newBuilder().setAccTok(token).setPid(pid).setRecId(recId).build();

    try {
      TResAuthReply res = stub.tResAuth(req);
      return TResManager.checkTResAuth(
              pid, recId, res.getLabSig().toByteArray(), res.getLabCrt().toByteArray())
          ? Message.VALID_TEST_RESULT.lbl
          : Message.INVALID_TEST_RESULT.lbl;
    } catch (StatusRuntimeException | AssertError e) {
      return e.getMessage();
    }
  }

  public String logout() {
    if (token == null) return Message.TOKEN_NOT_AVAILABLE.lbl;
    LogoutRequest req = LogoutRequest.newBuilder().setAccTok(token).build();

    stub.logout(req);
    token = null;
    return Message.LOGOUT_SUCCESS.lbl;
  }

  private String parseRecords(int pid, int recType, ReadReply res) {
    if (res.getRecsCount() == 0) return ErrorMessage.NO_RECORD.lbl;
    StringBuilder recs = new StringBuilder(String.format("Patient Id: %d, ", pid));
    recs.append(String.format("Record Type: %d%n", recType));

    for (PatientRecord pr : res.getRecsList()) {
      recs.append(String.format("Record Id: %d, ", pr.getRecId()));
      recs.append(String.format("Record Timestamp: %s, ", tsToStr(pr.getRecTs())));
      recs.append(String.format("Record Content: %s%n", pr.getRecCont()));
      if (recType == PatientRecordType.TestResultsRecords_VALUE) {
        TResManager.addTRes(pid, pr.getRecId(), pr.getRecCont(), toMillis(pr.getRecTs()));
      }
    }
    return recs.toString();
  }

  private String parsePatient(Patient pat) {
    return String.format(
        "Patient Id: %d, Patient Nif: %d, Patient Name: %s%n",
        pat.getPid(), pat.getNif(), pat.getName());
  }

  private String parsePatients(PatientDetailsReply res) {
    StringBuilder pats = new StringBuilder();
    for (Patient pat : res.getPatsList()) {
      pats.append(parsePatient(pat));
    }
    return pats.toString();
  }

  private Timestamp toTimestamp(LocalDateTime ts) {
    if (ts == null) return Timestamp.getDefaultInstance();
    Instant ins = ts.toInstant(ZoneOffset.UTC);
    return Timestamp.newBuilder().setSeconds(ins.getEpochSecond()).setNanos(ins.getNano()).build();
  }

  private LocalDateTime toLocalDateTime(Timestamp ts) {
    return LocalDateTime.ofEpochSecond(ts.getSeconds(), ts.getNanos(), ZoneOffset.UTC);
  }

  private long toMillis(Timestamp ts) {
    return Instant.ofEpochSecond(ts.getSeconds(), ts.getNanos()).toEpochMilli();
  }

  private String tsToStr(Timestamp ts) {
    return formatter.format(toLocalDateTime(ts));
  }

  public void shutdown() {
    channel.shutdown();
  }
}
