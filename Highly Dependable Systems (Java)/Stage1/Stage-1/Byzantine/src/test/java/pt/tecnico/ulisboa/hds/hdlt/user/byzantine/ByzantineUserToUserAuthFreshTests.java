package pt.tecnico.ulisboa.hds.hdlt.user.byzantine;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import pt.tecnico.ulisboa.hds.hdlt.byzantine.api.ByzantineUserToUserFrontend;
import pt.tecnico.ulisboa.hds.hdlt.user.UserApp;
import pt.tecnico.ulisboa.hds.hdlt.user.api.UserCrypto;
import pt.tecnico.ulisboa.hds.hdlt.user.api.UserUserServicesImpl;
import pt.tecnico.ulisboa.hds.hdlt.user.location.GridManager;
import pt.tecnico.ulisboa.hds.hdlt.user.session.Session;

import java.io.IOException;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ByzantineUserToUserAuthFreshTests extends BaseTests {
  private static final String uname = "User1";
  private static final Integer expSec = 60;
  private static final Integer uMaxDistance = 5;
  private static final Integer nByzantineUsers = 2;
  private static Integer epoch;
  private static String uUrl;
  private static ByzantineUserToUserFrontend buFrontend;
  private static Server buServer;

  @BeforeAll
  static void oneTimeSetUp() throws IOException {
    newUser();
  }

  private static void newUser() throws IOException {
    GridManager grid = new GridManager(uname, myGridPath);
    epoch = grid.getEpochs(uname).stream().findFirst().orElse(0);
    Session session = new Session(expSec);
    String uPrivKeyPath = String.format("%s/%s.der", myUserPrivKeyDirPath, uname);
    UserCrypto uCrypto = new UserCrypto(myServerPubKey, uPrivKeyPath, myUserPubKeyDirPath, session);
    Map<String, String> uUrls = UserApp.parseUsersURLs(myUsersURLsPath);
    buFrontend = new ByzantineUserToUserFrontend(uname, grid, uCrypto, nByzantineUsers, 1, uUrls);
    String unameDest = grid.getCloseUsers(epoch, uMaxDistance).stream().findFirst().orElse("");
    uUrl = UserApp.parseUsersURLs(myUsersURLsPath).get(unameDest);
    int uPort = UserApp.parsePort(uUrl.split(":")[1]);
    buServer =
        ServerBuilder.forPort(uPort)
            .addService(new UserUserServicesImpl(unameDest, grid, uCrypto, uMaxDistance))
            .build();
    buServer.start();
  }

  @AfterAll
  public static void oneTimeTearDown() {
    buFrontend.shutdown();
    buServer.shutdown();
  }

  @Test
  public void getAuthProofsInvalidSignature() {
    assertEquals(
        "INVALID_ARGUMENT: Authenticity Tests Failed!",
        buFrontend.getAuthProofsInvalidSignature(epoch, uUrl));
  }
}
