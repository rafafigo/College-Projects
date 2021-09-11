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
import pt.tecnico.ulisboa.hds.hdlt.user.api.*;
import pt.tecnico.ulisboa.hds.hdlt.user.error.UserRuntimeException;
import pt.tecnico.ulisboa.hds.hdlt.user.location.GridManager;
import pt.tecnico.ulisboa.hds.hdlt.user.session.Session;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ByzantineUserToServerTests extends BaseTests {

  private static final Integer nByzantineUsers = 2;
  private static final Integer uMaxDistance = 5;
  private static final Integer expSec = 60;
  private static final Map<String, Session> uSessions = new HashMap<>();
  private static final Map<String, UserCrypto> uCryptos = new HashMap<>();
  private static final Map<String, UserToDHServerFrontend> dhFrontends = new HashMap<>();
  private static final Map<String, UserToServerFrontend> usFrontends = new HashMap<>();
  private static final Map<String, UserToUserFrontend> uuFrontends = new HashMap<>();
  private static final Map<String, ByzantineUserToUserFrontend> buuFrontends = new HashMap<>();
  private static final Map<String, ByzantineUserUserServicesImpl> buuServices = new HashMap<>();
  private static final Map<String, Server> uServers = new HashMap<>();

  @BeforeAll
  public static void oneTimeSetUp() throws IOException {
    newUser("User1", true, true);
    newUser("User2", true, false);
    newUser("User3", true, false);
    newUser("User4", true, false);
    newUser("User5", false, false);
    newUser("User6", false, false);
    newUser("User7", false, false);
    newUser("User8", false, false);
    newUser("User9", false, false);
    newUser("User10", false, false);
  }

  private static void newUser(String uname, boolean isByzantine, boolean isImpersonator)
      throws IOException {
    Map<String, String> uURLs = UserApp.parseUsersURLs(myUsersURLsPath);
    int uPort = UserApp.parsePort(uURLs.get(uname).split(":")[1]);
    GridManager grid = new GridManager(uname, myGridPath);
    Session session = new Session(expSec);
    String uPrivKeyPath = String.format("%s/%s.der", myUserPrivKeyDirPath, uname);
    UserCrypto uCrypto = new UserCrypto(myServerPubKey, uPrivKeyPath, myUserPubKeyDirPath, session);
    String fUname = isImpersonator ? grid.getAllUsers().stream().findFirst().orElse(uname) : uname;
    UserToDHServerFrontend dhFrontend =
        new UserToDHServerFrontend(fUname, uCrypto, myServerHost, myServerPort, session);
    UserToServerFrontend usFrontend =
        new UserToServerFrontend(
            fUname, grid, uCrypto, dhFrontend, myServerHost, myServerPort, session);
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
    uSessions.put(uname, session);
    uCryptos.put(uname, uCrypto);
    dhFrontends.put(uname, dhFrontend);
    usFrontends.put(uname, usFrontend);
  }

  @AfterAll
  public static void oneTimeTearDown() {
    uServers.values().forEach(Server::shutdown);
    dhFrontends.values().forEach(UserToDHServerFrontend::shutdown);
    usFrontends.values().forEach(UserToServerFrontend::shutdown);
    uuFrontends.values().forEach(UserToUserFrontend::shutdown);
    buuFrontends.values().forEach(ByzantineUserToUserFrontend::shutdown);
  }

  @Test
  public void obtainULImpersonator() {
    String uname = "User1";
    Integer epoch = 1;

    UserRuntimeException exception =
        assertThrows(UserRuntimeException.class, () -> usFrontends.get(uname).obtainUL(epoch));
    assertEquals("INVALID_ARGUMENT: Possible Impersonation Attack!", exception.getMessage());
  }

  @Test
  public void submitULImpersonator() {
    String uname = "User1";
    Integer epoch = 1;

    buuServices.get("User2").setMode(ByzantineUserUserServicesImpl.ALWAYS_SIGN);
    buuServices.get("User3").setMode(ByzantineUserUserServicesImpl.ALWAYS_SIGN);

    UserRuntimeException exception =
        assertThrows(
            UserRuntimeException.class,
            () -> {
              // Getting Some Proofs of Byzantine Users
              Map<String, byte[]> authProofs =
                  buuFrontends.get(uname).getAuthProofsImpersonation(epoch);
              usFrontends.get(uname).submitULReport(epoch, authProofs);
            });
    assertEquals("INVALID_ARGUMENT: Possible Impersonation Attack!", exception.getMessage());
  }

  @Test
  public void submitULSelfGeneratedProofs() {
    String uname = "User2";
    Integer epoch = 1;

    UserRuntimeException exception =
        assertThrows(
            UserRuntimeException.class,
            () -> {
              // This User Attempts to Create Location Proofs itself!
              Map<String, byte[]> authProofs =
                  buuFrontends.get(uname).getSelfGeneratedAuthProofs(epoch);
              usFrontends.get(uname).submitULReport(epoch, authProofs);
            });
    assertEquals("INVALID_ARGUMENT: Not Enough Valid Proofs!", exception.getMessage());
  }

  @Test
  public void submitULReplicatedProofs() {
    String uname = "User2";
    Integer epoch = 1;

    buuServices.get("User1").setMode(ByzantineUserUserServicesImpl.ALWAYS_SIGN);

    UserRuntimeException exception =
        assertThrows(
            UserRuntimeException.class,
            () -> {
              // This User Tries to Replicate Location Proofs Made By Another Byzantine User!
              Map<String, byte[]> authProofs =
                  buuFrontends.get(uname).getReplicatedAuthProofs(epoch, "User1");
              usFrontends.get(uname).submitULReport(epoch, authProofs);
            });
    assertEquals("INVALID_ARGUMENT: Not Enough Valid Proofs!", exception.getMessage());
  }

  @Test
  public void submitULWithoutProofs() {
    String uname = "User2";
    Integer epoch = 1;

    UserRuntimeException exception =
        assertThrows(
            UserRuntimeException.class,
            () -> {
              // This User Tries to Submit its Location With no Proofs!
              usFrontends.get(uname).submitULReport(epoch, new HashMap<>());
            });
    assertEquals("INVALID_ARGUMENT: Number of Proofs Insufficient!", exception.getMessage());
  }

  @Test
  public void submitULWithInsufficientProofs() {
    String uname = "User2";
    Integer epoch = 1;

    buuServices.get("User1").setMode(ByzantineUserUserServicesImpl.NEVER_SIGN);
    buuServices.get("User4").setMode(ByzantineUserUserServicesImpl.NEVER_SIGN);

    UserRuntimeException exception =
        assertThrows(
            UserRuntimeException.class,
            () -> {
              // This User Does Not Get Enough Location Proofs!
              Map<String, byte[]> authProofs =
                  buuFrontends.get(uname).getAuthProofsImpersonation(epoch);
              usFrontends.get(uname).submitULReport(epoch, authProofs);
            });
    assertEquals("INVALID_ARGUMENT: Number of Proofs Insufficient!", exception.getMessage());
  }

  @Test
  public void submitULReportWithInvalidSession() {
    Integer epoch = 1;
    String bUname = "User5";
    String uname = "User6";
    Session bSession = uSessions.get(bUname);

    dhFrontends.get(bUname).dH();
    dhFrontends.get(uname).dH();

    uCryptos.get(bUname).setSession(uSessions.get(uname));
    dhFrontends.get(bUname).setSession(uSessions.get(uname));
    usFrontends.get(bUname).setSession(uSessions.get(uname));

    UserRuntimeException exception =
        assertThrows(
            UserRuntimeException.class,
            () -> {
              Map<String, byte[]> authProofs = uuFrontends.get(bUname).getAuthProofs(epoch);
              usFrontends.get(bUname).submitULReport(epoch, authProofs);
            });

    assertEquals("INVALID_ARGUMENT: Invalid Session!", exception.getMessage());
    uCryptos.get(bUname).setSession(bSession);
    dhFrontends.get(bUname).setSession(bSession);
    usFrontends.get(bUname).setSession(bSession);
  }

  @Test
  public void obtainULWithInvalidSession() {
    Integer epoch = 1;
    String bUname = "User5";
    String uname = "User6";
    Session bSession = uSessions.get(bUname);

    dhFrontends.get(bUname).dH();
    dhFrontends.get(uname).dH();

    uCryptos.get(bUname).setSession(uSessions.get(uname));
    dhFrontends.get(bUname).setSession(uSessions.get(uname));
    usFrontends.get(bUname).setSession(uSessions.get(uname));

    UserRuntimeException exception =
        assertThrows(UserRuntimeException.class, () -> usFrontends.get(bUname).obtainUL(epoch));

    assertEquals("INVALID_ARGUMENT: Authenticity Tests Failed!", exception.getMessage());
    uCryptos.get(bUname).setSession(bSession);
    dhFrontends.get(bUname).setSession(bSession);
    usFrontends.get(bUname).setSession(bSession);
  }
}
