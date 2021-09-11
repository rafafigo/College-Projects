package pt.tecnico.ulisboa.hds.hdlt.server.session;

import javax.crypto.SecretKey;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class SessionsManager {

  private static final ConcurrentMap<String, Session> sessions = new ConcurrentHashMap<>();

  public static void newSession(String uname, int expSec, SecretKey secKey) {
    sessions.put(uname, new Session(expSec, secKey));
  }

  public static Session getSession(String uname) {

    Session session = sessions.getOrDefault(uname, null);

    if (session != null) {
      if (session.isValid()) {
        return session;
      }
      sessions.remove(uname);
    }
    return null;
  }

  public static void delInactiveSessions() {
    for (Map.Entry<String, Session> e : sessions.entrySet()) {
      if (!e.getValue().isValid()) sessions.remove(e.getKey());
    }
  }
}
