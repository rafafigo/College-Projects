package ist.sirs.mtr.pdp;

import ist.sirs.mtr.error.AssertError;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.DecisionType;
import org.ow2.authzforce.core.pdp.api.AttributeFqn;
import org.ow2.authzforce.core.pdp.api.AttributeFqns;
import org.ow2.authzforce.core.pdp.api.DecisionRequest;
import org.ow2.authzforce.core.pdp.api.DecisionRequestBuilder;
import org.ow2.authzforce.core.pdp.api.value.AttributeBag;
import org.ow2.authzforce.core.pdp.api.value.Bags;
import org.ow2.authzforce.core.pdp.api.value.StandardDatatypes;
import org.ow2.authzforce.core.pdp.api.value.StringValue;
import org.ow2.authzforce.core.pdp.impl.BasePdpEngine;
import org.ow2.authzforce.core.pdp.impl.PdpEngineConfiguration;

import java.io.IOException;
import java.util.Optional;

public class PDP {

  final BasePdpEngine pdpEngine;
  final String[] XACML_3_0_ATTRIBUTE_CATEGORIES =
      new String[] {
        "urn:oasis:names:tc:xacml:3.0::subjectcategory:accesssubject",
        "urn:oasis:names:tc:xacml:3.0::attributecategory:resource",
        "urn:oasis:names:tc:xacml:3.0::attributecategory:action",
        "urn:oasis:names:tc:xacml:3.0::attributecategory:environment"
      };

  final String[] XACML_1_0_ATTRIBUTE_IDS =
      new String[] {
        "urn:oasis:names:tc:xacml:1.0:subject:subject-id",
        "urn:oasis:names:tc:xacml:1.0:resource:resource-id",
        "urn:oasis:names:tc:xacml:1.0:action:action-id",
        "urn:oasis:names:tc:xacml:1.0:environment:environment-id"
      };

  public PDP(String pdpConfPath) {
    try {
      this.pdpEngine = new BasePdpEngine(PdpEngineConfiguration.getInstance(pdpConfPath));
    } catch (IOException e) {
      throw new AssertError(PDP.class.getSimpleName(), "Constructor", e);
    }
  }

  public DecisionRequest createRequest(
      String subject, String resource, String action, String environment) {
    final DecisionRequestBuilder<?> reqBuilder = pdpEngine.newRequestBuilder(4, 4);
    String[] values = new String[] {subject, resource, action, environment};

    for (int i = 0; i < 4; i++) {
      final AttributeFqn attributeId =
          AttributeFqns.newInstance(
              XACML_3_0_ATTRIBUTE_CATEGORIES[i], Optional.empty(), XACML_1_0_ATTRIBUTE_IDS[i]);
      final AttributeBag<?> attributeValues =
          Bags.singletonAttributeBag(StandardDatatypes.STRING, new StringValue(values[i]));
      reqBuilder.putNamedAttributeIfAbsent(attributeId, attributeValues);
    }
    return reqBuilder.build(false);
  }

  public boolean evaluate(DecisionRequest decReq) {
    return pdpEngine.evaluate(decReq).getDecision() == DecisionType.PERMIT;
  }
}
