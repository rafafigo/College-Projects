package pt.tecnico.ulisboa.hds.hdlt.server.repository;

import org.intellij.lang.annotations.Language;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class DBPopulate {

  @Language("SQL")
  public static final String InsertNonceSQL = "INSERT INTO UserNonces (nonce) VALUES (?);";

  @Language("SQL")
  public static final String InsertUserReportSQL =
      "INSERT INTO UserReports (uname, epoch, x, y) VALUES (?, ?, ?, ?);";

  @Language("SQL")
  public static final String SelectNoncesSQL = "SELECT * FROM UserNonces;";

  @Language("SQL")
  public static final String SelectUserReportByEpochSQL =
      "SELECT * FROM UserReports WHERE uname = ? AND epoch = ?;";

  @Language("SQL")
  public static final String SelectUserReportsByEpochAndLocationSQL =
      "SELECT uname FROM UserReports WHERE epoch = ? AND x = ? AND y = ?;";

  public static void createTables(Connection connection) throws SQLException {
    Statement stmt = connection.createStatement();
    stmt.executeUpdate(
        "CREATE TABLE IF NOT EXISTS UserNonces("
            + "nonce BYTEA NOT NULL UNIQUE,"
            + "PRIMARY KEY (nonce));");
    stmt.executeUpdate(
        "CREATE TABLE IF NOT EXISTS UserReports("
            + "uname VARCHAR(20) NOT NULL,"
            + "epoch INTEGER NOT NULL,"
            + "x INTEGER NOT NULL,"
            + "y INTEGER NOT NULL,"
            + "PRIMARY KEY (uname, epoch));");
    stmt.close();
  }
}
