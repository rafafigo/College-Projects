package pt.tecnico.ulisboa.hds.hdlt.user.byzantine;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import pt.tecnico.ulisboa.hds.hdlt.byzantine.api.ByzantineUserToServerFrontend;
import pt.tecnico.ulisboa.hds.hdlt.user.api.UserCrypto;
import pt.tecnico.ulisboa.hds.hdlt.user.api.UserToDHServerFrontend;
import pt.tecnico.ulisboa.hds.hdlt.user.location.GridManager;
import pt.tecnico.ulisboa.hds.hdlt.user.session.Session;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ByzantineUserToServerAuthFreshTests extends BaseTests {
  private static final String uname = "User1";
  private static final Integer expSec = 60;
  private static Integer epoch;
  private static UserToDHServerFrontend dhFrontend;
  private static ByzantineUserToServerFrontend bsFrontend;

  @BeforeAll
  static void oneTimeSetUp() throws IOException {
    newUser();
    dhFrontend.dH();
  }

  private static void newUser() throws IOException {
    GridManager grid = new GridManager(uname, myGridPath);
    epoch = grid.getEpochs(uname).stream().findFirst().orElse(0);
    Session session = new Session(expSec);
    String uPrivKeyPath = String.format("%s/%s.der", myUserPrivKeyDirPath, uname);
    UserCrypto uCrypto = new UserCrypto(myServerPubKey, uPrivKeyPath, myUserPubKeyDirPath, session);
    dhFrontend = new UserToDHServerFrontend(uname, uCrypto, myServerHost, myServerPort, session);
    bsFrontend =
        new ByzantineUserToServerFrontend(uname, dhFrontend, myServerHost, myServerPort, session);
  }

  @AfterAll
  public static void oneTimeTearDown() {
    dhFrontend.shutdown();
    bsFrontend.shutdown();
  }

  @Test
  public void submitULReportFreshness() {
    assertEquals(
        "INVALID_ARGUMENT: Freshness Tests Failed!", bsFrontend.submitULReportNoFreshness(epoch));
  }

  @Test
  public void submitULReportNoValidHMAC() {
    assertEquals(
        "INVALID_ARGUMENT: Authenticity Tests Failed!",
        bsFrontend.submitULReportNoValidHMAC(epoch));
  }

  @Test
  public void obtainULNoFreshness() {
    assertEquals(
        "INVALID_ARGUMENT: Freshness Tests Failed!", bsFrontend.obtainULNoFreshness(epoch));
  }

  @Test
  public void obtainULNoValidHMAC() {
    assertEquals(
        "INVALID_ARGUMENT: Authenticity Tests Failed!", bsFrontend.obtainULNoValidHMAC(epoch));
  }
}
