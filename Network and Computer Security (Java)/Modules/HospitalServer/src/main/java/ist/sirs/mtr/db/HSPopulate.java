package ist.sirs.mtr.db;

import ist.sirs.mtr.crypto.Crypto;
import ist.sirs.mtr.pwd.PwdBE;
import org.intellij.lang.annotations.Language;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Scanner;

public class HSPopulate {

  @Language("SQL")
  public static final String InsertPatientDetailsSQL =
      "INSERT INTO PatientDetails (nif, name) VALUES (?, ?);";

  @Language("SQL")
  public static final String InsertUserCredentialsSQL =
      "INSERT INTO UserCredentials (uname, pwdHash, pwdSalt, role) VALUES (?, ?, ?, ?);";

  @Language("SQL")
  public static final String InsertTestResultSQL =
      "INSERT INTO TestResultsRecords (idPatient, idRecord, content, date, labUname, labSig) VALUES (?, ?, ?, ?, ?, ?);";

  @Language("SQL")
  public static final String InsertHouseKeepingSQL =
      "INSERT INTO HousekeepingRecords (idPatient, idRecord, content, date) VALUES (?, ?, ?, ?);";

  @Language("SQL")
  public static final String InsertDietSQL =
      "INSERT INTO DietRecords (idPatient, idRecord, content, date) VALUES (?, ?, ?, ?);";

  @Language("SQL")
  public static final String InsertTransportsSQL =
      "INSERT INTO TransportsRecords (idPatient, idRecord, content, date) VALUES (?, ?, ?, ?);";

  @Language("SQL")
  public static final String InsertReceptionSQL =
      "INSERT INTO ReceptionRecords (idPatient, idRecord, content, date) VALUES (?, ?, ?, ?);";

  @Language("SQL")
  public static final String InsertPrescriptionsSQL =
      "INSERT INTO PrescriptionRecords (idPatient, idRecord, content, date) VALUES (?, ?, ?, ?);";

  @Language("SQL")
  public static final String InsertLabsSQL = "INSERT INTO Labs (labUname, labCrt) VALUES (?, ?);";

  @Language("SQL")
  public static final String SelectLabCrtSQL = "SELECT labCrt FROM Labs WHERE labUname = ?;";

  @Language("SQL")
  public static final String SelectPatientDetailsByIdPatientSQL =
      "SELECT * FROM PatientDetails WHERE idPatient = ?;";

  @Language("SQL")
  public static final String SelectPatientDetailsByNifSQL =
      "SELECT * FROM PatientDetails WHERE nif = ?;";

  @Language("SQL")
  public static final String SelectPatientDetailsByNameSQL =
      "SELECT * FROM PatientDetails WHERE name LIKE ?;";

  @Language("SQL")
  public static final String SelectEmployeeCredentialsSQL =
      "SELECT * FROM UserCredentials WHERE uname = ? AND role <> 'PartnerLab';";

  @Language("SQL")
  public static final String SelectLabCredentialsSQL =
      "SELECT * FROM UserCredentials WHERE uname = ? AND role = 'PartnerLab';";

  @Language("SQL")
  public static final String SelectTestResultSQL =
      "SELECT idRecord, content, date FROM TestResultsRecords WHERE idPatient = ? AND date >= ?;";

  @Language("SQL")
  public static final String SelectHouseKeepingSQL =
      "SELECT idRecord, content, date FROM HousekeepingRecords WHERE idPatient = ? AND date >= ?;";

  @Language("SQL")
  public static final String SelectDietSQL =
      "SELECT idRecord, content, date FROM DietRecords WHERE idPatient = ? AND date >= ?;";

  @Language("SQL")
  public static final String SelectTransportsSQL =
      "SELECT idRecord, content, date FROM TransportsRecords WHERE idPatient = ? AND date >= ?;";

  @Language("SQL")
  public static final String SelectReceptionSQL =
      "SELECT idRecord, content, date FROM ReceptionRecords WHERE idPatient = ? AND date >= ?;";

  @Language("SQL")
  public static final String SelectPrescriptionsSQL =
      "SELECT idRecord, content, date FROM PrescriptionRecords WHERE idPatient = ? AND date >= ?;";

  @Language("SQL")
  public static final String SelectRecordIDTestResultSQL =
      "SELECT MAX(idRecord) FROM TestResultsRecords WHERE idPatient = ?;";

  @Language("SQL")
  public static final String SelectRecordIDHouseKeepingSQL =
      "SELECT MAX(idRecord) FROM HousekeepingRecords WHERE idPatient = ?;";

