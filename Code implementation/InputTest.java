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
import java.util.Scanner;

import oracle.jdbc.OracleConnection;
import oracle.jdbc.pool.OracleDataSource;
//import sun.jvm.hotspot.code.SafepointBlob;

public class InputTest {
    static final String DB_URL = "jdbc:oracle:thin:@cs174adb_tp?TNS_ADMIN=C:/Users/eli/Downloads/project/demo/wallet";
    static final String APP_USER = "STUDENT_ACCESS"; // shared DB user
    static final String APP_PASSWORD = "Somebodyplease23";
    static String studentPerm = null;
    static String CurrentQuarter = "S";
    static int CurrentYear = 25;
    public static void main(String[] args) {

try (Scanner scanner = new Scanner(System.in)) {
            System.out.print("Regent or Student? : ");
            String Usertype = scanner.nextLine().trim();
            Connection conn;
            String inputPerm = "-1";
            String inputPin = "-1";
            if("student".equalsIgnoreCase(Usertype)){
                conn = connectAsAppUser();
            while (studentPerm == null) {
                System.out.print("Enter perm: ");
                inputPerm = scanner.nextLine().trim();

                System.out.print("Enter pin: ");
                inputPin = scanner.nextLine().trim();

                if (VerifyPin(inputPerm, inputPin, conn)) {
                    studentPerm = inputPerm;
                    System.out.println("Login successful.");
                } else {
                    System.out.println("Invalid perm or pin. Try again.");
                }
            }}
            else {
                System.out.println("Only 'student' login is currently supported.");
                return;
            }
            
            while (true) {
                System.out.print(">> ");
                String command = scanner.nextLine().trim();

                if (command.equalsIgnoreCase("EXIT")) {
                    System.out.println("Goodbye!");
                    break;
                }else if (command.equalsIgnoreCase("VIEW")) {
                System.out.print("Enter current quarter (e.g., F, W, S, or Any): ");
                String quarter = scanner.nextLine().trim().toUpperCase();
                System.out.print("Enter current year (e.g., 24, 25, or 0 (0 = Any): ");
                int year = Integer.parseInt(scanner.nextLine().trim());

                showAvailableOfferings(conn, year, quarter);
}
                else if(command.equalsIgnoreCase("ADD")){
                    showAvailableOfferings(conn, CurrentYear, CurrentQuarter);
                    System.out.print("Enter Offering ID: ");
                    int offeringID = Integer.parseInt(scanner.nextLine().trim());
                    AddCourse(offeringID, inputPerm, conn);
                }
                else if(command.equalsIgnoreCase(("Drop"))){
                    displayTakingCourse(inputPerm, conn);
                    System.out.print("Enter Offering ID: ");
                    int offeringID = Integer.parseInt(scanner.nextLine().trim());
                    System.out.print("Enter Course ID: ");
                    String courseID = scanner.nextLine().trim().toUpperCase();
                    dropTakingCourse(offeringID, inputPerm, courseID, conn);
                }
else {
                    System.out.println("Unknown command.");
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
     }

//public static boolean authenticateStudent(String perm, String pin) throws SQLException {
 //   return VerifyPin(perm, pin);    
//}

public static void displayTakingCourse(String perm, Connection conn) throws SQLException {
    String sql = "SELECT * FROM ADMIN.STUDENTTAKING WHERE StudentPerm = ? ORDER BY Year DESC, Quarter DESC";

    try (PreparedStatement stmt = conn.prepareStatement(sql)) {
        stmt.setString(1, perm);
        ResultSet rs = stmt.executeQuery();

        System.out.printf("%-10s %-10s %-25s %-8s %-10s %-15s %-10s %-6s %-6s %-6s%n",
            "OfferingID", "CourseID", "Title", "Days", "Time", "Professor", "Building", "Room", "Quarter", "Year");

        boolean hasResults = false;
        while (rs.next()) {
            hasResults = true;
            System.out.printf("%-10d %-10s %-25s %-8s %-10s %-15s %-10s %-6s %-6s %-6d%n",
                rs.getInt("OfferingID"),
                rs.getString("CourseID"),
                rs.getString("CourseTitle"),
                rs.getString("Day"),
                rs.getString("Timeslot"),
                rs.getString("ProfessorName"),
                rs.getString("Building"),
                rs.getString("Room"),
                rs.getString("Quarter"),
                rs.getInt("Year"));
        }

        if (!hasResults) {
            System.out.println("You are not currently enrolled in any courses.");
        }
    }
}


private static void showAvailableOfferings(Connection conn, int year, String quarter) {
    StringBuilder sql = new StringBuilder("SELECT * FROM ADMIN.STUDENTOFFERING WHERE 1=1");

    boolean includeQuarter = quarter != null && !quarter.equalsIgnoreCase("Any");
    boolean includeYear = year != 0;

    if (includeQuarter) {
        sql.append(" AND Quarter = ?");
    }
    if (includeYear) {
        sql.append(" AND Year = ?");
    }

    try (PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
        int paramIndex = 1;
        if (includeQuarter) {
            stmt.setString(paramIndex++, quarter);
        }
        if (includeYear) {
            stmt.setInt(paramIndex, year);
        }

        ResultSet rs = stmt.executeQuery();

        System.out.printf("%-8s %-7s %-20s %-6s %-10s %-15s %-8s %-6s %-6s %-8s%n",
            "OfferID", "Course", "Title", "Days", "Time", "Professor", "Bldg", "Room", "Year", "Quarter");

        while (rs.next()) {
            System.out.printf("%-8d %-7s %-20s %-6s %-10s %-15s %-8s %-6s %-6d %-8s%n",
                rs.getInt("OfferingID"),
                rs.getString("CourseID"),
                rs.getString("CourseTitle"),
                rs.getString("Day"),
                rs.getString("Timeslot"),
                rs.getString("ProfessorName"),
                rs.getString("Building"),
                rs.getString("Room"),
                rs.getInt("Year"),
                rs.getString("Quarter"));
        }

    } catch (SQLException e) {
        System.err.println("Failed to query view: " + e.getMessage());
    }
}




    private static void AddCourse(int offeringID, String perm, Connection conn) {
    
    

    try (PreparedStatement stmt = conn.prepareStatement("SELECT * FROM ADMIN.STUDENTOFFERING WHERE OfferingID = ?"))
        {
        stmt.setInt(1, offeringID);
        ResultSet rs = stmt.executeQuery();
         if (rs.next()) {
    
            String courseID = rs.getString("CourseID");
            insertTakingCourse(offeringID, perm, courseID, conn);

        } else {
            System.out.println("No course found with OfferingID: " + offeringID);
        }
        }

     catch (SQLException e) {
        System.err.println("Failed to query view: " + e.getMessage());
    }
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
    
    public static boolean  VerifyPin(String perm, String pin, Connection conn) throws SQLException {
        String hashedPin = hashPin(pin);
        CallableStatement stmt = conn.prepareCall("{ ? = call Admin.VerifyPin(?, ?) }");
        stmt.registerOutParameter(1, java.sql.Types.INTEGER);
        stmt.setString(2, perm);
        stmt.setString(3, hashedPin); 
        stmt.execute();
        int result = stmt.getInt(1);
        return result == 1;
        
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
public static void insertNewCourseRequirement(String courseID, String prereqID) throws SQLException {
    OracleConnection conn = connectAsUser("ADMIN", "Somebodyplease2#");
    try {
        String checkSql = "SELECT COUNT(*) FROM ReqCourses WHERE CourseID1 = ? AND CourseID2 = ?";
        try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
            checkStmt.setString(1, courseID);
            checkStmt.setString(2, prereqID);
            ResultSet rs = checkStmt.executeQuery();
            rs.next();
            if (rs.getInt(1) > 0) {
                System.out.println("Requirement already exists: " + courseID + " requires " + prereqID);
                return;
            }
        }

        String insertSql = "INSERT INTO ReqCourses (CourseID1, CourseID2) VALUES (?, ?)";
        try (PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
            insertStmt.setString(1, courseID);
            insertStmt.setString(2, prereqID);
            insertStmt.executeUpdate();
            System.out.println("Requirement inserted: " + courseID + " requires " + prereqID);
        }

    } finally {
        conn.close();
    }
}

public static void insertNewStudent(String perm, String name, String address, String dept, String major, String pin) throws SQLException {
    OracleConnection conn = connectAsUser("ADMIN", "Somebodyplease2#");
    try {

        String checkStudentSql = "SELECT COUNT(*) FROM Students WHERE perm = ?";
        try (PreparedStatement checkStmt = conn.prepareStatement(checkStudentSql)) {
            checkStmt.setString(1, perm);
            ResultSet studentCheck = checkStmt.executeQuery();
            studentCheck.next();
            if (studentCheck.getInt(1) > 0) {
                System.out.println("Student already exists. Skipping insertion.");
                return;
            }
        }
        String checkMajorSql = "SELECT COUNT(*) FROM Majors WHERE MajorName = ?";
        try (PreparedStatement majorStmt = conn.prepareStatement(checkMajorSql)) {
            majorStmt.setString(1, major);
            ResultSet majorCheck = majorStmt.executeQuery();
            majorCheck.next();
            if (majorCheck.getInt(1) == 0) {
                System.out.println("Error: Major not found. Student not inserted.");
                return;
            }
        }

        String hashedPin = hashPin(pin);
        String insertSql = "INSERT INTO Students (perm, name, address, dept, major, pin) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
            insertStmt.setString(1, perm);
            insertStmt.setString(2, name);
            insertStmt.setString(3, address);
            insertStmt.setString(4, dept);
            insertStmt.setString(5, major);
            insertStmt.setString(6, hashedPin);
            insertStmt.executeUpdate();
            System.out.println("Student inserted: " + perm);
        }

    } finally {
        conn.close();
    }
}

public static void insertNewProfessor(int id, String name, String dept) throws SQLException {
    OracleConnection conn = connectAsUser("ADMIN", "Somebodyplease2#");
    try {
        // Check if professor already exists
        String checkSql = "SELECT COUNT(*) FROM Professors WHERE ID = ?";
        try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
            checkStmt.setInt(1, id);
            ResultSet rs = checkStmt.executeQuery();
            rs.next();
            if (rs.getInt(1) > 0) {
                System.out.println("Professor already exists: " + id);
                return;
            }
        }

        // Insert professor
        String insertSql = "INSERT INTO Professors (ID, Name, Dept) VALUES (?, ?, ?)";
        try (PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
            insertStmt.setInt(1, id);
            insertStmt.setString(2, name);
            insertStmt.setString(3, dept);
            insertStmt.executeUpdate();
            System.out.println("Professor inserted: " + name);
        }
    } finally {
        conn.close();
    }
}
public static void insertOffering( int offeringID, String courseID,int profID,String building,String room, String quarter, int year,String day, int capacity,String timeslot
) throws SQLException {
    OracleConnection conn = connectAsUser("ADMIN", "Somebodyplease2#");
    try {
        // Check if the offering already exists
        String checkSql = "SELECT COUNT(*) FROM Offerings WHERE OfferingID = ?";
        try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
            checkStmt.setInt(1, offeringID);
            ResultSet rs = checkStmt.executeQuery();
            rs.next();
            if (rs.getInt(1) > 0) {
                System.out.println("Offering already exists: " + offeringID);
                return;
            }
        }

        // Insert the offering
        String insertSql = "INSERT INTO Offerings (OfferingID, CourseID, Prof, Building, Room, Quarter, Year, Day, Capacity, Timeslot) " +
                           "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
            insertStmt.setInt(1, offeringID);
            insertStmt.setString(2, courseID);
            insertStmt.setInt(3, profID);
            insertStmt.setString(4, building);
            insertStmt.setString(5, room);
            insertStmt.setString(6, quarter);
            insertStmt.setInt(7, year);
            insertStmt.setString(8, day);
            insertStmt.setInt(9, capacity);
            insertStmt.setString(10, timeslot);
            insertStmt.executeUpdate();
            System.out.println("Offering inserted: " + offeringID + " (" + courseID + ")");
        }

    } finally {
        conn.close();
    }
}

public static void insertNewMajor(String name, String dept, int num_elect) throws SQLException {
    OracleConnection conn = connectAsUser("ADMIN", "Somebodyplease2#");
    try (Statement stmt = conn.createStatement()) {

        // Check if major already exists
        String checkSql = String.format("SELECT COUNT(*) FROM Majors WHERE MajorName = '%s'", name);
        ResultSet rs = stmt.executeQuery(checkSql);
        rs.next();
        if (rs.getInt(1) > 0) {
            System.out.println("Major already exists. Skipping insertion.");
            return;
        }

        String sql = String.format(
            "INSERT INTO Majors (MajorName, dept, num_elect) VALUES ('%s', '%s', %d)",
            name, dept, num_elect
        );
        stmt.executeUpdate(sql);
        System.out.println("Major inserted: " + name);
    } finally {
        conn.close();
    }
}

public static void dropTakingCourse(int offeringID, String perm, String courseID, Connection conn) throws SQLException {
    String sql = "DELETE FROM ADMIN.TAKING WHERE OfferingID = ? AND StudentPerm = ? AND CourseID = ?";
    try (PreparedStatement stmt = conn.prepareStatement(sql)) {
        stmt.setInt(1, offeringID);
        stmt.setString(2, perm);
        stmt.setString(3, courseID);

        int rowsAffected = stmt.executeUpdate();
        if (rowsAffected > 0) {
            System.out.println("Successfully dropped course: " + courseID);
        } else {
            System.out.println("No matching enrollment found to drop.");
        }
    }
}


public static void insertTakenCourse(String courseID, String perm, int year, String quarter, String grade) throws SQLException {
    OracleConnection conn = connectAsUser("ADMIN", "Somebodyplease2#");
    try {
        String checkSql = "SELECT COUNT(*) FROM Taken WHERE TCourseID = ? AND StudentPerm = ? AND TakenYear = ? AND Quarter = ?";
        try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
            checkStmt.setString(1, courseID);
            checkStmt.setString(2, perm);
            checkStmt.setInt(3, year);
            checkStmt.setString(4, quarter);
            ResultSet rs = checkStmt.executeQuery();
            rs.next();
            if (rs.getInt(1) > 0) {
                System.out.println("Already recorded: " + perm + " took " + courseID + " in " + year + quarter);
                return;
            }
        }

        String insertSql = "INSERT INTO Taken (TCourseID, StudentPerm, TakenYear, Quarter, grade) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
            insertStmt.setString(1, courseID);
            insertStmt.setString(2, perm);
            insertStmt.setInt(3, year);
            insertStmt.setString(4, quarter);
            insertStmt.setString(5, grade);
            insertStmt.executeUpdate();
            System.out.println("Inserted Taken: " + perm + " -> " + courseID + " (" + grade + ")");
        }
    } finally {
        conn.close();
    }
}
public static void insertTakingCourse(int offeringID, String perm, String courseID, Connection conn) throws SQLException {
    try {

        String checkSql = "SELECT COUNT(*) FROM ADMIN.STUDENTTAKING WHERE StudentPerm = ? AND CourseID = ?";
        try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
            checkStmt.setString(1, perm);
            checkStmt.setString(2, courseID);
            ResultSet rs = checkStmt.executeQuery();
            rs.next();
            if (rs.getInt(1) > 0) {
                System.out.println("Already enrolled: " + perm + " in " + courseID);
                return;
            }
        }

        String takenCheckSql = "SELECT grade FROM ADMIN.STUDENTTAKEN WHERE StudentPerm = ? AND TCourseID = ?";
        try (PreparedStatement takenStmt = conn.prepareStatement(takenCheckSql)) {
            takenStmt.setString(1, perm);
            takenStmt.setString(2, courseID);
            ResultSet rs = takenStmt.executeQuery();
            while (rs.next()) {
                String grade = rs.getString("Grade");
                if (grade != null && grade.compareToIgnoreCase("C") < 0) {
                    System.out.println("Course " + courseID + " already completed with grade: " + grade);
                    return;
                }
            }
        }

        String prereqCheckSql =
            "SELECT rc.CourseID2 FROM ADMIN.ReqCourses rc " +
            "WHERE rc.CourseID1 = ? " +
            "AND NOT EXISTS ( " +
            "  SELECT 1 FROM ADMIN.STUDENTTAKEN st " +
            "  WHERE st.StudentPerm = ? " +
            "    AND st.TCourseID = rc.CourseID2 " +
            "    AND st.Grade IS NOT NULL " +
            "    AND LOWER(st.Grade) < 'c' " +  
            ")";
        try (PreparedStatement prereqStmt = conn.prepareStatement(prereqCheckSql)) {
            prereqStmt.setString(1, courseID);
            prereqStmt.setString(2, perm);
            ResultSet rs = prereqStmt.executeQuery();
            if (rs.next()) {
                String missingPrereq = rs.getString("CourseID2");
                System.out.println("Cannot enroll: Missing prerequisite course: " + missingPrereq);
                return;
            }
        }

        String insertSql = "INSERT INTO ADMIN.TAKING (OfferingID, StudentPerm, CourseID) VALUES (?, ?, ?)";
        try (PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
            insertStmt.setInt(1, offeringID);
            insertStmt.setString(2, perm);
            insertStmt.setString(3, courseID);
            insertStmt.executeUpdate();
            System.out.println("Inserted Taking: " + perm + " -> " + courseID);
        }
    } finally {
        conn.close();
    }
}


