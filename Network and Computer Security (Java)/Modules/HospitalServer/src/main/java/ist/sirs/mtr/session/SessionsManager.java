package ist.sirs.mtr.session;

import ist.sirs.mtr.crypto.Crypto;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class SessionsManager {

  private static final ConcurrentMap<String, Session> sessions = new ConcurrentHashMap<>();

  private static final int tokSize = 256;
  private static final SecureRandom secRandom = new SecureRandom();
  private static final Base64.Encoder encB64 = Base64.getEncoder();

  public static String newSession(String uname, String role) {

    String tok;
    byte[] tokBytes = new byte[tokSize];

    while (true) {
      secRandom.nextBytes(tokBytes);
      tok = encB64.encodeToString(tokBytes);
      synchronized (sessions) {
        if (!sessions.containsKey(tok)) {
          sessions.put(tok, new Session(uname, role));
          return tok;
        }
      }
    }
  }

  public static Session getSession(String tok, boolean isActive) {

    Session session = sessions.getOrDefault(tok, null);

    if (session != null) {
      if (session.isAuthentic()) {
        if (isActive && !session.isActive()) return null;
        session.renew();
        return session;
      }
      delSession(tok);
    }
    return null;
  }

  public static void delInactiveSessions() {
    for (Map.Entry<String, Session> e : sessions.entrySet()) {
      if (!e.getValue().isAuthentic()) delSession(e.getKey());
    }
  }

  public static void delSession(String tok) {
    Crypto.removeCryptoRSAKeys(tok);
    Crypto.removeCryptoAESKeys(tok);
    sessions.remove(tok);
  }
}
