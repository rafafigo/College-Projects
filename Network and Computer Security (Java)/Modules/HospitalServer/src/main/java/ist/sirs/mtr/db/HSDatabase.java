package ist.sirs.mtr.db;

import ist.sirs.mtr.error.AssertError;
import ist.sirs.mtr.exception.HSRuntimeException;
import ist.sirs.mtr.proto.hse.ErrorMessage;
import ist.sirs.mtr.pwd.PwdBE;
import org.postgresql.util.PSQLException;

import java.io.FileNotFoundException;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static ist.sirs.mtr.proto.hspl.ErrorMessage.MODIFIED_CERTIFICATE;
import static org.postgresql.util.PSQLState.FOREIGN_KEY_VIOLATION;
import static org.postgresql.util.PSQLState.UNIQUE_VIOLATION;

public class HSDatabase {
  private final Connection conn;

  public HSDatabase(String dbName, String dbUser, String dbPwd, String recordsPath) {
    String url = String.format("jdbc:postgresql://localhost:5432/%s", dbName);
    try {
      conn = DriverManager.getConnection(url, dbUser, dbPwd);
      if (recordsPath != null) {
        HSPopulate.createTables(conn);
        HSPopulate.populate(conn, recordsPath);
      }
    } catch (SQLException | FileNotFoundException e) {
      throw new AssertError(HSDatabase.class.getSimpleName(), "Constructor", e);
    }
  }

  // Fonts:
  // http://www.projectocolibri.com/forum/api-interface-de-programacao/4599-como-validar-nif-em-java
  private static boolean isNif(String nif) {
    final int max = 9;
    // Check if is Numeric and has 9 Numbers
    if (!nif.matches("[0-9]+") || nif.length() != max) return false;
    int checkSum = 0;
    // Calculate CheckSum
    for (int i = 0; i < max - 1; i++) {
      checkSum += (nif.charAt(i) - '0') * (max - i);
    }
    int checkDigit = 11 - (checkSum % 11);
    if (checkDigit >= 10) checkDigit = 0;
    // Compare checkDigit with the Last Number of Nif
    return checkDigit == nif.charAt(max - 1) - '0';
  }

  public String isEmployeeGetRole(String uname, String pwd) {
    try {
      PreparedStatement stmt = conn.prepareStatement(HSPopulate.SelectEmployeeCredentialsSQL);
      stmt.setString(1, uname);
      ResultSet rs = stmt.executeQuery();
      stmt.close();
      if (!rs.next()) return null;
      byte[] pwdHash = rs.getBytes("pwdHash");
      byte[] pwdSalt = rs.getBytes("pwdSalt");
      String role = rs.getString("role");
      return PwdBE.isExpectedPwd(pwd, pwdHash, pwdSalt) ? role : null;
    } catch (SQLException e) {
      throw new AssertError(HSDatabase.class.getSimpleName(), "isEmployeeGetRole", e);
    }
  }

  public boolean isLab(String uname, String pwd) {
    try {
      PreparedStatement stmt = conn.prepareStatement(HSPopulate.SelectLabCredentialsSQL);
      stmt.setString(1, uname);
      ResultSet rs = stmt.executeQuery();
      stmt.close();
      if (!rs.next()) return false;
      byte[] pwdHash = rs.getBytes("pwdHash");
      byte[] pwdSalt = rs.getBytes("pwdSalt");
      return PwdBE.isExpectedPwd(pwd, pwdHash, pwdSalt);
    } catch (SQLException e) {
      throw new AssertError(HSDatabase.class.getSimpleName(), "isLab", e);
    }
  }

  public void insertLabCrt(String uname, byte[] labCrt) throws HSRuntimeException {
    try {
      PreparedStatement stmt = conn.prepareStatement(HSPopulate.SelectLabCrtSQL);
      stmt.setString(1, uname);
      ResultSet rs = stmt.executeQuery();
      stmt.close();
      if (rs.next()) {
        if (!Arrays.equals(rs.getBytes("labCrt"), labCrt))
          throw new HSRuntimeException(MODIFIED_CERTIFICATE.lbl);
        return;
      }
      stmt = conn.prepareStatement(HSPopulate.InsertLabsSQL);
      stmt.setString(1, uname);
      stmt.setBytes(2, labCrt);
      stmt.executeUpdate();
      stmt.close();
    } catch (SQLException e) {
      throw new AssertError(HSDatabase.class.getSimpleName(), "insertLabCrt", e);
    }
  }

