// Java code that uses a shared database user and enforces per-student access control

package com.example;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

import oracle.jdbc.OracleConnection;
import oracle.jdbc.pool.OracleDataSource;
//import sun.jvm.hotspot.code.SafepointBlob;

public class Studenttest2 {
    static final String DB_URL = "jdbc:oracle:thin:@cs174adb_tp?TNS_ADMIN=C:/Users/eli/Downloads/project/demo/wallet";
    static final String APP_USER = "STUDENT_ACCESS"; // shared DB user
    static final String APP_PASSWORD = "Somebodyplease23";

     public static void main(String[] args) {
        String perm = "001421";
        String pin = "0000";
        
        try {
            //insertNewStudent(perm, "Stringname", "String", "CS", "CS", "0000");
            if (authenticateStudent(perm, pin)) {
                showStudentInfo(perm);
                // Optionally allow taking course actions here...
            } else {
                System.out.println("Invalid credentials.");
            }
            SetPin(perm, pin, "0001");
            showStudentInfo(perm);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

public static boolean authenticateStudent(String perm, String pin) throws SQLException {
    return VerifyPin(perm, pin);    
}



    public static void showStudentInfo(String perm) throws SQLException {
        try (Connection conn = connectAsAppUser()) {
            PreparedStatement stmt = conn.prepareStatement("SELECT * FROM Admin.STUDENTINFO WHERE perm = ?");
            stmt.setString(1, perm);
            ResultSet rs = stmt.executeQuery();

            System.out.println("Student Info:");
            while (rs.next()) {
                System.out.println("Perm: " + rs.getString("perm"));
                System.out.println("Name: " + rs.getString("name"));
                ///System.out.println("Name: " + rs.getString("pin"));
                // Add other fields as needed
            }
        }
    }
    
    public static boolean  VerifyPin(String perm, String pin) throws SQLException {
        try (Connection conn = connectAsAppUser()) {
        String hashedPin = hashPin(pin);
        CallableStatement stmt = conn.prepareCall("{ ? = call Admin.VerifyPin(?, ?) }");
        stmt.registerOutParameter(1, java.sql.Types.INTEGER);
        stmt.setString(2, perm);
        stmt.setString(3, hashedPin); 
        stmt.execute();
        int result = stmt.getInt(1);
        return result == 1;
        }
    }
    public static boolean  SetPin(String perm, String pin, String newpin) throws SQLException {
        try (Connection conn = connectAsAppUser()) {
        String hashedPin = hashPin(pin);
        String hashedPin2 = hashPin(newpin);
        CallableStatement stmt = conn.prepareCall("{ ? = call Admin.Setpin(?, ?, ?) }");
        stmt.registerOutParameter(1, java.sql.Types.INTEGER);
        stmt.setString(2, perm);
        stmt.setString(3, hashedPin); 
        stmt.setString(4, hashedPin2); 
        stmt.execute();
        int result = stmt.getInt(1);
        return result == 1;
        }
    }
    public static Connection connectAsAppUser() throws SQLException {
        Properties info = new Properties();
        info.put(OracleConnection.CONNECTION_PROPERTY_USER_NAME, APP_USER);
        info.put(OracleConnection.CONNECTION_PROPERTY_PASSWORD, APP_PASSWORD);
        info.put(OracleConnection.CONNECTION_PROPERTY_DEFAULT_ROW_PREFETCH, "20");

        System.setProperty("oracle.net.tns_admin", "C:/Users/eli/Downloads/project/demo/wallet");
        OracleDataSource ods = new OracleDataSource();
        ods.setURL(DB_URL);
        ods.setConnectionProperties(info);
        return ods.getConnection();
    }
        public static void insertNewStudent(String perm, String name, String address, String dept, String major, String pin) throws SQLException {
        OracleConnection conn = connectAsUser("ADMIN", "Somebodyplease2#");
        try (Statement stmt = conn.createStatement()) {
            String checkMajorSql = String.format("SELECT COUNT(*) FROM Majors WHERE MajorName = '%s'", major);
            ResultSet rs = stmt.executeQuery(checkMajorSql);
            rs.next();
            if (rs.getInt(1) == 0) {
                System.out.println("Major not found. Inserting new major...");
                String insertMajorSql = String.format(
                    "INSERT INTO Majors (MajorName, dept, num_elect) VALUES ('%s', '%s', 3)", major, dept);
                stmt.executeUpdate(insertMajorSql);
            }
                String hashedPin = hashPin(pin);
                String sql = String.format(
                "INSERT INTO Students (perm, name, address, dept, major, pin) " +
                "VALUES ('%s', '%s', '%s', '%s', '%s', '%s')",
                perm, name, address, dept,  major, hashedPin
            );
            stmt.executeUpdate(sql);
            System.out.println("Student inserted.");
        } finally {
            conn.close();
        }
    }
        public static OracleConnection connectAsUser(String user, String password) throws SQLException {
        Properties info = new Properties();
        info.put(OracleConnection.CONNECTION_PROPERTY_USER_NAME, user);
        info.put(OracleConnection.CONNECTION_PROPERTY_PASSWORD, password);
        info.put(OracleConnection.CONNECTION_PROPERTY_DEFAULT_ROW_PREFETCH, "20");

        System.setProperty("oracle.net.tns_admin", "C:/Users/eli/Downloads/project/demo/wallet");
        OracleDataSource ods = new OracleDataSource();
        ods.setURL("jdbc:oracle:thin:@cs174adb_tp?TNS_ADMIN=C:/Users/eli/Downloads/project/demo/wallet");
        ods.setConnectionProperties(info);
        return (OracleConnection) ods.getConnection();
    }

        public static String hashPin(String pin) {
    try {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] hashBytes = md.digest(pin.getBytes());
        BigInteger number = new BigInteger(1, hashBytes);
        return String.format("%064x", number); // zero-padded to 64 hex characters
    } catch (NoSuchAlgorithmException e) {
        throw new RuntimeException("SHA-256 not supported", e);
    }
}

}
