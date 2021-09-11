package pt.tecnico.ulisboa.hds.hdlt.byzantine.api;

import com.google.common.primitives.Bytes;
import com.google.protobuf.ByteString;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import pt.tecnico.ulisboa.hds.hdlt.contract.uu.UserUserServicesGrpc;
import pt.tecnico.ulisboa.hds.hdlt.contract.uu.UserUserServicesGrpc.UserUserServicesBlockingStub;
import pt.tecnico.ulisboa.hds.hdlt.contract.uu.UserUserServicesOuterClass.Header;
import pt.tecnico.ulisboa.hds.hdlt.contract.uu.UserUserServicesOuterClass.Proof;
import pt.tecnico.ulisboa.hds.hdlt.contract.uu.UserUserServicesOuterClass.RequestULProofReq;
import pt.tecnico.ulisboa.hds.hdlt.lib.crypto.Crypto;
import pt.tecnico.ulisboa.hds.hdlt.user.api.RequestULStreamObserver;
import pt.tecnico.ulisboa.hds.hdlt.user.api.SyncProofs;
import pt.tecnico.ulisboa.hds.hdlt.user.api.UserCrypto;
import pt.tecnico.ulisboa.hds.hdlt.user.location.GridManager;
import pt.tecnico.ulisboa.hds.hdlt.user.location.Location;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ByzantineUserToUserFrontend {

  private final String uname;
  private final GridManager grid;
  private final UserCrypto uCrypto;
  private final Integer nByzantineUsers;
  private final Integer uMaxDistance;
  private final Map<String, UserUserServicesGrpc.UserUserServicesStub> asyncStubs = new HashMap<>();
  private final Map<String, ManagedChannel> channels = new HashMap<>();

  public ByzantineUserToUserFrontend(
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

  public Map<String, byte[]> getAuthProofsImpersonation(Integer epoch) {
    List<String> closeUsersUnames = this.grid.getCloseUsers(epoch, this.uMaxDistance);
    Location location = this.grid.getLocation(this.uname, epoch);
    byte[] proof =
        Proof.newBuilder()
            .setUname(this.uname)
            .setEpoch(epoch)
            .setX(location.getX())
            .setY(location.getY())
            .build()
            .toByteArray();

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

    SyncProofs syncProofs = new SyncProofs();
    for (String closeUsersUname : closeUsersUnames) {
      this.asyncStubs
          .get(closeUsersUname)
          .requestULProof(
              req,
              new RequestULStreamObserver(this.uCrypto, closeUsersUname, syncProofs, proof, nonce));
    }

    synchronized (syncProofs) {
      while (syncProofs.getNrOfReplies() != closeUsersUnames.size()) {
        try {
          syncProofs.wait();
        } catch (InterruptedException ignored) {
        }
      }
      return new HashMap<>(syncProofs.getAuthProofs());
    }
  }

  public Map<String, byte[]> getSelfGeneratedAuthProofs(Integer epoch) {
    Location location = this.grid.getLocation(this.uname, epoch);
    byte[] proof =
        Proof.newBuilder()
            .setUname(this.uname)
            .setEpoch(epoch)
            .setX(location.getX())
            .setY(location.getY())
            .build()
            .toByteArray();

    System.out.println("Forging Proof (Self Sign)");
    // Make Valid Proof Myself
    Map<String, byte[]> authProofs = new HashMap<>();
    byte[] signedProof = this.uCrypto.signPayload(proof);
    for (int i = 1; i <= this.nByzantineUsers; i++) {
      authProofs.put(String.format("User%d", i), signedProof);
    }
    return authProofs;
  }

  public Map<String, byte[]> getReplicatedAuthProofs(Integer epoch, String uname) {
    Location location = this.grid.getLocation(this.uname, epoch);
    byte[] proof =
        Proof.newBuilder()
            .setUname(this.uname)
            .setEpoch(epoch)
            .setX(location.getX())
            .setY(location.getY())
            .build()
            .toByteArray();

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

    SyncProofs syncProofs = new SyncProofs();
    this.asyncStubs
        .get(uname)
        .requestULProof(
            req, new RequestULStreamObserver(this.uCrypto, uname, syncProofs, proof, nonce));

    synchronized (syncProofs) {
      while (syncProofs.getNrOfReplies() != 1) {
        try {
          syncProofs.wait();
        } catch (InterruptedException ignored) {
        }
      }
      if (syncProofs.getNrOfValidReplies() > 0) {
        System.out.println("Replicating Valid Proofs");
        // Replicate Valid Replies (Possibly Made By Other Byzantine Users)
        byte[] signedProof = syncProofs.getAuthProofs().values().iterator().next();
        for (int i = 1; i <= this.nByzantineUsers; i++) {
          if (!syncProofs.getAuthProofs().containsKey(String.format("User%d", i))) {
            syncProofs.getAuthProofs().put(String.format("User%d", i), signedProof);
          }
        }
        return new HashMap<>(syncProofs.getAuthProofs());
      }
      return null;
    }
  }

  public String getAuthProofsInvalidSignature(Integer epoch, String uURL) {
    ManagedChannel channel = ManagedChannelBuilder.forTarget(uURL).usePlaintext().build();
    UserUserServicesBlockingStub stub = UserUserServicesGrpc.newBlockingStub(channel);

    BigInteger nonce = Crypto.generateRandomNonce();
    Header header =
        Header.newBuilder()
            .setUname(this.uname)
            .setEpoch(epoch)
            .setNonce(ByteString.copyFrom(nonce.toByteArray()))
            .build();
    byte[] signature = this.uCrypto.signPayload(header.toByteArray());
    // Invalid Signature (Added Header 2 Times)
    RequestULProofReq req =
        RequestULProofReq.newBuilder()
            .setHeader(header)
            .setSignature(ByteString.copyFrom(Bytes.concat(signature, header.toByteArray())))
            .build();
    try {
      stub.requestULProof(req);
    } catch (StatusRuntimeException e) {
      channel.shutdown();
      return e.getMessage();
    }
    channel.shutdown();
    return "Should Not Happen!";
  }

  public String getAuthProofsFromFarAwayUser(Integer epoch, String uURL) {
    ManagedChannel channel = ManagedChannelBuilder.forTarget(uURL).usePlaintext().build();
    UserUserServicesBlockingStub stub = UserUserServicesGrpc.newBlockingStub(channel);

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
    try {
      stub.requestULProof(req);
    } catch (StatusRuntimeException e) {
      channel.shutdown();
      return e.getMessage();
    }
    channel.shutdown();
    return "Should Not Happen!";
  }

  public void getMalformedExceptionAuthProofs(Integer epoch, String uURL, String uname) {
    ManagedChannel channel = ManagedChannelBuilder.forTarget(uURL).usePlaintext().build();
    UserUserServicesBlockingStub stub = UserUserServicesGrpc.newBlockingStub(channel);

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
    try {
      stub.requestULProof(req);
    } catch (StatusRuntimeException e) {
      channel.shutdown();
      this.uCrypto.checkErrorAuth(uname, e, nonce);
    }
    channel.shutdown();
  }

  public void shutdown() {
    this.channels.values().forEach(ManagedChannel::shutdown);
  }
}
