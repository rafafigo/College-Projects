package pt.tecnico.ulisboa.hds.hdlt.user;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import org.junit.jupiter.api.*;
import pt.tecnico.ulisboa.hds.hdlt.user.api.*;
import pt.tecnico.ulisboa.hds.hdlt.user.error.UserRuntimeException;
import pt.tecnico.ulisboa.hds.hdlt.user.location.GridManager;
import pt.tecnico.ulisboa.hds.hdlt.user.session.Session;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class UserToServerTests extends BaseTests {

  private static final Integer nUsers = 10;
  private static final Integer nByzantineUsers = 2;
  private static final Integer uMaxDistance = 5;
  private static final Integer expSec = 60;
  private static final Map<String, GridManager> grids = new HashMap<>();
  private static final Map<String, UserToDHServerFrontend> dhFrontends = new HashMap<>();
  private static final Map<String, UserToUserFrontend> uuFrontends = new HashMap<>();
  private static final Map<String, UserToServerFrontend> usFrontends = new HashMap<>();
  private static final Map<String, Server> uServers = new HashMap<>();

  @BeforeAll
  public static void oneTimeSetUp() throws IOException {
    for (int i = 1; i <= nUsers; i++) {
      String uname = String.format("User%d", i);
      Map<String, String> uURLs = UserApp.parseUsersURLs(myUsersURLsPath);
      int uPort = UserApp.parsePort(uURLs.get(uname).split(":")[1]);
      GridManager grid = new GridManager(uname, myGridPath);
      Session session = new Session(expSec);
      String uPrivKeyPath = String.format("%s/%s.der", myUserPrivKeyDirPath, uname);
      UserCrypto uCrypto =
          new UserCrypto(myServerPubKey, uPrivKeyPath, myUserPubKeyDirPath, session);
      UserToDHServerFrontend dhFrontend =
          new UserToDHServerFrontend(uname, uCrypto, myServerHost, myServerPort, session);
      UserToUserFrontend uuFrontend =
          new UserToUserFrontend(uname, grid, uCrypto, nByzantineUsers, uMaxDistance, uURLs);
      UserToServerFrontend usFrontend =
          new UserToServerFrontend(
              uname, grid, uCrypto, dhFrontend, myServerHost, myServerPort, session);
      Server uServer =
          ServerBuilder.forPort(uPort)
              .addService(new UserUserServicesImpl(uname, grid, uCrypto, uMaxDistance))
              .build();
      uServer.start();
      grids.put(uname, grid);
      uServers.put(uname, uServer);
      dhFrontends.put(uname, dhFrontend);
      uuFrontends.put(uname, uuFrontend);
      usFrontends.put(uname, usFrontend);
    }
  }

  @AfterAll
  public static void oneTimeTearDown() {
    uServers.values().forEach(Server::shutdown);
    dhFrontends.values().forEach(UserToDHServerFrontend::shutdown);
    uuFrontends.values().forEach(UserToUserFrontend::shutdown);
    usFrontends.values().forEach(UserToServerFrontend::shutdown);
  }

  @Test
  @Order(1)
  public void submitUL() {
    String uname = "User2";
    Integer epoch = 1;
    try {
      Map<String, byte[]> authProofs = uuFrontends.get(uname).getAuthProofs(epoch);
      if (authProofs != null) {
        usFrontends.get(uname).submitULReport(epoch, authProofs);
        return;
      }
    } catch (UserRuntimeException e) {
      fail(e.getMessage());
    }
    fail("AuthProofs are null!");
  }

  @Test
  @Order(2)
  public void submitULThatAlreadyExists() {
    String uname = "User2";
    Integer epoch = 1;
    try {
      Map<String, byte[]> authProofs = uuFrontends.get(uname).getAuthProofs(epoch);
      if (authProofs != null) {
        usFrontends.get(uname).submitULReport(epoch, authProofs);
        fail("Submit was successful!");
        return;
      }
    } catch (UserRuntimeException e) {
      assertEquals("ABORTED: Report Already Exists!", e.getMessage());
      return;
    }
    fail("AuthProofs are null!");
  }

  @Test
  @Order(3)
  public void obtainUL() {
    String uname = "User2";
    Integer epoch = 1;
    assertEquals(
        usFrontends.get(uname).obtainUL(epoch), grids.get(uname).getLocation(uname, epoch));
  }

  @Test
  @Order(4)
  public void obtainULNotPresent() {
    String uname = "User3";
    Integer epoch = 1;
    UserRuntimeException exception =
        assertThrows(UserRuntimeException.class, () -> usFrontends.get(uname).obtainUL(epoch));
    assertEquals("ABORTED: No Report Found!", exception.getMessage());
  }
}
