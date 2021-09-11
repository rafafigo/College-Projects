import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import pt.tecnico.ulisboa.hds.hdlt.ha.HAApp;

import java.io.IOException;
import java.util.Properties;

public class BaseTests {

  private static final String TEST_PROP_FILE = "/test.properties";
  protected static String myUsername;
  protected static String myServerHost;
  protected static Integer myServerPort;
  protected static String myHAPrivKey;
  protected static String myServerPubKey;
  protected static String myGridPath;

  @BeforeAll
  public static void oneTimeSetup() throws IOException {
    Properties testProps = new Properties();
    try {
      testProps.load(BaseTests.class.getResourceAsStream(TEST_PROP_FILE));
    } catch (IOException e) {
      final String msg = String.format("Could not load properties file %s", TEST_PROP_FILE);
      System.out.println(msg);
      throw e;
    }
    System.out.println("Properties:");
    System.out.println(testProps);
    myUsername = testProps.getProperty("myUsername");
    myServerHost = testProps.getProperty("myServerHost");
    myServerPort = HAApp.parsePort(testProps.getProperty("myServerPort"));
    myHAPrivKey = testProps.getProperty("myHAPrivKey");
    myServerPubKey = testProps.getProperty("myServerPubKey");
    myGridPath = testProps.getProperty("myGridPath");
  }

  @AfterAll
  public static void cleanup() {}
}
