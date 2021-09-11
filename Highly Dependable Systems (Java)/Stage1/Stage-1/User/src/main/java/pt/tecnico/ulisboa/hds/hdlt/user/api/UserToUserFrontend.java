package pt.tecnico.ulisboa.hds.hdlt.user.api;

import com.google.protobuf.ByteString;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import pt.tecnico.ulisboa.hds.hdlt.contract.uu.UserUserServicesGrpc;
import pt.tecnico.ulisboa.hds.hdlt.contract.uu.UserUserServicesOuterClass.Header;
import pt.tecnico.ulisboa.hds.hdlt.contract.uu.UserUserServicesOuterClass.Proof;
import pt.tecnico.ulisboa.hds.hdlt.contract.uu.UserUserServicesOuterClass.RequestULProofReq;
import pt.tecnico.ulisboa.hds.hdlt.lib.crypto.Crypto;
import pt.tecnico.ulisboa.hds.hdlt.user.location.GridManager;
import pt.tecnico.ulisboa.hds.hdlt.user.location.Location;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static pt.tecnico.ulisboa.hds.hdlt.lib.crypto.Crypto.sleep;

public class UserToUserFrontend {

  private static final Integer TIMEOUT = 5;
  private final String uname;
  private final GridManager grid;
  private final UserCrypto uCrypto;
  private final Integer nByzantineUsers;
  private final Integer uMaxDistance;
  private final Map<String, UserUserServicesGrpc.UserUserServicesStub> asyncStubs = new HashMap<>();
  private final Map<String, ManagedChannel> channels = new HashMap<>();

  public UserToUserFrontend(
      String uname,
      GridManager grid,
      UserCrypto uCrypto,
      Integer nByzantineUsers,
      Integer uMaxDistance,
      Map<String, String> uURLs) {
    this.uname = uname;
    this.grid = grid;
    this.uCrypto = uCrypto;
    this.nByzantineUsers = nByzantineUsers;
    this.uMaxDistance = uMaxDistance;
    this.parseURLs(uURLs);
  }

  private void parseURLs(Map<String, String> uURLs) {
    ManagedChannel channel;
    for (Map.Entry<String, String> uURL : uURLs.entrySet()) {
      channel = ManagedChannelBuilder.forTarget(uURL.getValue()).usePlaintext().build();
      this.channels.put(uURL.getKey(), channel);
      this.asyncStubs.put(uURL.getKey(), UserUserServicesGrpc.newStub(channel));
    }
  }

  public Map<String, byte[]> getAuthProofs(Integer epoch) {
    List<String> closeUsersUnames = this.grid.getCloseUsers(epoch, this.uMaxDistance);
    if (closeUsersUnames.size() < this.nByzantineUsers) return null;

    Location location = this.grid.getLocation(this.uname, epoch);
    byte[] proof =
        Proof.newBuilder()
            .setUname(this.uname)
            .setEpoch(epoch)
            .setX(location.getX())
            .setY(location.getY())
            .build()
            .toByteArray();

    SyncProofs syncProofs = new SyncProofs();
    Map<String, byte[]> authProofs;
    do {
      for (String uname : syncProofs.getAuthProofs().keySet()) {
        closeUsersUnames.remove(uname);
      }
      syncProofs.resetNrOfReplies();
      authProofs = this.doGetAuthProofs(epoch, closeUsersUnames, proof, syncProofs);
    } while (authProofs == null);
    return authProofs;
  }

  public Map<String, byte[]> doGetAuthProofs(
      Integer epoch, List<String> closeUsersUnames, byte[] proof, SyncProofs syncProofs) {

    BigInteger nonce = Crypto.generateRandomNonce();
    Header header =
        Header.newBuilder()
            .setUname(this.uname)
            .setEpoch(epoch)
            .setNonce(ByteString.copyFrom(nonce.toByteArray()))
            .build();
    byte[] signature = this.uCrypto.signPayload(header.toByteArray());
    RequestULProofReq req =
        RequestULProofReq.newBuilder()
            .setHeader(header)
            .setSignature(ByteString.copyFrom(signature))
            .build();

    for (String closeUsersUname : closeUsersUnames) {
      this.asyncStubs
          .get(closeUsersUname)
          .withDeadlineAfter(TIMEOUT, TimeUnit.SECONDS)
          .requestULProof(
              req,
              new RequestULStreamObserver(this.uCrypto, closeUsersUname, syncProofs, proof, nonce));
    }
    synchronized (syncProofs) {
      while (syncProofs.getNrOfValidReplies() < this.nByzantineUsers) {
        // Missing Answers + Approved Answers < Nr of Byzantine Users
        if ((closeUsersUnames.size() - syncProofs.getNrOfReplies())
                + syncProofs.getNrOfValidReplies()
            < this.nByzantineUsers) {
          sleep(1000);
          return null;
        }
        try {
          syncProofs.wait();
        } catch (InterruptedException ignored) {
        }
      }
      return new HashMap<>(syncProofs.getAuthProofs());
    }
  }

  public void shutdown() {
    this.channels.values().forEach(ManagedChannel::shutdown);
  }
}