  public List<DBPatientRecord> read(int pid, int recType, LocalDateTime recTs)
      throws HSRuntimeException {
    List<DBPatientRecord> records = new ArrayList<>();
    String sql = createSql(recType, true);
    try {
      PreparedStatement stmt = conn.prepareStatement(sql);
      stmt.setInt(1, pid);
      stmt.setTimestamp(2, Timestamp.valueOf(recTs));
      ResultSet rs = stmt.executeQuery();
      stmt.close();
      while (rs.next()) {
        records.add(
            new DBPatientRecord(
                rs.getInt("idRecord"),
                rs.getString("content"),
                rs.getTimestamp("date").toLocalDateTime()));
      }
    } catch (SQLException e) {
      throw new AssertError(HSDatabase.class.getSimpleName(), "read", e);
    }
    return records;
  }

  public void write(
      int pid, int recType, String recCont, String labUname, byte[] labSig, long millis)
      throws HSRuntimeException {
    try {
      PreparedStatement stmt = conn.prepareStatement(createSql(recType, false));
      stmt.setInt(1, pid);
      stmt.setInt(2, getRecordId(pid, recType));
      stmt.setString(3, recCont);
      stmt.setTimestamp(4, new Timestamp(millis));
      if (recType == 0) {
        stmt.setString(5, labUname);
        stmt.setBytes(6, labSig);
      }
      stmt.executeUpdate();
      stmt.close();
    } catch (PSQLException e) {
      if (e.getSQLState().equals(FOREIGN_KEY_VIOLATION.getState()))
        throw new HSRuntimeException(ErrorMessage.PID_NOT_FOUND.lbl);
      else throw new AssertError(HSDatabase.class.getSimpleName(), "write", e);
    } catch (SQLException e) {
      throw new AssertError(HSDatabase.class.getSimpleName(), "write", e);
    }
  }

  public List<DBPatientDetails> readPatientDetails(int pid, int nif, String name)
      throws HSRuntimeException {
    try {
      PreparedStatement stmt;
      List<DBPatientDetails> pDetailsList = new ArrayList<>();
      if (pid != 0) {
        stmt = conn.prepareStatement(HSPopulate.SelectPatientDetailsByIdPatientSQL);
        stmt.setInt(1, pid);
      } else if (nif != 0) {
        stmt = conn.prepareStatement(HSPopulate.SelectPatientDetailsByNifSQL);
        stmt.setInt(1, nif);
      } else if (name.length() != 0) {
        stmt = conn.prepareStatement(HSPopulate.SelectPatientDetailsByNameSQL);
        stmt.setString(1, "%" + name + "%");
      } else {
        throw new HSRuntimeException(ErrorMessage.NO_FIELD_SPECIFIED.lbl);
      }
      ResultSet rs = stmt.executeQuery();
      stmt.close();
      while (rs.next()) {
        pDetailsList.add(
            new DBPatientDetails(rs.getInt("idPatient"), rs.getInt("nif"), rs.getString("name")));
      }
      return pDetailsList;
    } catch (SQLException e) {
      throw new AssertError(HSDatabase.class.getSimpleName(), "readPatientDetails", e);
    }
  }

  public DBPatientDetails writePatientDetails(Integer nif, String name) throws HSRuntimeException {
    if (!isNif(nif.toString())) {
      throw new HSRuntimeException(ErrorMessage.INVALID_NIF.lbl);
    }
    try {
      PreparedStatement stmt = conn.prepareStatement(HSPopulate.InsertPatientDetailsSQL);
      stmt.setInt(1, nif);
      stmt.setString(2, name);
      stmt.executeUpdate();
      stmt.close();
      return readPatientDetails(0, nif, name).get(0);
    } catch (PSQLException e) {
      if (e.getSQLState().equals(UNIQUE_VIOLATION.getState()))
        throw new HSRuntimeException(ErrorMessage.NIF_ALREADY_EXISTS.lbl);
      else throw new AssertError(HSDatabase.class.getSimpleName(), "writePatientDetails", e);
    } catch (SQLException e) {
      throw new AssertError(HSDatabase.class.getSimpleName(), "writePatientDetails", e);
    }
  }