public static void insertNewTerm(int year, String quarter) throws SQLException {
    OracleConnection conn = connectAsUser("ADMIN", "Somebodyplease2#");
    try {
        // Check if term already exists
        String checkSql = "SELECT COUNT(*) FROM Term WHERE TermYear = ? AND Quarter = ?";
        try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
            checkStmt.setInt(1, year);
            checkStmt.setString(2, quarter);
            ResultSet rs = checkStmt.executeQuery();
            rs.next();
            if (rs.getInt(1) > 0) {
                System.out.println("Term already exists: " + year + " " + quarter);
                return;
            }
        }


        String insertSql = "INSERT INTO Term (TermYear, Quarter) VALUES (?, ?)";
        try (PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
            insertStmt.setInt(1, year);
            insertStmt.setString(2, quarter);
            insertStmt.executeUpdate();
            System.out.println("Term inserted: " + year + " " + quarter);
        }
    } finally {
        conn.close();
    }
}

public static void insertNewLocation(String building, String room) throws SQLException {
    OracleConnection conn = connectAsUser("ADMIN", "Somebodyplease2#");
    try {

        String checkSql = "SELECT COUNT(*) FROM Locations WHERE Building = ? AND Room = ?";
        try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
            checkStmt.setString(1, building);
            checkStmt.setString(2, room);
            ResultSet rs = checkStmt.executeQuery();
            rs.next();
            if (rs.getInt(1) > 0) {
                System.out.println("Location already exists: " + building + " " + room);
                return;
            }
        }

        // Insert the new location
        String insertSql = "INSERT INTO Locations (Building, Room) VALUES (?, ?)";
        try (PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
            insertStmt.setString(1, building);
            insertStmt.setString(2, room);
            insertStmt.executeUpdate();
            System.out.println("Location inserted: " + building + " " + room);
        }

    } finally {
        conn.close();
    }
}

