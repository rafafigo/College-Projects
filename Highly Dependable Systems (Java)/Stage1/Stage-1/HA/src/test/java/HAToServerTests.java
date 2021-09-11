import com.google.protobuf.ProtocolStringList;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import pt.tecnico.ulisboa.hds.hdlt.ha.api.HACrypto;
import pt.tecnico.ulisboa.hds.hdlt.ha.api.HAToDHServerFrontend;
import pt.tecnico.ulisboa.hds.hdlt.ha.api.HAToServerFrontend;
import pt.tecnico.ulisboa.hds.hdlt.ha.error.HARuntimeException;
import pt.tecnico.ulisboa.hds.hdlt.ha.session.Session;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static pt.tecnico.ulisboa.hds.hdlt.ha.api.HAToServerFrontend.parseLocation;
import static pt.tecnico.ulisboa.hds.hdlt.ha.api.HAToServerFrontend.parseUnames;

public class HAToServerTests extends BaseTests {
  private static final Integer expSec = 60;
  protected static Map<String, Map<Integer, Integer[]>> grid = new HashMap<>();
  protected static Map<Integer, Map<Integer[], List<String>>> gridLocations = new HashMap<>();
  private static Session session;
  private static HACrypto crypto;
  private static HAToServerFrontend hsFrontend;
  private static HAToDHServerFrontend dhFrontend;

  @BeforeAll
  public static void oneTimeSetUp() {
    session = new Session(expSec);
    crypto = new HACrypto(myHAPrivKey, myServerPubKey, session);
    dhFrontend = new HAToDHServerFrontend(myUsername, crypto, myServerHost, myServerPort, session);
    hsFrontend =
        new HAToServerFrontend(myUsername, crypto, dhFrontend, myServerHost, myServerPort, session);
    dhFrontend.dH();
    createGrid();
  }

  @AfterAll
  public static void cleanup() {
    dhFrontend.shutdown();
    hsFrontend.shutdown();
  }

  private static void createGrid() {
    String[] userPosition;
    Map<Integer, Integer[]> epochGrid;
    Scanner scanner;
    try {
      scanner = new Scanner(new File(myGridPath));
    } catch (FileNotFoundException e) {
      return;
    }
    while (scanner.hasNextLine()) {
      userPosition = scanner.nextLine().split(";");
      epochGrid = grid.getOrDefault(userPosition[0].trim(), null);
      if (epochGrid == null) {
        epochGrid = new HashMap<>();
        grid.put(userPosition[0].trim(), epochGrid);
      }
      epochGrid.put(
          Integer.parseInt(userPosition[1].trim()),
          new Integer[] {
            Integer.parseInt(userPosition[2].trim()), Integer.parseInt(userPosition[3].trim())
          });
    }
    scanner.close();
  }

  @Test
  public void obtainULWithInvalidSession() {
    String uname = "User1";
    Integer epoch = 1;

    Session oldSession = session;
    session = new Session(expSec);
    crypto.setSession(session);
    dhFrontend.setSession(session);

    dhFrontend.dH();
    crypto.setSession(oldSession);
    dhFrontend.setSession(oldSession);
    hsFrontend.setSession(oldSession);

    HARuntimeException exception =
        assertThrows(
            HARuntimeException.class,
            () -> {
              hsFrontend.obtainUL(uname, epoch);
            });

    assertEquals("INVALID_ARGUMENT: Invalid Session!", exception.getMessage());
    crypto.setSession(session);
    dhFrontend.setSession(session);
    hsFrontend.setSession(session);
  }

  @Test
  public void obtainUAtLWithInvalidSession() {
    Integer epoch = 1;
    Integer x = 0;
    Integer y = 0;

    dhFrontend.dH();
    Session oldSession = session;
    session = new Session(expSec);
    dhFrontend.setSession(session);

    dhFrontend.dH();
    crypto.setSession(oldSession);
    dhFrontend.setSession(oldSession);
    hsFrontend.setSession(oldSession);

    HARuntimeException exception =
        assertThrows(
            HARuntimeException.class,
            () -> {
              hsFrontend.obtainUAtL(epoch, x, y);
            });

    assertEquals("INVALID_ARGUMENT: Invalid Session!", exception.getMessage());
    crypto.setSession(session);
    dhFrontend.setSession(session);
    hsFrontend.setSession(session);
  }

  @Test
  public void obtainUL() {
    for (String uname : grid.keySet()) {
      for (Integer epoch : grid.get(uname).keySet()) {
        Integer[] location = grid.get(uname).get(epoch);
        assertEquals(
            parseLocation(uname, location[0], location[1], epoch),
            hsFrontend.obtainUL(uname, epoch));
        if (gridLocations.containsKey(epoch)) gridLocations.put(epoch, new HashMap<>());
        if (gridLocations.get(epoch).containsKey(location))
          gridLocations.get(epoch).put(location, new ArrayList<>());
        gridLocations.get(epoch).get(location).add(uname);
      }
    }
  }

  @Test
  public void obtainULNotPresent() {
    HARuntimeException exception =
        assertThrows(HARuntimeException.class, () -> hsFrontend.obtainUL("NonExistent", 0));
    assertEquals("ABORTED: No Report Found!", exception.getMessage());
  }

  @Test
  public void obtainUAtL() {
    for (Integer epoch : gridLocations.keySet()) {
      for (Integer[] location : gridLocations.get(epoch).keySet()) {
        assertEquals(
            parseUnames(
                location[0],
                location[1],
                epoch,
                (ProtocolStringList) gridLocations.get(epoch).get(location)),
            hsFrontend.obtainUAtL(location[0], location[1], epoch));
      }
    }
  }
}
