package ist.sirs.mtr.crypto;

import java.math.BigInteger;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class CryptoManager {

  private static final int MILLIS_FRESH = 30 * 1000;
  private static final ConcurrentMap<BigInteger, Long> nonces = new ConcurrentHashMap<>();

  public static boolean isFresh(Map.Entry<BigInteger, Long> freshness) {
    if (System.currentTimeMillis() - freshness.getValue() >= MILLIS_FRESH) return false;
    synchronized (nonces) {
      if (nonces.containsKey(freshness.getKey())) return false;
      nonces.put(freshness.getKey(), freshness.getValue());
    }
    return true;
  }

  public static void delOldNonces() {
    synchronized (nonces) {
      for (Map.Entry<BigInteger, Long> e : nonces.entrySet()) {
        if (System.currentTimeMillis() - e.getValue() >= MILLIS_FRESH) nonces.remove(e.getKey());
      }
    }
  }
}
