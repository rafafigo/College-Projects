package pt.tecnico.ulisboa.hds.hdlt.user.byzantine;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import pt.tecnico.ulisboa.hds.hdlt.user.UserApp;

import java.io.IOException;
import java.util.Properties;

/* User's Vicinity on Grid
User1: ['User2', 'User3', 'User4', 'User6', 'User7', 'User10']
User2: ['User1', 'User4', 'User7']
User3: ['User1', 'User5', 'User6', 'User8', 'User10']
User4: ['User1', 'User2', 'User6', 'User7']
User5: ['User3', 'User8', 'User9', 'User10']
User6: ['User1', 'User3', 'User4', 'User10']
User7: ['User1', 'User2', 'User4']
User8: ['User3', 'User5', 'User9']
User9: ['User5', 'User8']
User10: ['User1', 'User3', 'User5', 'User6']
*/

public class BaseTests {

  private static final String TEST_PROP_FILE = "/test.properties";
  protected static String myServerHost;
  protected static Integer myServerPort;
  protected static String myServerPrivKey;
  protected static String myServerPubKey;
  protected static String myHaPubKey;
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
    myServerPrivKey = testProps.getProperty("myServerPrivKey");
    myServerPubKey = testProps.getProperty("myServerPubKey");
    myHaPubKey = testProps.getProperty("myHAPubKey");
    myUserPrivKeyDirPath = testProps.getProperty("myUserPrivKeyDirPath");
    myUserPubKeyDirPath = testProps.getProperty("myUserPubKeyDirPath");
    myGridPath = testProps.getProperty("myGridPath");
    myUsersURLsPath = testProps.getProperty("myUsersURLsPath");
    myEpochLifeTime = Integer.parseInt(testProps.getProperty("myEpochLifeTime"));
  }

  @AfterAll
  public static void cleanup() {}
}