public static void insertReqMajor(String major, String courseID) throws SQLException {
    OracleConnection conn = connectAsUser("ADMIN", "Somebodyplease2#");
    try {
        String checkSql = "SELECT COUNT(*) FROM ReqMajor WHERE MajorName = ? AND CourseID = ?";
        try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
            checkStmt.setString(1, major);
            checkStmt.setString(2, courseID);
            ResultSet rs = checkStmt.executeQuery();
            rs.next();
            if (rs.getInt(1) > 0) {
                System.out.println("Already exists in ReqMajor: " + major + " requires " + courseID);
                return;
            }
        }

        String insertSql = "INSERT INTO ReqMajor (MajorName, CourseID) VALUES (?, ?)";
        try (PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
            insertStmt.setString(1, major);
            insertStmt.setString(2, courseID);
            insertStmt.executeUpdate();
            System.out.println("Inserted into ReqMajor: " + major + " requires " + courseID);
        }
    } finally {
        conn.close();
    }
}

public static void insertElectiveCourse(String major, String courseID) throws SQLException {
    OracleConnection conn = connectAsUser("ADMIN", "Somebodyplease2#");
    try {
        // Check if elective already exists
        String checkSql = "SELECT COUNT(*) FROM Electives WHERE MajorName = ? AND CourseID = ?";
        try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
            checkStmt.setString(1, major);
            checkStmt.setString(2, courseID);
            ResultSet rs = checkStmt.executeQuery();
            rs.next();
            if (rs.getInt(1) > 0) {
                System.out.println("Elective already exists: " + major + " -> " + courseID);
                return;
            }
        }

        // Insert elective
        String insertSql = "INSERT INTO Electives (MajorName, CourseID) VALUES (?, ?)";
        try (PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
            insertStmt.setString(1, major);
            insertStmt.setString(2, courseID);
            insertStmt.executeUpdate();
            System.out.println("Elective inserted: " + major + " -> " + courseID);
        }

    } finally {
        conn.close();
    }
}

public static void insertNewCourse(String courseID, String title, int enrollCode) throws SQLException {
    OracleConnection conn = connectAsUser("ADMIN", "Somebodyplease2#");
    try (Statement stmt = conn.createStatement()) {

        // Check if course already exists
        String checkSql = String.format("SELECT COUNT(*) FROM Courses WHERE CourseID = '%s'", courseID);
        ResultSet rs = stmt.executeQuery(checkSql);
        rs.next();
        if (rs.getInt(1) > 0) {
            System.out.println("Course already exists. Skipping insertion.");
            return;
        }

        String sql = String.format(
            "INSERT INTO Courses (CourseID, Title, enroll_code) VALUES ('%s', '%s', %d)",
            courseID, title, enrollCode
        );
        stmt.executeUpdate(sql);
        System.out.println("Course inserted: " + courseID);
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
