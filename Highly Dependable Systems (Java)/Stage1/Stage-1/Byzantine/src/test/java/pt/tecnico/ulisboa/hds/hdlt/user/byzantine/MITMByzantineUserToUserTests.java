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
import pt.tecnico.ulisboa.hds.hdlt.user.error.UserRuntimeException;
import pt.tecnico.ulisboa.hds.hdlt.user.location.GridManager;
import pt.tecnico.ulisboa.hds.hdlt.user.session.Session;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static pt.tecnico.ulisboa.hds.hdlt.lib.crypto.Crypto.sleep;

public class MITMByzantineUserToUserTests extends BaseTests {

  private static final Integer nByzantineUsers = 2;
  private static final Integer uMaxDistance = 5;
  private static final Integer expSec = 60;
  private static final Map<String, UserToUserFrontend> uuFrontends = new HashMap<>();
  private static final Map<String, ByzantineUserToUserFrontend> buuFrontends = new HashMap<>();
  private static final Map<String, ByzantineUserUserServicesImpl> buuServices = new HashMap<>();
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
      buuServices.put(uname, buuService);
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
  public void requestULMalformedExceptionProof() {
    Integer epoch = 1;
    String uname = "User1";
    String destUname = "User2";

    buuServices.get(destUname).setMode(ByzantineUserUserServicesImpl.MALFORMED_EXCEPTION);

    UserRuntimeException exception =
        assertThrows(
            UserRuntimeException.class,
            () -> {
              // This User Receives a Response which is not Properly Signed
              buuFrontends
                  .get(uname)
                  .getMalformedExceptionAuthProofs(epoch, uURLs.get(destUname), destUname);
            });
    assertEquals("Authenticity Tests Failed!", exception.getMessage());
  }

  @Test
  public void requestULProofDoesNotAnswer() {
    Integer epoch = 1;
    String uname = "User3";
    String bDestUname = "User1";
    String destUname = "User5";

    buuServices.get(bDestUname).setMode(ByzantineUserUserServicesImpl.DOES_NOT_ANSWER);

    new Thread(
            () -> {
              sleep(2500);
              buuServices.get(bDestUname).setMode(ByzantineUserUserServicesImpl.ALWAYS_SIGN);
            })
        .start();
    // This User Retries its Requests
    // Because a Man In the Middle is Dropping a Request
    Map<String, byte[]> authProofs = uuFrontends.get(uname).getAuthProofs(epoch);
    assertTrue(authProofs.containsKey(destUname) && authProofs.containsKey(bDestUname));
  }
}
