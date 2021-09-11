package pt.tecnico.ulisboa.hds.hdlt.user;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;

import java.io.IOException;
import java.util.Properties;

public class BaseTests {

  private static final String TEST_PROP_FILE = "/test.properties";
  protected static String myServerHost;
  protected static Integer myServerPort;
  protected static String myServerPubKey;
  protected static String myUserPrivKeyDirPath;
  protected static String myUserPubKeyDirPath;
  protected static String myGridPath;
  protected static String myUsersURLsPath;
  protected static Integer myEpochLifeTime;

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
    myServerHost = testProps.getProperty("myServerHost");
    myServerPort = UserApp.parsePort(testProps.getProperty("myServerPort"));
    myServerPubKey = testProps.getProperty("myServerPubKey");
    myUserPrivKeyDirPath = testProps.getProperty("myUserPrivKeyDirPath");
    myUserPubKeyDirPath = testProps.getProperty("myUserPubKeyDirPath");
    myGridPath = testProps.getProperty("myGridPath");
    myUsersURLsPath = testProps.getProperty("myUsersURLsPath");
    myEpochLifeTime = Integer.parseInt(testProps.getProperty("myEpochLifeTime"));
  }

  @AfterAll
  public static void cleanup() {}
}
