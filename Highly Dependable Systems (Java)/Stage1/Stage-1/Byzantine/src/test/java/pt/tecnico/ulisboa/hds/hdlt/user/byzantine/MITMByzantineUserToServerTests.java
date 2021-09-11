package pt.tecnico.ulisboa.hds.hdlt.user.byzantine;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import pt.tecnico.ulisboa.hds.hdlt.byzantine.api.ByzantineDHServicesImpl;
import pt.tecnico.ulisboa.hds.hdlt.byzantine.api.ByzantineUserServerServicesImpl;
import pt.tecnico.ulisboa.hds.hdlt.server.api.ServerCrypto;
import pt.tecnico.ulisboa.hds.hdlt.server.error.ExceptionHandler;
import pt.tecnico.ulisboa.hds.hdlt.user.api.UserCrypto;
import pt.tecnico.ulisboa.hds.hdlt.user.api.UserToDHServerFrontend;
import pt.tecnico.ulisboa.hds.hdlt.user.api.UserToServerFrontend;
import pt.tecnico.ulisboa.hds.hdlt.user.error.UserRuntimeException;
import pt.tecnico.ulisboa.hds.hdlt.user.location.GridManager;
import pt.tecnico.ulisboa.hds.hdlt.user.session.Session;

import java.io.IOException;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static pt.tecnico.ulisboa.hds.hdlt.byzantine.api.ByzantineUserServerServicesImpl.*;
import static pt.tecnico.ulisboa.hds.hdlt.lib.crypto.Crypto.sleep;

public class MITMByzantineUserToServerTests extends BaseTests {
  private static final String uname = "User1";
  private static final Integer expSec = 60;
  private static Integer epoch;
  private static UserToDHServerFrontend dhFrontend;
  private static UserToServerFrontend usFrontend;
  private static ByzantineUserServerServicesImpl bsImpl;
  private static Server usServer;

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
    ServerCrypto sCrypto = new ServerCrypto(myServerPrivKey, myUserPubKeyDirPath, myHaPubKey);
    dhFrontend = new UserToDHServerFrontend(uname, uCrypto, myServerHost, 8090, session);
    usFrontend =
        new UserToServerFrontend(uname, grid, uCrypto, dhFrontend, myServerHost, 8090, session);

    bsImpl = new ByzantineUserServerServicesImpl(sCrypto);
    usServer =
        ServerBuilder.forPort(8090)
            .addService(bsImpl)
            .addService(new ByzantineDHServicesImpl(sCrypto, expSec))
            .intercept(new ExceptionHandler())
            .build();
    usServer.start();
  }

  @AfterAll
  public static void oneTimeTearDown() {
    dhFrontend.shutdown();
    usFrontend.shutdown();
    usServer.shutdown();
  }

  @Test
  public void submitULReportWhereServerReject() {
    bsImpl.setMode(UNSIGNED_EXCEPTION);
    new Thread(
            () -> {
              sleep(50);
              bsImpl.setMode(SIGN_EXCEPTION);
            })
        .start();
    UserRuntimeException exception =
        assertThrows(
            UserRuntimeException.class, () -> usFrontend.submitULReport(epoch, new HashMap<>()));

    // This User Retries its Requests
    // Because a Man In the Middle is Rejecting a Request
    assertEquals("UNKNOWN: Exception Signed!", exception.getMessage());
  }

  @Test
  public void submitULReportDoesNotAnswer() {
    bsImpl.setMode(NEVER_SIGN_EXCEPTION);
    new Thread(
            () -> {
              sleep(2500);
              bsImpl.setMode(SIGN_EXCEPTION);
            })
        .start();
    UserRuntimeException exception =
        assertThrows(
            UserRuntimeException.class, () -> usFrontend.submitULReport(epoch, new HashMap<>()));

    // This User Retries its Requests
    // Because a Man In the Middle is Dropping a Request
    assertEquals("UNKNOWN: Exception Signed!", exception.getMessage());
  }

  @Test
  public void obtainULWhereServerReject() {
    bsImpl.setMode(UNSIGNED_EXCEPTION);
    new Thread(
            () -> {
              sleep(50);
              bsImpl.setMode(SIGN_EXCEPTION);
            })
        .start();
    UserRuntimeException exception =
        assertThrows(UserRuntimeException.class, () -> usFrontend.obtainUL(epoch));

    // This User Retries its Requests
    // Because a Man In the Middle is Rejecting a Request
    assertEquals("UNKNOWN: Exception Signed!", exception.getMessage());
  }

  @Test
  public void obtainULDoesNotAnswer() {
    bsImpl.setMode(NEVER_SIGN_EXCEPTION);
    new Thread(
            () -> {
              sleep(2500);
              bsImpl.setMode(SIGN_EXCEPTION);
            })
        .start();
    UserRuntimeException exception =
        assertThrows(
            UserRuntimeException.class, () -> usFrontend.submitULReport(epoch, new HashMap<>()));

    // This User Retries its Requests
    // Because a Man In the Middle is Dropping a Request
    assertEquals("UNKNOWN: Exception Signed!", exception.getMessage());
  }
}
