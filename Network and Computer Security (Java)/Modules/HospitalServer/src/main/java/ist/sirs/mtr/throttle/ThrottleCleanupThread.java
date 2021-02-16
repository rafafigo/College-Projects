package ist.sirs.mtr.throttle;

public class ThrottleCleanupThread implements Runnable {

  private static final int sleepMillis = 5 * 60 * 1000;
  private static boolean stopped = false;

  public static void stop(Thread thread) {
    ThrottleCleanupThread.stopped = true;
    thread.interrupt();
  }

  @Override
  public void run() {
    while (!stopped) {
      ThrottleManager.delAttempts();
      try {
        Thread.sleep(sleepMillis);
      } catch (InterruptedException ignored) {
      }
    }
  }
}