  public void writeUserCredentials(String uname, String pwd, String role)
      throws HSRuntimeException {
    try {
      PreparedStatement stmt = conn.prepareStatement(HSPopulate.InsertUserCredentialsSQL);
      byte[] salt = PwdBE.newSalt();
      stmt.setString(1, uname);
      stmt.setBytes(2, PwdBE.hash(pwd, salt));
      stmt.setBytes(3, salt);
      stmt.setString(4, role);
      stmt.executeUpdate();
      stmt.close();
    } catch (PSQLException e) {
      if (e.getSQLState().equals(UNIQUE_VIOLATION.getState()))
        throw new HSRuntimeException(ErrorMessage.INVALID_UNAME.lbl);
      else throw new AssertError(HSDatabase.class.getSimpleName(), "writeUserCredentials", e);
    } catch (DataTruncation e) {
      throw new HSRuntimeException(ErrorMessage.INVALID_UNAME.lbl);
    } catch (SQLException e) {
      throw new AssertError(HSDatabase.class.getSimpleName(), "writeUserCredentials", e);
    }
  }

  public DBTResAuth tResAuth(int pid, int recId) throws HSRuntimeException {
    try {
      PreparedStatement stmt = conn.prepareStatement(HSPopulate.SelectTResAuthSQL);
      stmt.setInt(1, pid);
      stmt.setInt(2, recId);
      ResultSet res = stmt.executeQuery();
      stmt.close();
      if (!res.next()) throw new HSRuntimeException(ErrorMessage.NO_RECORD.lbl);
      return new DBTResAuth(res.getBytes("labSig"), res.getBytes("labCrt"));
    } catch (SQLException e) {
      throw new AssertError(HSDatabase.class.getSimpleName(), "tResAuth", e);
    }
  }

  private int getRecordId(int pid, int recType) throws HSRuntimeException, SQLException {
    PreparedStatement stmt = conn.prepareStatement(getSqlRecordId(recType));
    stmt.setInt(1, pid);
    ResultSet res = stmt.executeQuery();
    if (!res.next()) throw new AssertError(HSDatabase.class.getSimpleName(), "getRecordId");
    return res.getInt(1) + 1;
  }

  private String getSqlRecordId(int recType) throws HSRuntimeException {
    switch (recType) {
      case 0:
        return HSPopulate.SelectRecordIDTestResultSQL;
      case 1:
        return HSPopulate.SelectRecordIDHouseKeepingSQL;
      case 2:
        return HSPopulate.SelectRecordIDDietSQL;
      case 3:
        return HSPopulate.SelectRecordIDTransportsSQL;
      case 4:
        return HSPopulate.SelectRecordIDReceptionSQL;
      case 5:
        return HSPopulate.SelectRecordIDPrescriptionsSQL;
      default:
        throw new HSRuntimeException(ErrorMessage.INVALID_RECORD_TYPE.lbl);
    }
  }

  private String createSql(int recType, boolean isSelect) throws HSRuntimeException {
    switch (recType) {
      case 0:
        return isSelect ? HSPopulate.SelectTestResultSQL : HSPopulate.InsertTestResultSQL;
      case 1:
        return isSelect ? HSPopulate.SelectHouseKeepingSQL : HSPopulate.InsertHouseKeepingSQL;
      case 2:
        return isSelect ? HSPopulate.SelectDietSQL : HSPopulate.InsertDietSQL;
      case 3:
        return isSelect ? HSPopulate.SelectTransportsSQL : HSPopulate.InsertTransportsSQL;
      case 4:
        return isSelect ? HSPopulate.SelectReceptionSQL : HSPopulate.InsertReceptionSQL;
      case 5:
        return isSelect ? HSPopulate.SelectPrescriptionsSQL : HSPopulate.InsertPrescriptionsSQL;
      default:
        throw new HSRuntimeException(ErrorMessage.INVALID_RECORD_TYPE.lbl);
    }
  }

  public void closeConn() {
    try {
      conn.close();
    } catch (SQLException e) {
      throw new AssertError(HSDatabase.class.getSimpleName(), "closeConn", e);
    }
  }
}
