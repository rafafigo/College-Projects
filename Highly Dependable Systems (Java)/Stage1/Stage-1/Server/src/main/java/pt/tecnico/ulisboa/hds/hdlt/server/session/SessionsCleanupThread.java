package pt.tecnico.ulisboa.hds.hdlt.server.session;

import static pt.tecnico.ulisboa.hds.hdlt.lib.crypto.Crypto.sleep;

public class SessionsCleanupThread implements Runnable {

  private static final int sleepMillis = 5 * 60 * 1000;
  private static boolean stopped = false;

  public static void stop(Thread thread) {
    SessionsCleanupThread.stopped = true;
    thread.interrupt();
  }

  @Override
  public void run() {
    while (!stopped) {
      SessionsManager.delInactiveSessions();
      sleep(sleepMillis);
    }
  }
}
