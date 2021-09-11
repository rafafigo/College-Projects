package pt.tecnico.ulisboa.hds.hdlt.server.repository;

import java.math.BigInteger;
import java.util.HashSet;
import java.util.Set;

public class InMemoryDB {

  private static final Set<BigInteger> nonces = new HashSet<>();

  public static boolean addNonce(DBManager db, BigInteger nonce) {
    synchronized (nonces) {
      boolean isAdded = nonces.add(nonce);
      if (isAdded) db.addNonce(nonce);
      return isAdded;
    }
  }

  public static void addAllNonces(Set<BigInteger> nonces) {
    InMemoryDB.nonces.addAll(nonces);
  }
}
