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

public class Studenttest3 {
    static final String DB_URL = "";
    static final String APP_USER = ""; // shared DB user
    static final String APP_PASSWORD = "";

     public static void main(String[] args) {
        String perm = "001421";
        String pin = "0000";
        
        
        try {
            //insertTakingCourse(32165, "37642", "1004");
            insertNewTerm(26, "S");
            insertNewTerm(26, "W");
            insertNewTerm(26, "F");
            //insertTakenCourse("CS026", "81934", 24, "F", "A");
            insertNewMajor("CS", "CS", 5);
            insertNewMajor("ECE", "ECE", 5);
            insertNewStudent("001421", "Stringname", "String", "CS", "CS", "0000");
            insertNewStudent("001422", "Stringname", "String", "ECE", "ECE", "0001");
            insertNewStudent("001423", "Stringname", "String", "CS", "CS", "0000");
            insertNewCourse("CS174", "CS174", "12345");
            insertNewCourse("CS170", "CS170", "54321");
            insertNewCourse("CS160", "CS160", "41725");
            insertNewCourse("CS026", "CS026", "76543");
            insertNewCourse("EC154", "EC154", "93156");
            insertNewCourse("EC140", "EC140", "19023");
            insertNewCourse("EC015", "EC015", "71631");
            insertNewCourse("CS154", "CS154", "32165");
            insertNewCourse("CS130", "CS130", "56789");
            insertNewCourse("EC152", "EC152", "91823");
            insertNewCourse("CS010", "CS010", "81623");
            insertNewCourse("EC010", "EC010", "82612");
            insertNewStudent("12345", "Alfred Hitchcock", "6667 El Colegio #40", "CS", "CS", "12345");
            insertNewStudent("14682", "Billy Clinton", "5777 Hollister", "ECE", "ECE", "14682");
            insertNewStudent("37642", "Cindy Laugher", "7000 Hollister", "CS", "CS", "37642");
            insertNewStudent("85821", "David Copperfill", "1357 State St", "CS", "CS", "85821");
            insertNewStudent("38567", "Elizabeth Sailor", "4321 State St", "ECE", "ECE", "38567");
            insertNewStudent("81934", "Fatal Castro", "3756 La Cumbre Plaza", "CS", "CS", "81934");
            insertNewStudent("98246", "George Brush", "5346 Foothill AvCS", "CS", "CS", "98246");
            insertNewStudent("35328", "Hurryson Ford", "678 State St", "ECE", "ECE", "35328");
            insertNewStudent("84713", "Ivan Lendme", "1235 Johnson Dr", "ECE", "ECE", "84713");
            insertNewStudent("36912", "Joe Pepsi", "3210 State St", "CS", "CS", "36912");
            insertNewStudent("46590", "Kelvin Coster", "Santa Cruz #3579", "CS", "CS", "46590");
            insertNewStudent("91734", "Li Kung", "2 People's Rd Beijing", "ECE", "ECE", "91734");
            insertNewStudent("73521", "Magic Jordon", "3852 Court Rd", "CS", "CS", "73521");
            insertNewStudent("53540", "Nam-hoi Chung", "1997 People's St HK", "CS", "CS", "53540");
            insertNewStudent("82452", "Olive Stoner", "6689 El Colegio #151", "ECE", "ECE", "82452");
            insertNewStudent("18221", "Pit Wilson", "911 State St", "ECE", "ECE", "18221");
            
            insertNewLocation("Psycho", "1132");
            insertNewLocation("English", "1124");
            insertNewLocation("Engr", "1132");
            insertNewLocation("Bio", "2222");
            insertNewLocation("Maths", "3333");
            insertNewLocation("Chem", "1234");
            insertNewLocation("Engr", "2116");
            insertNewLocation("Chem", "1111");
            insertNewLocation("Engr", "3163");
            insertNewLocation("Engr", "1124");
            insertNewLocation("Physics", "4004");
            insertNewLocation("Chem", "3333");
            insertNewCourseRequirement("CS174", "CS130");
            insertNewCourseRequirement("CS174", "CS026");
            insertNewCourseRequirement("CS170", "CS130");
            insertNewCourseRequirement("CS170", "CS154");
            insertNewCourseRequirement("CS160", "CS026");
            insertNewCourseRequirement("CS154", "CS026");
            insertNewCourseRequirement("EC154", "CS026");
            insertNewCourseRequirement("EC154", "EC152");
            insertNewTerm(25, "S");
            insertNewTerm(25, "W");
            insertNewTerm(24, "F");

            insertNewProfessor(1001, "Venus", "CS");
            insertNewProfessor(1002, "Jupiter", "CS");
            insertNewProfessor(1003, "Mercury", "CS");
            insertNewProfessor(1004, "Mars", "CS");
            insertNewProfessor(1005, "Saturn", "EC");
            insertNewProfessor(1006, "Gold", "EC");
            insertNewProfessor(1007, "Silver", "EC");
            insertNewProfessor(1008, "Copper", "CS");
            insertNewProfessor(1009, "Iron", "CS");
            insertNewProfessor(1010, "Tin", "CS");
            insertNewProfessor(1011, "Star", "CS");
            insertNewProfessor(1012, "Sun", "EC");
            insertNewProfessor(1013, "Moon", "EC");
            insertNewProfessor(1014, "Earth", "EC");
            String[] required = { "CS026", "CS130", "CS154", "CS160", "CS170" };
            for (String course : required) {
            insertReqMajor("CS", course);
            insertReqMajor("ECE", course);
            }
            String[] electives = { "CS010", "EC010", "EC015", "EC140", "EC152", "EC154", "CS174" };
            for (String course : electives) {
                insertElectiveCourse("CS", course);
                insertElectiveCourse("ECE", course);
            }
            insertTakenCourse("CS154", "12345", 25, "W", "A");
            insertTakenCourse("CS130", "12345", 25, "W", "B");
            insertTakenCourse("EC154", "12345", 25, "W", "C");
            insertTakenCourse("CS026", "12345", 24, "F", "A");
            insertTakenCourse("CS010", "12345", 24, "F", "A");

            insertTakenCourse("CS160", "14682", 25, "W", "B");
            insertTakenCourse("CS130", "14682", 25, "W", "B");
            insertTakenCourse("CS026", "14682", 24, "F", "B");
            insertTakenCourse("CS010", "14682", 24, "F", "A");

            insertTakenCourse("EC152", "37642", 25, "W", "C");
            insertTakenCourse("CS130", "37642", 25, "W", "B");
            insertTakenCourse("EC015", "37642", 24, "F", "B");
            insertTakenCourse("EC010", "37642", 24, "F", "A");

            insertTakenCourse("CS130", "85821", 25, "W", "C");
            insertTakenCourse("CS026", "85821", 25, "W", "A");
            insertTakenCourse("CS010", "85821", 24, "F", "A");
            insertTakenCourse("EC015", "85821", 24, "F", "B");

            insertTakenCourse("EC154", "38567", 25, "W", "C");
            insertTakenCourse("CS130", "38567", 25, "W", "A");
            insertTakenCourse("EC152", "38567", 24, "F", "B");
            insertTakenCourse("CS154", "38567", 24, "F", "B");

            insertTakenCourse("CS154", "81934", 25, "W", "C");
            insertTakenCourse("CS130", "81934", 25, "W", "A");
            insertTakenCourse("CS026", "81934", 24, "F", "A");
            insertTakenCourse("EC152", "81934", 24, "F", "B");

            insertTakenCourse("EC152", "98246", 25, "W", "B");
            insertTakenCourse("CS154", "98246", 24, "F", "A");
            insertTakenCourse("CS130", "98246", 24, "F", "B");
            insertTakenCourse("CS026", "98246", 24, "F", "A");

            insertTakenCourse("CS130", "35328", 24, "F", "B");
            insertTakenCourse("CS026", "35328", 24, "F", "A");

            insertTakenCourse("CS026", "84713", 25, "W", "D");
            insertTakenCourse("EC015", "84713", 24, "F", "F");
            insertTakenCourse("CS010", "84713", 24, "F", "C");

            insertTakenCourse("CS026", "46590", 25, "W", "A");

            insertTakenCourse("CS026", "91734", 25, "W", "A");

            insertTakenCourse("CS026", "73521", 25, "W", "B");

            insertTakenCourse("CS154", "53540", 25, "W", "C");
            insertTakenCourse("CS130", "53540", 25, "W", "C");

            insertTakenCourse("EC152", "82452", 25, "W", "C");
            insertTakenCourse("CS026", "82452", 25, "W", "C");

            insertTakenCourse("CS130", "18221", 25, "W", "B");
            insertTakenCourse("CS026", "18221", 25, "W", "B");
            insertCourseOffering("CS174", "S");
            insertCourseOffering("CS170", "W");
            insertCourseOffering("CS170", "S");
            insertCourseOffering("CS170", "F");
            insertCourseOffering("CS160", "F");
            insertCourseOffering("CS160", "W");
            insertCourseOffering("CS160", "S");
            insertCourseOffering("CS026", "F");
            insertCourseOffering("CS026", "W");
            insertCourseOffering("CS026", "S");
            insertCourseOffering("EC154", "F");
            insertCourseOffering("EC154", "W");
            insertCourseOffering("EC154", "S");
            insertCourseOffering("EC140", "S");
            insertCourseOffering("EC015", "F");
            insertCourseOffering("EC015", "S");
            insertCourseOffering("CS154", "F");
            insertCourseOffering("CS154", "W");
            insertCourseOffering("CS130", "F");
            insertCourseOffering("CS130", "W");
            insertCourseOffering("EC152", "F");
            insertCourseOffering("EC152", "W");
            insertCourseOffering("CS010", "F");
            insertCourseOffering("EC010", "F");
            int i = 0;
            insertOffering(i++, "CS174", 1001, "Psycho", "1132", "S", 25, "TR", 8, "10-12");
            insertOffering(i++, "CS170", 1002, "English", "1124", "S", 25, "MWF", 8, "10-11");
            insertOffering(i++, "CS160", 1003, "Engr", "1132", "S", 25, "MWF", 8, "2-3");
            insertOffering(i++, "CS026", 1004, "Bio", "2222", "S", 25, "MWF", 8, "2-3");
            insertOffering(i++, "EC154", 1005, "Maths", "3333", "S", 25, "T", 7, "3-5");
            insertOffering(i++, "EC140", 1006, "Chem", "1234", "S", 25, "TR", 10, "1-3");
            insertOffering(i++, "EC015", 1007, "Engr", "2116", "S", 25, "MW", 8, "11-1");
            
            insertOffering(i++, "CS170", 1008, "English", "1124", "W", 25, "MWF", 18, "10-11");
            insertOffering(i++, "CS160", 1009, "Engr", "1132", "W", 25, "MWF", 15, "2-3");
            insertOffering(i++, "CS154", 1010, "Engr", "2116", "W", 25, "MF", 10, "8-9");
            insertOffering(i++, "CS130", 1011, "Chem", "1111", "W", 25, "TR", 15, "2-4");
            insertOffering(i++, "CS026", 1010, "Bio", "2222", "W", 25, "MWF", 15, "2-3");
            insertOffering(i++, "EC154", 1005, "Maths", "3333", "W", 25, "T", 18, "3-5");
            insertOffering(i++, "EC152", 1006, "Engr", "3163", "W", 25, "MW", 10, "11-1");

            insertOffering(i++, "CS170", 1008, "English", "1124", "F", 24, "MWF", 15, "10-11");
            insertOffering(i++, "CS160", 1003, "Engr", "1132", "F", 24, "MWF", 10, "2-3");
            insertOffering(i++, "CS154", 1004, "Engr", "2116", "F", 24, "MWF", 10, "8-9");
            insertOffering(i++, "CS130", 1002, "Chem", "1111", "F", 24, "TR", 15, "2-4");
            insertOffering(i++, "CS026", 1010, "Bio", "2222", "F", 24, "MWF", 15, "2-3");
            insertOffering(i++, "CS010", 1006, "Chem", "3333", "F", 24, "MWR", 10, "3-4");
            insertOffering(i++, "EC154", 1007, "Maths", "3333", "F", 24, "T", 10, "3-5");
            insertOffering(i++, "EC152", 1012, "Engr", "3163", "F", 24, "MW", 10, "11-1");
            insertOffering(i++, "EC015", 1013, "Engr", "1124", "F", 24, "TR", 15, "2-4");
            insertOffering(i++, "EC010", 1014, "Physics", "4004", "F", 24, "MWF", 15, "8-9");
            insertTakingCourse(1, "12345", "CS170");
            insertTakingCourse(2, "12345", "CS160");

            insertTakingCourse(4, "37642", "EC154");
            insertTakingCourse(2, "37642", "CS160");

            insertTakingCourse(0, "85821", "CS174");
            insertTakingCourse(2, "85821", "CS160");

            insertTakingCourse(0, "38567", "CS174");
            insertTakingCourse(1, "38567", "CS170");
            insertTakingCourse(2, "38567", "CS160");

            insertTakingCourse(4, "81934", "EC154");

            insertTakingCourse(2, "98246", "CS160");
            insertTakingCourse(0, "98246", "CS174");
            insertTakingCourse(1, "98246", "CS170");
            insertTakingCourse(4, "98246", "EC154");

            insertTakingCourse(0, "35328", "CS174");

            insertTakingCourse(1, "53540", "CS170");

            insertTakingCourse(4, "82452", "EC154");

            insertTakingCourse(0, "18221", "CS174");

            if (authenticateStudent(perm, pin)) {
                showStudentInfo(perm);

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
    public static void insertCourseOffering(String courseID, String quarter) throws SQLException {
    OracleConnection conn = connectAsUser("ADMIN", "Somebodyplease2#");
    try {

        String checkSql = "SELECT COUNT(*) FROM QuarterOffered WHERE CourseID = ? AND Quarter = ?";
        try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
            checkStmt.setString(1, courseID);
            checkStmt.setString(2, quarter);
            ResultSet rs = checkStmt.executeQuery();
            rs.next();
            if (rs.getInt(1) > 0) {
                System.out.printf("Offering already exists: %s in %s%n", courseID, quarter);
                return;
            }
        }

   
        String insertSql = "INSERT INTO QuarterOffered (CourseID, Quarter) VALUES (?, ?)";
        try (PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
            insertStmt.setString(1, courseID);
            insertStmt.setString(2, quarter);
            insertStmt.executeUpdate();
            System.out.printf("Inserted: %s offered in %s%n", courseID, quarter);
        }

    } finally {
        conn.close();
    }
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
        // Check if student already exists
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
public static void insertOffering( int offeringID, String courseID, int profID, String building,String room, String quarter, int year,String day, int capacity,String timeslot
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
public static void insertTakingCourse(int offeringID, String perm, String courseID) throws SQLException {
    OracleConnection conn = connectAsUser("ADMIN", "Somebodyplease2#");
    try {
        String checkSql = "SELECT COUNT(*) FROM Taking WHERE OfferingID = ? AND StudentPerm = ? AND CourseID = ?";
        try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
            checkStmt.setInt(1, offeringID);
            checkStmt.setString(2, perm);
            checkStmt.setString(3, courseID);
            ResultSet rs = checkStmt.executeQuery();
            rs.next();
            if (rs.getInt(1) > 0) {
                System.out.println("Already enrolled: " + perm + " in " + courseID);
                return;
            }
        }

        String insertSql = "INSERT INTO Taking (OfferingID, StudentPerm, CourseID) VALUES (?, ?, ?)";
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

public static void insertNewCourse(String courseID, String title, String enrollCode) throws SQLException {
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
            "INSERT INTO Courses (CourseID, Title, enroll_code) VALUES ('%s', '%s', %s)",
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

