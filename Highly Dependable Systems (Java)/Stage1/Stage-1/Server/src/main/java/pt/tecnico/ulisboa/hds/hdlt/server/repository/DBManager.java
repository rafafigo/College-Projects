package pt.tecnico.ulisboa.hds.hdlt.server.repository;

import io.grpc.Status;
import org.postgresql.util.PSQLException;
import pt.tecnico.ulisboa.hds.hdlt.lib.error.AssertError;
import pt.tecnico.ulisboa.hds.hdlt.server.api.ServerCrypto;
import pt.tecnico.ulisboa.hds.hdlt.server.error.ServerStatusRuntimeException;
import pt.tecnico.ulisboa.hds.hdlt.server.location.Location;

import java.math.BigInteger;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.postgresql.util.PSQLState.UNIQUE_VIOLATION;

public class DBManager {
  private final Connection connection;
  private final ServerCrypto serverCrypto;

  public DBManager(String dbName, String dbUser, String dbPwd, ServerCrypto serverCrypto) {
    this.serverCrypto = serverCrypto;
    String url = String.format("jdbc:postgresql://localhost:5432/%s", dbName);
    try {
      this.connection = DriverManager.getConnection(url, dbUser, dbPwd);
      DBPopulate.createTables(this.connection);
      InMemoryDB.addAllNonces(this.getNonces());
    } catch (SQLException e) {
      throw new AssertError(DBManager.class.getSimpleName(), "Constructor", e);
    }
  }

  public void addNonce(BigInteger nonce) {
    try (PreparedStatement stmt = this.connection.prepareStatement(DBPopulate.InsertNonceSQL)) {
      stmt.setBytes(1, nonce.toByteArray());
      stmt.executeUpdate();
    } catch (PSQLException e) {
      if (e.getSQLState().equals(UNIQUE_VIOLATION.getState()))
        throw new ServerStatusRuntimeException(
            this.serverCrypto, Status.ABORTED, "Nonce Already Exists!", nonce);
      else throw new AssertError(DBManager.class.getSimpleName(), "addNonce", e);
    } catch (SQLException e) {
      throw new AssertError(DBManager.class.getSimpleName(), "addNonce", e);
    }
  }

  public void addUserLocation(String uname, int epoch, Location location, BigInteger nonce) {
    try (PreparedStatement stmt =
        this.connection.prepareStatement(DBPopulate.InsertUserReportSQL)) {
      stmt.setString(1, uname);
      stmt.setInt(2, epoch);
      stmt.setInt(3, location.getX());
      stmt.setInt(4, location.getY());
      stmt.executeUpdate();
    } catch (PSQLException e) {
      if (e.getSQLState().equals(UNIQUE_VIOLATION.getState()))
        throw new ServerStatusRuntimeException(
            this.serverCrypto, Status.ABORTED, "Report Already Exists!", nonce);
      else throw new AssertError(DBManager.class.getSimpleName(), "addUserLocation", e);
    } catch (SQLException e) {
      throw new AssertError(DBManager.class.getSimpleName(), "addUserLocation", e);
    }
  }

  public Set<BigInteger> getNonces() {
    Set<BigInteger> nonces = new HashSet<>();
    try (PreparedStatement stmt = this.connection.prepareStatement(DBPopulate.SelectNoncesSQL)) {
      ResultSet rs = stmt.executeQuery();
      while (rs.next()) {
        nonces.add(new BigInteger(1, rs.getBytes("nonce")));
      }
    } catch (SQLException e) {
      throw new AssertError(DBManager.class.getSimpleName(), "getNonces", e);
    }
    return nonces;
  }

  public Location getUserLocation(String uname, int epoch, BigInteger nonce) {
    try (PreparedStatement stmt =
        this.connection.prepareStatement(DBPopulate.SelectUserReportByEpochSQL)) {
      stmt.setString(1, uname);
      stmt.setInt(2, epoch);
      ResultSet rs = stmt.executeQuery();
      if (!rs.next())
        throw new ServerStatusRuntimeException(
            this.serverCrypto, Status.ABORTED, "No Report Found!", nonce);
      return new Location(rs.getInt("x"), rs.getInt("y"));
    } catch (SQLException e) {
      throw new AssertError(DBManager.class.getSimpleName(), "getUserLocation", e);
    }
  }

  public List<String> getUsersAtLocation(int epoch, Location location) {
    List<String> unames = new ArrayList<>();
    try (PreparedStatement stmt =
        this.connection.prepareStatement(DBPopulate.SelectUserReportsByEpochAndLocationSQL)) {
      stmt.setInt(1, epoch);
      stmt.setInt(2, location.getX());
      stmt.setInt(3, location.getY());
      ResultSet rs = stmt.executeQuery();
      while (rs.next()) {
        unames.add(rs.getString("uname"));
      }
    } catch (SQLException e) {
      throw new AssertError(DBManager.class.getSimpleName(), "getUsersAtLocation", e);
    }
    return unames;
  }

  public void closeConnection() {
    try {
      this.connection.close();
    } catch (SQLException e) {
      throw new AssertError(DBManager.class.getSimpleName(), "closeConn", e);
    }
  }
}
