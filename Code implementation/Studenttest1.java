package com.example;

import java.sql.CallableStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

import oracle.jdbc.OracleConnection;
import oracle.jdbc.pool.OracleDataSource;

public class Studenttest1 {
    static final String ADMIN_URL = "";
    static final String ADMIN_USER = "";
    static final String ADMIN_PASSWORD = "";

    public static void main(String[] args) {
        int perm = 121231131;
        String defaultPin = "0000";
        try {
            //System.out.println("Inserting new student as ADMIN...");
            //insertNewStudent(perm, "Test Student", "123 Campus Way", "CS", "MyMajor");

            //System.out.println("Creating DB user for new student...");
            //createStudentUser(perm, defaultPin);

            System.out.println("Logging in as new student...");
            testStudentLogin(perm, defaultPin);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void insertNewStudent(int perm, String name, String address, String dept, String major) throws SQLException {
        OracleConnection conn = connectAsUser(ADMIN_USER, ADMIN_PASSWORD);
        try (Statement stmt = conn.createStatement()) {
            String checkMajorSql = String.format("SELECT COUNT(*) FROM Majors WHERE MajorName = '%s'", major);
            ResultSet rs = stmt.executeQuery(checkMajorSql);
            rs.next();
            if (rs.getInt(1) == 0) {
                System.out.println("⚠️ Major not found. Inserting new major...");
                String insertMajorSql = String.format(
                    "INSERT INTO Majors (MajorName, dept, num_elect) VALUES ('%s', '%s', 3)", major, dept);
                stmt.executeUpdate(insertMajorSql);
            }

            String sql = String.format(
                "INSERT INTO Students (perm, name, address, dept, pin, major) " +
                "VALUES (%d, '%s', '%s', '%s', '%s', '%s')",
                perm, name, address, dept, "0000", major
            );
            stmt.executeUpdate(sql);
            System.out.println("✅ Student inserted.");
        } finally {
            conn.close();
        }
    }

    public static void createStudentUser(int perm, String pin) throws SQLException {
        OracleConnection conn = connectAsUser(ADMIN_USER, ADMIN_PASSWORD);
        try (CallableStatement cs = conn.prepareCall("{CALL CreateStudentUser(?, ?)}")) {
            cs.setInt(1, perm);
            cs.setString(2, pin);
            cs.execute();
            System.out.println("✅ Student DB user created.");
        } finally {
            conn.close();
        }
    }

public static void testStudentLogin(int perm, String pin) throws SQLException {
    String username =   String.valueOf(perm);
    String password = "Student" + pin;  

    OracleConnection conn = connectAsUser(username, password);
    try (Statement stmt = conn.createStatement()) {
        ResultSet rs = stmt.executeQuery("SELECT * FROM ADMIN.STUDENTINFO");
        System.out.println("✅ Student connected. Student info:");
        while (rs.next()) {
            System.out.println("Perm: " + rs.getString("perm"));
            System.out.println("Name: " + rs.getString("name"));
        }
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
        ods.setURL(ADMIN_URL);
        ods.setConnectionProperties(info);
        return (OracleConnection) ods.getConnection();
    }
}