  @Language("SQL")
  public static final String SelectRecordIDDietSQL =
      "SELECT MAX(idRecord) FROM DietRecords WHERE idPatient = ?;";

  @Language("SQL")
  public static final String SelectRecordIDTransportsSQL =
      "SELECT MAX(idRecord) FROM TransportsRecords WHERE idPatient = ?;";

  @Language("SQL")
  public static final String SelectRecordIDReceptionSQL =
      "SELECT MAX(idRecord) FROM ReceptionRecords WHERE idPatient = ?;";

  @Language("SQL")
  public static final String SelectRecordIDPrescriptionsSQL =
      "SELECT MAX(idRecord) FROM PrescriptionRecords WHERE idPatient = ?;";

  @Language("SQL")
  public static final String SelectTResAuthSQL =
      "SELECT labCrt, labSig FROM Labs "
          + "NATURAL JOIN TestResultsRecords "
          + "WHERE idPatient = ? AND idRecord = ?;";

  private static final DateTimeFormatter dateTimeFormatter =
      DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

  public static void createTables(Connection con) throws SQLException {
    Statement stmt = con.createStatement();
    stmt.executeUpdate(
        "CREATE TABLE IF NOT EXISTS PatientDetails("
            + "idPatient SERIAL NOT NULL,"
            + "nif INTEGER NOT NULL UNIQUE,"
            + "name TEXT NOT NULL,"
            + "PRIMARY KEY (idPatient));");

    stmt.executeUpdate(
        "CREATE TABLE IF NOT EXISTS UserCredentials("
            + "uname VARCHAR(20) NOT NULL,"
            + "pwdHash BYTEA NOT NULL,"
            + "pwdSalt BYTEA NOT NULL,"
            + "role TEXT NOT NULL,"
            + "PRIMARY KEY (uname));");

    stmt.executeUpdate(
        "CREATE TABLE IF NOT EXISTS Labs("
            + "labUname VARCHAR(20) NOT NULL REFERENCES UserCredentials(uname),"
            + "labCrt BYTEA NOT NULL,"
            + "PRIMARY KEY (labUname));");

    stmt.executeUpdate(
        "CREATE TABLE IF NOT EXISTS TestResultsRecords("
            + "idPatient INTEGER NOT NULL REFERENCES PatientDetails(idPatient),"
            + "idRecord INTEGER NOT NULL,"
            + "content TEXT NOT NULL,"
            + "date TIMESTAMP NOT NULL,"
            + "labUname VARCHAR(20) NOT NULL REFERENCES Labs(labUname),"
            + "labSig BYTEA NOT NULL,"
            + "PRIMARY KEY (idPatient, idRecord));");

    stmt.executeUpdate(
        "CREATE TABLE IF NOT EXISTS HousekeepingRecords("
            + "idPatient INTEGER NOT NULL REFERENCES PatientDetails(idPatient),"
            + "idRecord INTEGER NOT NULL,"
            + "content TEXT NOT NULL,"
            + "date TIMESTAMP NOT NULL,"
            + "PRIMARY KEY (idPatient, idRecord));");

    stmt.executeUpdate(
        "CREATE TABLE IF NOT EXISTS DietRecords("
            + "idPatient INTEGER NOT NULL REFERENCES PatientDetails(idPatient),"
            + "idRecord INTEGER NOT NULL,"
            + "content TEXT NOT NULL,"
            + "date TIMESTAMP NOT NULL,"
            + "PRIMARY KEY (idPatient, idRecord));");

    stmt.executeUpdate(
        "CREATE TABLE IF NOT EXISTS TransportsRecords("
            + "idPatient INTEGER NOT NULL REFERENCES PatientDetails(idPatient),"
            + "idRecord INTEGER NOT NULL,"
            + "content TEXT NOT NULL,"
            + "date TIMESTAMP NOT NULL,"
            + "PRIMARY KEY (idPatient, idRecord));");

    stmt.executeUpdate(
        "CREATE TABLE IF NOT EXISTS ReceptionRecords("
            + "idPatient INTEGER NOT NULL REFERENCES PatientDetails(idPatient),"
            + "idRecord INTEGER NOT NULL,"
            + "content TEXT NOT NULL,"
            + "date TIMESTAMP NOT NULL,"
            + "PRIMARY KEY (idPatient, idRecord));");

    stmt.executeUpdate(
        "CREATE TABLE IF NOT EXISTS PrescriptionRecords("
            + "idPatient INTEGER NOT NULL REFERENCES PatientDetails(idPatient),"
            + "idRecord INTEGER NOT NULL,"
            + "content TEXT NOT NULL,"
            + "date TIMESTAMP NOT NULL,"
            + "PRIMARY KEY (idPatient, idRecord));");
    stmt.close();
  }

