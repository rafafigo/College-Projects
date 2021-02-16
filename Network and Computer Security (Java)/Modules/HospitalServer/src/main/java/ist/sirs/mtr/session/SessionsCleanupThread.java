package ist.sirs.mtr.session;

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
      try {
        Thread.sleep(sleepMillis);
      } catch (InterruptedException ignored) {
      }
    }
  }
}
