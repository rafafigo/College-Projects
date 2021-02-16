package ist.sirs.mtr.api;

import io.grpc.ManagedChannel;
import io.grpc.Status;
import io.grpc.netty.shaded.io.grpc.netty.GrpcSslContexts;
import io.grpc.netty.shaded.io.grpc.netty.NettyChannelBuilder;
import ist.sirs.mtr.error.AssertError;
import ist.sirs.mtr.exception.HSRuntimeException;
import ist.sirs.mtr.proto.hspa.HSPolicyAuthoringContract.*;
import ist.sirs.mtr.proto.hspa.HSPolicyAuthoringServicesGrpc;
import ist.sirs.mtr.proto.hspl.ErrorMessage;

import javax.net.ssl.SSLException;
import java.io.File;

public class PEPFrontend {

  private final ManagedChannel channel;
  private final HSPolicyAuthoringServicesGrpc.HSPolicyAuthoringServicesBlockingStub stub;
  private Environment environment = Environment.Regular;

  public PEPFrontend(String host, int port, String caCrtPath) {
    try {
      channel =
          NettyChannelBuilder.forAddress(host, port)
              .sslContext(GrpcSslContexts.forClient().trustManager(new File(caCrtPath)).build())
              .build();
    } catch (SSLException e) {
      throw new AssertError(PEPFrontend.class.getSimpleName(), "Constructor", e);
    }
    stub = HSPolicyAuthoringServicesGrpc.newBlockingStub(channel);
  }

  /*==========
  | Services |
  ==========*/

  public void checkDecision(String subject, String resource, String action) {
    if (!stub.decide(
            DecideRequest.newBuilder()
                .setSubject(strToSubject(subject))
                .setResource(strToResource(resource))
                .setAction(strToAction(action))
                .setEnvironment(environment)
                .build())
        .getDecision())
      throw new HSRuntimeException(Status.PERMISSION_DENIED, ErrorMessage.PERMISSION_DENIED.lbl);
  }

  /*=============
  | Auxiliaries |
  =============*/

  private Subject strToSubject(String subject) {
    switch (subject) {
      case "HospitalManager":
        return Subject.HospitalManager;
      case "LaboratoryTechnician":
        return Subject.LaboratoryTechnician;
      case "WardClerk":
        return Subject.WardClerk;
      case "Porter":
        return Subject.Porter;
      case "PatientServicesAssistant":
        return Subject.PatientServicesAssistant;
      case "ClinicalAssistant":
        return Subject.ClinicalAssistant;
      case "Nurse":
        return Subject.Nurse;
      case "Doctor":
        return Subject.Doctor;
      case "Employee":
        return Subject.Employee;
      case "PartnerLab":
        return Subject.PartnerLab;
      default:
        throw new AssertError(PEPFrontend.class.getSimpleName(), "strToSubject");
    }
  }

  private Resource strToResource(String resource) {
    switch (resource) {
      case "TestResultsRecords":
        return Resource.TestResultsRecords;
      case "HousekeepingRecords":
        return Resource.HousekeepingRecords;
      case "DietRecords":
        return Resource.DietRecords;
      case "TransportsRecords":
        return Resource.TransportsRecords;
      case "ReceptionRecords":
        return Resource.ReceptionRecords;
      case "PrescriptionRecords":
        return Resource.PrescriptionRecords;
      case "EmployeeCredentials":
        return Resource.EmployeeCredentials;
      case "PatientDetails":
        return Resource.PatientDetails;
      case "Mode":
        return Resource.Mode;
      default:
        throw new AssertError(PEPFrontend.class.getSimpleName(), "strToResource");
    }
  }

  private Action strToAction(String action) {
    switch (action) {
      case "Read":
        return Action.Read;
      case "Write":
        return Action.Write;
      case "TestResultAuthenticityCheck":
        return Action.TestResultAuthenticityCheck;
      default:
        throw new AssertError(PEPFrontend.class.getSimpleName(), "strToAction");
    }
  }

  /*=================
  | Outside Methods |
  =================*/

  public int getEnvironmentValue() {
    synchronized (this) {
      return environment.getNumber();
    }
  }

  public void setEnvironmentValue(int value) {
    synchronized (this) {
      environment = Environment.forNumber(value);
    }
  }

  public void shutdown() {
    channel.shutdown();
  }
}