  public static void populate(Connection con, String recordsPath)
      throws SQLException, FileNotFoundException {
    popPatientDetails(
        con.prepareStatement(InsertPatientDetailsSQL),
        new Scanner(new FileReader(String.format("%sPatientDetails.txt", recordsPath))));
    popUserCredentials(
        con.prepareStatement(InsertUserCredentialsSQL),
        new Scanner(new FileReader(String.format("%sUserCredentials.txt", recordsPath))));
    popLabs(con.prepareStatement(InsertLabsSQL));
    popTestResultsRecords(
        con.prepareStatement(InsertTestResultSQL),
        new Scanner(new FileReader(String.format("%sTestResultsRecords.txt", recordsPath))));
    popRecords(
        con.prepareStatement(InsertHouseKeepingSQL),
        new Scanner(new FileReader(String.format("%sHousekeepingRecords.txt", recordsPath))));
    popRecords(
        con.prepareStatement(InsertDietSQL),
        new Scanner(new FileReader(String.format("%sDietRecords.txt", recordsPath))));
    popRecords(
        con.prepareStatement(InsertTransportsSQL),
        new Scanner(new FileReader(String.format("%sTransportsRecords.txt", recordsPath))));
    popRecords(
        con.prepareStatement(InsertReceptionSQL),
        new Scanner(new FileReader(String.format("%sReceptionRecords.txt", recordsPath))));
    popRecords(
        con.prepareStatement(InsertPrescriptionsSQL),
        new Scanner(new FileReader(String.format("%sPrescriptionRecords.txt", recordsPath))));
  }

  public static void popPatientDetails(PreparedStatement stmt, Scanner scanner)
      throws SQLException {
    while (scanner.hasNextLine()) {
      String[] patientDetail = scanner.nextLine().split(",");
      stmt.setInt(1, Integer.parseInt(patientDetail[0]));
      stmt.setString(2, patientDetail[1]);
      stmt.executeUpdate();
    }
    stmt.close();
  }

  public static void popUserCredentials(PreparedStatement stmt, Scanner scanner)
      throws SQLException {
    while (scanner.hasNextLine()) {
      String[] employeeCredential = scanner.nextLine().split(",");
      byte[] salt = PwdBE.newSalt();
      stmt.setString(1, employeeCredential[0]);
      stmt.setBytes(2, PwdBE.hash(employeeCredential[1], salt));
      stmt.setBytes(3, salt);
      stmt.setString(4, employeeCredential[2]);
      stmt.executeUpdate();
    }
    stmt.close();
  }

  public static void popTestResultsRecords(PreparedStatement stmt, Scanner scanner)
      throws SQLException {
    while (scanner.hasNextLine()) {
      String[] testResult = scanner.nextLine().split(",");
      int pid = Integer.parseInt(testResult[0]);
      stmt.setInt(1, pid);
      stmt.setInt(2, Integer.parseInt(testResult[1]));
      stmt.setString(3, testResult[2]);
      Timestamp ts = toTimestamp(testResult[3]);
      stmt.setTimestamp(4, ts);
      stmt.setString(5, testResult[4]);
      byte[] hash = Crypto.hash(String.format("%d%s%d", pid, testResult[2], ts.getTime()));
      stmt.setBytes(6, Crypto.cipherBytesRSAPriv("HS", hash));
      stmt.executeUpdate();
    }
    stmt.close();
  }

  public static void popRecords(PreparedStatement stmt, Scanner scanner) throws SQLException {
    while (scanner.hasNextLine()) {
      String[] record = scanner.nextLine().split(",");
      stmt.setInt(1, Integer.parseInt(record[0]));
      stmt.setInt(2, Integer.parseInt(record[1]));
      stmt.setString(3, record[2]);
      stmt.setTimestamp(4, toTimestamp(record[3]));
      stmt.executeUpdate();
    }
    stmt.close();
  }

  public static void popLabs(PreparedStatement stmt) throws SQLException {
    stmt.setString(1, "HS");
    stmt.setBytes(2, Crypto.getCrtBytes("HS"));
    stmt.executeUpdate();
    stmt.close();
  }

  public static Timestamp toTimestamp(String ts) {
    return Timestamp.valueOf(LocalDateTime.parse(ts, dateTimeFormatter));
  }
}
