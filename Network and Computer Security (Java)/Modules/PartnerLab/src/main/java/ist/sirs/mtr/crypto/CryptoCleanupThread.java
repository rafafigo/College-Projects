package ist.sirs.mtr.crypto;

public class CryptoCleanupThread implements Runnable {

  private static final int sleepMillis = 30 * 1000;
  private static boolean stopped = false;

  public static void stop(Thread thread) {
    stopped = true;
    thread.interrupt();
  }

  @Override
  public void run() {
    while (!stopped) {
      CryptoManager.delOldNonces();
      try {
        Thread.sleep(sleepMillis);
      } catch (InterruptedException ignored) {
      }
    }
  }
}
