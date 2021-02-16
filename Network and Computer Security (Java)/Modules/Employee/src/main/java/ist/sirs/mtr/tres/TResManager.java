package ist.sirs.mtr.tres;

import ist.sirs.mtr.api.Message;
import ist.sirs.mtr.crypto.Crypto;
import ist.sirs.mtr.error.AssertError;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class TResManager {

  private static final Map<TResKey, TRes> tResMap = new HashMap<>();

  public static void addTRes(int pid, int recId, String content, long millis) {
    tResMap.put(new TResKey(pid, recId), new TRes(content, millis));
  }

  public static boolean checkTResAuth(int pid, int recId, byte[] labSig, byte[] labCrt) {
    TRes tRes = tResMap.getOrDefault(new TResKey(pid, recId), null);
    if (tRes == null) throw new AssertError(Message.TEST_RESULT_NOT_FOUND.lbl);
    Crypto.addCrt("PL", labCrt);
    byte[] tResCmp = Crypto.decipherBytesRSAPub("PL", labSig);
    byte[] tResHash = Crypto.hash(String.format("%d%s%d", pid, tRes.getContent(), tRes.getTs()));
    return Arrays.equals(tResHash, tResCmp);
  }
}
