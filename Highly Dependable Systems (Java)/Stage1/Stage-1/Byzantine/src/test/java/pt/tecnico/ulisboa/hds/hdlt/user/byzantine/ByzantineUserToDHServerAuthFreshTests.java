package pt.tecnico.ulisboa.hds.hdlt.user.byzantine;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import pt.tecnico.ulisboa.hds.hdlt.byzantine.api.ByzantineUserToDHServerFrontend;
import pt.tecnico.ulisboa.hds.hdlt.user.api.UserCrypto;
import pt.tecnico.ulisboa.hds.hdlt.user.session.Session;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ByzantineUserToDHServerAuthFreshTests extends BaseTests {
  private static final String uname = "User1";
  private static final Integer expSec = 60;
  private static ByzantineUserToDHServerFrontend dhFrontend;

  @BeforeAll
  static void oneTimeSetUp() {
    newUser();
  }

  private static void newUser() {
    Session session = new Session(expSec);
    String uPrivKeyPath = String.format("%s/%s.der", myUserPrivKeyDirPath, "User1");
    UserCrypto uCrypto = new UserCrypto(myServerPubKey, uPrivKeyPath, myUserPubKeyDirPath, session);
    dhFrontend = new ByzantineUserToDHServerFrontend(uname, uCrypto, myServerHost, myServerPort);
  }

  @AfterAll
  public static void oneTimeTearDown() {
    dhFrontend.shutdown();
  }

  @Test
  public void dHNoFreshness() {
    assertEquals("INVALID_ARGUMENT: Freshness Tests Failed!", dhFrontend.dHNoFreshness());
  }

  @Test
  public void dHNoValidSignature() {
    assertEquals("INVALID_ARGUMENT: Authenticity Tests Failed!", dhFrontend.dHNoValidSignature());
  }
}
