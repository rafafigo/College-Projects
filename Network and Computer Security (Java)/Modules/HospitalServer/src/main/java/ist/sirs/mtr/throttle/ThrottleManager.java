package ist.sirs.mtr.throttle;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class ThrottleManager {

  private static final ConcurrentMap<String, Integer> attempts = new ConcurrentHashMap<>();
  private static final int maxAttempts = 3;

  public static void unsuccessful(String uname) {
    Integer att = attempts.getOrDefault(uname, 0);
    attempts.put(uname, att + 1);
  }

  public static void successful(String uname) {
    attempts.remove(uname);
  }

  public static boolean getThrottle(String uname) {
    Integer att = attempts.getOrDefault(uname, 0);
    return att >= maxAttempts;
  }

  public static void delAttempts() {
    attempts.clear();
  }
}
