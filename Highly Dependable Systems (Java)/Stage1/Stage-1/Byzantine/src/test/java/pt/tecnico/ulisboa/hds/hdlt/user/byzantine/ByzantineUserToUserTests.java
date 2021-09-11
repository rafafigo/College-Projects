package pt.tecnico.ulisboa.hds.hdlt.user.byzantine;

import io.grpc.BindableService;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import pt.tecnico.ulisboa.hds.hdlt.byzantine.api.ByzantineUserToUserFrontend;
import pt.tecnico.ulisboa.hds.hdlt.byzantine.api.ByzantineUserUserServicesImpl;
import pt.tecnico.ulisboa.hds.hdlt.user.UserApp;
import pt.tecnico.ulisboa.hds.hdlt.user.api.UserCrypto;
import pt.tecnico.ulisboa.hds.hdlt.user.api.UserToUserFrontend;
import pt.tecnico.ulisboa.hds.hdlt.user.api.UserUserServicesImpl;
import pt.tecnico.ulisboa.hds.hdlt.user.location.GridManager;
import pt.tecnico.ulisboa.hds.hdlt.user.session.Session;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ByzantineUserToUserTests extends BaseTests {

  private static final Integer nByzantineUsers = 2;
  private static final Integer uMaxDistance = 5;
  private static final Integer expSec = 60;
  private static final Map<String, UserToUserFrontend> uuFrontends = new HashMap<>();
  private static final Map<String, ByzantineUserToUserFrontend> buuFrontends = new HashMap<>();
  private static final Map<String, Server> uServers = new HashMap<>();
  private static Map<String, String> uURLs;

  @BeforeAll
  public static void oneTimeSetUp() throws IOException {
    newUser("User1", true, false);
    newUser("User2", true, false);
    newUser("User3", false, false);
    newUser("User5", false, false);
  }

  private static void newUser(String uname, boolean isByzantine, boolean isImpersonator)
      throws IOException {
    uURLs = UserApp.parseUsersURLs(myUsersURLsPath);
    int uPort = UserApp.parsePort(uURLs.get(uname).split(":")[1]);
    GridManager grid = new GridManager(uname, myGridPath);
    Session session = new Session(expSec);
    String uPrivKeyPath = String.format("%s/%s.der", myUserPrivKeyDirPath, uname);
    UserCrypto uCrypto = new UserCrypto(myServerPubKey, uPrivKeyPath, myUserPubKeyDirPath, session);
    String fUname = isImpersonator ? grid.getAllUsers().stream().findFirst().orElse(uname) : uname;
    BindableService uuBindableService;
    if (isByzantine) {
      ByzantineUserToUserFrontend buuFrontend =
          new ByzantineUserToUserFrontend(
              fUname, grid, uCrypto, nByzantineUsers, uMaxDistance, uURLs);
      ByzantineUserUserServicesImpl buuService = new ByzantineUserUserServicesImpl(grid, uCrypto);
      buuFrontends.put(uname, buuFrontend);
      uuBindableService = buuService;
    } else {
      UserToUserFrontend uuFrontend =
          new UserToUserFrontend(fUname, grid, uCrypto, nByzantineUsers, uMaxDistance, uURLs);
      UserUserServicesImpl uuService = new UserUserServicesImpl(uname, grid, uCrypto, uMaxDistance);
      uuFrontends.put(uname, uuFrontend);
      uuBindableService = uuService;
    }
    Server uServer = ServerBuilder.forPort(uPort).addService(uuBindableService).build();
    uServer.start();
    uServers.put(uname, uServer);
  }

  @AfterAll
  public static void oneTimeTearDown() {
    uServers.values().forEach(Server::shutdown);
    uuFrontends.values().forEach(UserToUserFrontend::shutdown);
    buuFrontends.values().forEach(ByzantineUserToUserFrontend::shutdown);
  }

  @Test
  public void requestULProofToFarAwayUser() {
    Integer epoch = 1;
    String bUname = "User1";
    String uname = "User5";

    assertEquals(
        "FAILED_PRECONDITION: User Too Far Away!",
        buuFrontends.get(bUname).getAuthProofsFromFarAwayUser(epoch, uURLs.get(uname)));
  }
}
