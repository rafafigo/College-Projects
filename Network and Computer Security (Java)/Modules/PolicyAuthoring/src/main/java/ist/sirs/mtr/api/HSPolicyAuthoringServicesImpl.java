package ist.sirs.mtr.api;

import io.grpc.stub.StreamObserver;
import ist.sirs.mtr.pdp.PDP;
import ist.sirs.mtr.proto.hspa.HSPolicyAuthoringContract.DecideReply;
import ist.sirs.mtr.proto.hspa.HSPolicyAuthoringContract.DecideRequest;
import ist.sirs.mtr.proto.hspa.HSPolicyAuthoringServicesGrpc.HSPolicyAuthoringServicesImplBase;
import org.ow2.authzforce.core.pdp.api.DecisionRequest;

public class HSPolicyAuthoringServicesImpl extends HSPolicyAuthoringServicesImplBase {

  private final PDP pdp;

  public HSPolicyAuthoringServicesImpl(PDP pdp) {
    this.pdp = pdp;
  }

  @Override
  public void decide(DecideRequest req, StreamObserver<DecideReply> resObs) {
    DecisionRequest decReq =
        pdp.createRequest(
            req.getSubject().toString(),
            req.getResource().toString(),
            req.getAction().toString(),
            req.getEnvironment().toString());

    resObs.onNext(DecideReply.newBuilder().setDecision(pdp.evaluate(decReq)).build());
    resObs.onCompleted();
  }
}
