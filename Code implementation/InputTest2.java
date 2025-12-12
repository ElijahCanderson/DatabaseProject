// Java code that uses a shared database user and enforces per-student access control

package com.example;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Scanner;
import java.util.Set;

import oracle.jdbc.OracleConnection;
import oracle.jdbc.pool.OracleDataSource;
//import sun.jvm.hotspot.code.SafepointBlob;

public class InputTest2 {
    static final String DB_URL = "jdbc:oracle:thin:@cs174adb_tp?TNS_ADMIN=C:/Users/eli/Downloads/project/demo/wallet";
    static final String APP_USER = ""; // shared DB user
    static final String APP_PASSWORD = "";
    static String studentPerm = null;
    static String CurrentQuarter = "S";
    static int CurrentYear = 25;

public static void main(String[] args) {

    try (Scanner scanner = new Scanner(System.in)) {
            System.out.print("Regent or Student? : ");
            String Usertype = scanner.nextLine().trim();
            Connection conn = null;
            String inputPerm = "-1";
            String inputPin = "-1";
            String inputpass = "";
            boolean isStudent = false;
            boolean isRegent = false;
            if("student".equalsIgnoreCase(Usertype)){
                conn = connectAsAppUser();
            while (studentPerm == null) {
                System.out.print("Enter perm: ");
                inputPerm = scanner.nextLine().trim();

                System.out.print("Enter pin: ");
                inputPin = scanner.nextLine().trim();

                if (VerifyPin(inputPerm, inputPin, conn)) {
                    studentPerm = inputPerm;
                    isStudent = true;
                    String studentname = GetName(conn, inputPerm);
                    System.out.print("Welcome " + studentname + " \n Commands Are:   \n VIEW, \n ADD, \n DROP, \n TAKING, \n GRADES, \n REQUIREMENTS, \n SETPIN , \n PLAN \n");
                } else {
                    System.out.println("Invalid perm or pin. Try again.");
                }
            }}
            
            else if ("regent".equalsIgnoreCase(Usertype)) {
            while (isRegent == false) {
                System.out.print("Regent Password: ");
                inputpass = scanner.nextLine().trim();
                conn = connectAsUser("regent", inputpass);
                if(conn != null){
                        isRegent = true;
                        System.out.print("Welcome Regent \n commands are  \n VIEW, \n ADD, \n DROP, \n CLASS_LIST, \n TRANSCRIPT, \n GRADE_MAILER, \n ENTER_GRADES \n");
                }
            } }
            else {
                System.out.println("This login is not currently supported.");
                return;
            }

            while (true) {
                System.out.print(">> ");
                String command = scanner.nextLine().trim();

                if (command.equalsIgnoreCase("EXIT")) {
                    System.out.println("Goodbye!");
                    break;
                }
                
                // commands to run if you are a student
                else if(isStudent) {
                    if (command.equalsIgnoreCase("VIEW")) {
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
                        AddCourse(offeringID, inputPerm, conn, CurrentQuarter, CurrentYear);
                    }

                    else if(command.equalsIgnoreCase(("DROP"))){
                        displayTakingCourse(inputPerm, conn);
                        System.out.print("Enter Offering ID: ");
                        int offeringID = Integer.parseInt(scanner.nextLine().trim());
                        System.out.print("Enter Course ID: ");
                        String courseID = scanner.nextLine().trim().toUpperCase();
                        dropTakingCourse(inputPerm, courseID, conn);
                    }
                    else if(command.equalsIgnoreCase(("Taking"))){
                        displayTakingCourse(inputPerm, conn);
                    }
                    else if(command.equalsIgnoreCase(("Grades"))){
                        System.out.print("Enter current quarter (e.g., F, W, S, or Any): ");
                        String quarter = scanner.nextLine().trim().toUpperCase();
                        System.out.print("Enter current year (e.g., 24, 25, or 0 (0 = Any): ");
                        int year = Integer.parseInt(scanner.nextLine().trim());
                        showTakenGrades(conn, year, quarter, inputPerm);
                    }
                    else if(command.equalsIgnoreCase(("Requirements"))){
                        checkStudentRequirements(inputPerm, conn);
                    }
                    else if(command.equalsIgnoreCase("setPin")){
                        System.out.print("Enter Current Pin: ");
                        String curpin = scanner.nextLine().trim();
                        System.out.print("Enter NewPin: ");
                        String newpin = scanner.nextLine().trim();
                        if(SetPin(inputPerm, curpin, newpin, conn)){
                            System.out.print("Success \n");
                        }
                    }
                    else if(command.equalsIgnoreCase(("Plan"))){
                        displayGraduationPlan(inputPerm, CurrentYear, CurrentQuarter, conn);
                    }
                    else {
                        System.out.println("Unknown command.");
                    }

                // commands to run if you work for the registrar
                } else if(isRegent) { 
                    if (command.equalsIgnoreCase("VIEW")) {
                        System.out.print("Enter current quarter (e.g., F, W, S, or Any): ");
                        String quarter = scanner.nextLine().trim().toUpperCase();
                        System.out.print("Enter current year (e.g., 24, 25, or 0 (0 = Any): ");
                        int year = Integer.parseInt(scanner.nextLine().trim());
                        showAvailableOfferings(conn, year, quarter);
                    }

                    else if (command.equalsIgnoreCase("ADD")) {
                        showAvailableOfferings(conn, CurrentYear, CurrentQuarter);
                        System.out.print("Enter student perm: ");
                    
                        String studentPerm = scanner.nextLine().trim();
                        System.out.print("Enter Offering ID: ");
                        int offeringID = Integer.parseInt(scanner.nextLine().trim());
                        AddCourse(offeringID, studentPerm, conn, CurrentQuarter, CurrentYear);
                    } 
                    
                    else if (command.equalsIgnoreCase("DROP")) {
                        System.out.print("Enter student perm: ");
                        String studentPerm = scanner.nextLine().trim();
                        displayTakingCourse(studentPerm, conn);
                        System.out.print("Enter Course ID: ");
                        String courseID = scanner.nextLine().trim().toUpperCase();
                        dropTakingCourse(studentPerm, courseID, conn);
                    } 

                    else if (command.equalsIgnoreCase("CLASS_LIST")) {
                        showAvailableOfferings(conn, CurrentYear, CurrentQuarter);
                        System.out.print("Enter CourseID: ");
                        String CourseID = (scanner.nextLine().trim());
                        listStudentsInCourse(CourseID, conn);
                    } 
                    
                    else if (command.equalsIgnoreCase("TRANSCRIPT")) {
                        System.out.print("Enter student perm: ");
                        String studentPerm = scanner.nextLine().trim();
                        generateTranscript(studentPerm, conn);
                    } 

                    else if (command.equalsIgnoreCase("GRADE_MAILER")) {
                        System.out.print("Enter quarter of choice (e.g., F, W, S, or Any): ");
                        String quarter = scanner.nextLine().trim().toUpperCase();

                        System.out.print("Enter year of choice (e.g., 24, 25, or 0 for Any): ");
                        int year = Integer.parseInt(scanner.nextLine().trim());

                        System.out.print("Enter student perm (or leave blank for all): ");
                        String studentPerm = scanner.nextLine().trim();
                        if (studentPerm.isEmpty()) {
                            studentPerm = null;
                        }
                        System.out.print("Enter course ID (or leave blank for all): ");
                        String courseid = scanner.nextLine().trim();
                        if (courseid.isEmpty()) {
                            courseid = null;
                        }
                        requestGradeMailer(conn, year, quarter, studentPerm, courseid);
                    }
                    
                    else if (command.equalsIgnoreCase("ENTER_GRADES")) {
                        System.out.print("Enter Course ID (e.g., CS130): ");
                        String courseID = scanner.nextLine().trim().toUpperCase();

                        System.out.print("Enter Year (e.g., 24, 25): ");
                        int year = Integer.parseInt(scanner.nextLine().trim());

                        System.out.print("Enter Quarter (e.g., F, W, S): ");
                        String quarter = scanner.nextLine().trim().toUpperCase();

                        System.out.print("Enter file name (e.g., grades.txt): ");
                        String fileName = scanner.nextLine().trim();

                        enterGrades(courseID, year, quarter, fileName, conn);
                    }
            
                    
                    else {
                        System.out.println("Unknown Regent command.");
                    }

                }
                
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
}

//public static boolean authenticateStudent(String perm, String pin) throws SQLException {
 //   return VerifyPin(perm, pin);    
//}

public static void displayGraduationPlan(String perm, int currentYear, String currentQuarter, Connection conn) throws SQLException {
    String major = null;
    int numElectivesRequired = 0;
    int electivesAlreadyCompleted = 0;


    try (PreparedStatement stmt = conn.prepareStatement("SELECT Major FROM ADMIN.STUDENTINFO WHERE perm = ?")) {
        stmt.setString(1, perm);
        ResultSet rs = stmt.executeQuery();
        if (rs.next()) major = rs.getString("Major");
        else {
            System.out.println("Student not found.");
            return;
        }
    }

    try (PreparedStatement stmt = conn.prepareStatement("SELECT num_elect FROM ADMIN.STUDENTMAJORS WHERE MajorName = ?")) {
        stmt.setString(1, major);
        ResultSet rs = stmt.executeQuery();
        if (rs.next()) numElectivesRequired = rs.getInt(1);
    }

    Set<String> completed = new HashSet<>();
    try (PreparedStatement stmt = conn.prepareStatement(
        "SELECT TCourseID FROM ADMIN.STUDENTTAKEN WHERE StudentPerm = ? AND grade IN ('A','A-','B+','B','B-','C+','C') " +
        "UNION SELECT CourseID FROM ADMIN.STUDENTTAKING WHERE StudentPerm = ?")) {
        stmt.setString(1, perm);
        stmt.setString(2, perm);
        ResultSet rs = stmt.executeQuery();
        while (rs.next()) completed.add(rs.getString(1));
    }

 
    try (PreparedStatement stmt = conn.prepareStatement(
        "SELECT CourseID FROM ADMIN.STUDENTELECTIVES WHERE MajorName = ?")) {
        stmt.setString(1, major);
        ResultSet rs = stmt.executeQuery();
        while (rs.next()) {
            String cid = rs.getString("CourseID");
            if (completed.contains(cid)) electivesAlreadyCompleted++;
        }
    }

    int electivesLeftToSchedule = Math.max(0, numElectivesRequired - electivesAlreadyCompleted);


    Set<String> requiredCourses = new HashSet<>();
    Set<String> electiveCourses = new HashSet<>();
    Map<String, String> courseTitles = new HashMap<>();

    try (PreparedStatement stmt = conn.prepareStatement(
        "SELECT CourseID, Title FROM ADMIN.STUDENTCOURSES WHERE CourseID IN (" +
        "SELECT CourseID FROM ADMIN.STUDENTREQMAJOR WHERE MajorName = ? " +
        "UNION SELECT CourseID FROM ADMIN.STUDENTELECTIVES WHERE MajorName = ?)")) {
        stmt.setString(1, major);
        stmt.setString(2, major);
        ResultSet rs = stmt.executeQuery();
        while (rs.next()) courseTitles.put(rs.getString("CourseID"), rs.getString("Title"));
    }

    try (PreparedStatement stmt = conn.prepareStatement("SELECT CourseID FROM ADMIN.STUDENTREQMAJOR WHERE MajorName = ?")) {
        stmt.setString(1, major);
        ResultSet rs = stmt.executeQuery();
        while (rs.next()) {
            String cid = rs.getString("CourseID");
            if (!completed.contains(cid)) requiredCourses.add(cid);
        }
    }

    try (PreparedStatement stmt = conn.prepareStatement("SELECT CourseID FROM ADMIN.STUDENTELECTIVES WHERE MajorName = ?")) {
        stmt.setString(1, major);
        ResultSet rs = stmt.executeQuery();
        while (rs.next()) {
            String cid = rs.getString("CourseID");
            if (!completed.contains(cid)) electiveCourses.add(cid);
        }
    }

    Set<String> remaining = new LinkedHashSet<>();
    remaining.addAll(requiredCourses);
    remaining.addAll(electiveCourses);


    Map<String, List<String>> prereqMap = new HashMap<>();
    for (String course : remaining) prereqMap.put(course, new ArrayList<>());
    try (PreparedStatement stmt = conn.prepareStatement("SELECT CourseID1, CourseID2 FROM ADMIN.REQCOURSES")) {
        ResultSet rs = stmt.executeQuery();
        while (rs.next()) {
            String course = rs.getString("CourseID1");
            String prereq = rs.getString("CourseID2");
            if (remaining.contains(course)) {
                prereqMap.get(course).add(prereq);
            }
        }
    }

    Map<String, Set<String>> courseToQuarters = new HashMap<>();
    try (PreparedStatement stmt = conn.prepareStatement("SELECT CourseID, Quarter FROM ADMIN.QuarterOffered")) {
        ResultSet rs = stmt.executeQuery();
        while (rs.next()) {
            String cid = rs.getString("CourseID");
            String qtr = rs.getString("Quarter");
            courseToQuarters.computeIfAbsent(cid, k -> new HashSet<>()).add(qtr);
        }
    }

    String[] quarters = {"W", "S", "F"};
    Map<String, Integer> quarterIndexMap = Map.of("W", 0, "S", 1, "F", 2);
    int year = currentYear;
    int quarterIndex = quarterIndexMap.getOrDefault(currentQuarter.toUpperCase(), 0);

    System.out.printf("\n%-8s %-8s %-10s %-30s%n", "Year", "Quarter", "CourseID", "Course Title");

    Set<String> scheduled = new HashSet<>();
    List<String> sortedRemaining = new ArrayList<>(remaining);

    while (scheduled.size() < requiredCourses.size() + Math.min(electivesLeftToSchedule, electiveCourses.size())) {
        int added = 0;
        List<String> toScheduleNow = new ArrayList<>();
        String term = quarters[quarterIndex];

        for (String course : sortedRemaining) {
            if (scheduled.contains(course)) continue;
            if (!courseToQuarters.getOrDefault(course, Set.of()).contains(term)) continue;

            for (String pre : prereqMap.getOrDefault(course, List.of())) {
                if (!completed.contains(pre)) continue;
            }

            if (electiveCourses.contains(course) && electivesLeftToSchedule <= 0) continue;

            toScheduleNow.add(course);
        }

        for (String course : toScheduleNow) {
            if (added >= 5) break;
            scheduled.add(course);
            completed.add(course);
            if (electiveCourses.contains(course)) electivesLeftToSchedule--;
            System.out.printf("%-8d %-8s %-10s %-30s%n", year, term, course, courseTitles.get(course));
            added++;
        }

        quarterIndex = (quarterIndex + 1) % 3;
        if (quarterIndex == 0) year++;
    }

    if (scheduled.isEmpty()) {
        System.out.println("Courses are ALready completed");
    }
}








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

private static String GetName(Connection conn, String perm) {
    String sql = "SELECT Name FROM ADMIN.STUDENTINFO WHERE perm = ?";
    try (PreparedStatement stmt = conn.prepareStatement(sql)) {
        stmt.setString(1, perm);
        try (ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                return rs.getString("Name");
            }
        }
    } catch (SQLException e) {
        System.err.println("Error fetching name: " + e.getMessage());
    }
    return null; 
}



private static void showTakenGrades(Connection conn, int year, String quarter, String perm) {
    StringBuilder sql = new StringBuilder("SELECT * FROM ADMIN.STUDENTTAKEN t WHERE t.StudentPerm = ?");

    boolean includeQuarter = quarter != null && !quarter.equalsIgnoreCase("Any");
    boolean includeYear = year != 0;

    if (includeQuarter) {
        sql.append(" AND Quarter = ?");
    }
    if (includeYear) {
        sql.append(" AND Year = ?");
    }
        sql.append(" ORDER BY t.TakenYear DESC, CASE t.Quarter " +
                "WHEN 'S' THEN 1 " +
                "WHEN 'W' THEN 2 " +
                "WHEN 'F' THEN 3 " +
                "ELSE 4 END");
    try (PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
        stmt.setString(1, perm);
        int paramIndex = 1;
        if (includeQuarter) {
            stmt.setString(paramIndex++, quarter);
        }
        if (includeYear) {
            stmt.setInt(paramIndex, year);
        }


        ResultSet rs = stmt.executeQuery();

        System.out.printf("%-10s %-8s %-20s %-10s %-8s %-5s%n",
            "Perm", "Course", "Title", "Year", "Quarter", "Grade");

        while (rs.next()) {
            System.out.printf("%-10s %-8s %-20s %-10d %-8s %-5s%n",
                rs.getString("StudentPerm"),
                rs.getString("TCourseID"),
                rs.getString("CourseTitle"),
                rs.getInt("TakenYear"),
                rs.getString("Quarter"),
                rs.getString("Grade"));
        }


    } catch (SQLException e) {
        System.err.println("Failed to query view: " + e.getMessage());
    }
}




private static void showAvailableOfferings(Connection conn, int year, String quarter) {
    StringBuilder sql = new StringBuilder(
        "SELECT * FROM ADMIN.STUDENTOFFERING WHERE 1=1");

    boolean includeQuarter = quarter != null && !quarter.equalsIgnoreCase("Any");
    boolean includeYear = year != 0;

    if (includeQuarter) {
        sql.append(" AND Quarter = ?");
    }
    if (includeYear) {
        sql.append(" AND Year = ?");
    }

    // Add ORDER BY year, then by quarter in logical order
    sql.append(" ORDER BY Year, CASE Quarter ")
       .append("WHEN 'W' THEN 1 ")
       .append("WHEN 'S' THEN 2 ")
       .append("WHEN 'F' THEN 3 ")
       .append("ELSE 4 END");

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





private static void AddCourse(int offeringID, String perm, Connection conn, String CurrentQuarter, int CurrentYear) {
    try (PreparedStatement stmt = conn.prepareStatement("SELECT * FROM ADMIN.STUDENTOFFERING WHERE OfferingID = ? and quarter = ? and year = ?"))
        {
        stmt.setInt(1, offeringID);
        stmt.setString(2, CurrentQuarter);
        stmt.setInt(3, CurrentYear);
        ResultSet rs = stmt.executeQuery();
         if (rs.next()) {
    
            String courseID = rs.getString("CourseID");
            String quarter = rs.getString("Quarter");
            int year = rs.getInt("Year");
            if(quarter.equalsIgnoreCase(CurrentQuarter) && year == CurrentYear){
           insertTakingCourse(offeringID, perm, courseID, conn);}
           else{
            System.out.println("Offering not in current Quarter");
           }

        } else {
            System.out.println("No course found with OfferingID: " + offeringID);
        }
        }

     catch (SQLException e) {
        System.out.println("Failed to query view: " + e.getMessage());
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

public static boolean SetPin(String perm, String pin, String newpin, Connection conn) throws SQLException {
    if (newpin == null || !newpin.matches("\\d{5}")) {
        System.out.println("Invalid new PIN. It must be a 5-digit number.");
        return false;
    }
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


public static boolean checkStudentRequirements(String perm, Connection conn) throws SQLException {
    // Step 1: Get student's major
    String majorQuery = "SELECT Major FROM ADMIN.STUDENTINFO WHERE perm = ?";
    String majorID = null;
    try (PreparedStatement stmt = conn.prepareStatement(majorQuery)) {
        stmt.setString(1, perm);
        ResultSet rs = stmt.executeQuery();
        if (rs.next()) {
            majorID = rs.getString("Major");
        } else {
            System.out.println("Student not found.");
            return false;
        }
    }

    // Step 2: Get counts of requirements met (with grade > C)
        String sql =
            "SELECT m.num_elect, " +
            "       (SELECT COUNT(*) FROM ADMIN.STUDENTREQMAJOR rm " +
            "        WHERE rm.MajorName = m.MajorName) AS NumCoreRequired, " +
            "       (SELECT COUNT(*) FROM ADMIN.STUDENTREQMAJOR rm " +
            "        WHERE rm.MajorName = m.MajorName AND rm.CourseID IN ( " +
            "            SELECT TCourseID FROM ADMIN.STUDENTTAKEN " +
            "            WHERE StudentPerm = ? AND grade IN ('A', 'A-', 'B+', 'B', 'B-', 'C+', 'C') " +
            "            UNION " +
            "            SELECT CourseID FROM ADMIN.STUDENTTAKING " +
            "            WHERE StudentPerm = ? " +
            "        )) AS NumCoreTaken, " +
            "       (SELECT COUNT(*) FROM ADMIN.STUDENTELECTIVES e " +
            "        WHERE e.MajorName = m.MajorName AND e.CourseID IN ( " +
            "            SELECT TCourseID FROM ADMIN.STUDENTTAKEN " +
            "            WHERE StudentPerm = ? AND grade IN ('A', 'A-', 'B+', 'B', 'B-', 'C+', 'C') " +
            "            UNION " +
            "            SELECT CourseID FROM ADMIN.STUDENTTAKING " +
            "            WHERE StudentPerm = ? " +
            "        )) AS NumElectivesTaken " +
            "FROM ADMIN.STUDENTMAJORS m WHERE m.MajorName = ?";

    int numElect = 0, coreRequired = 0, coreTaken = 0, electTaken = 0;

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, perm);     // for core taken
            stmt.setString(2, perm);     // for core taking
            stmt.setString(3, perm);     // for electives taken
            stmt.setString(4, perm);     // for electives taking
            stmt.setString(5, majorID);  // for main WHERE clause
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                numElect = rs.getInt("num_elect");
                coreRequired = rs.getInt("NumCoreRequired");
                coreTaken = rs.getInt("NumCoreTaken");
                electTaken = rs.getInt("NumElectivesTaken");
            }
        }


    // Step 3: Determine if requirements are satisfied
    boolean coreOk = (coreTaken >= coreRequired);
    boolean electOk = (electTaken >= numElect);

    if (coreOk && electOk) {
        System.out.println("Requirements Complete");
        return true;
    }

    System.out.println("Missing requirements for student " + perm + " in major " + majorID);

    // Step 4: Show missing core courses (not passed with grade > C)
if (!coreOk) {
    System.out.println("Missing core courses:");
    String coreMissingSql =
        "SELECT rm.CourseID, c.Title " +
        "FROM ADMIN.STUDENTREQMAJOR rm " +
        "JOIN ADMIN.STUDENTCOURSES c ON rm.CourseID = c.CourseID " +
        "WHERE rm.MajorName = ? AND rm.CourseID NOT IN ( " +
        "    SELECT TCourseID FROM ADMIN.STUDENTTAKEN " +
        "    WHERE StudentPerm = ? AND grade IN ('A', 'A-', 'B+', 'B', 'B-', 'C+', 'C') " +
        "    UNION " +
        "    SELECT CourseID FROM ADMIN.STUDENTTAKING " +
        "    WHERE StudentPerm = ? " +
        ")";

    try (PreparedStatement stmt = conn.prepareStatement(coreMissingSql)) {
        stmt.setString(1, majorID);
        stmt.setString(2, perm); // for STUDENTTAKEN
        stmt.setString(3, perm); // for STUDENTTAKING
        ResultSet rs = stmt.executeQuery();
        while (rs.next()) {
            System.out.println("- " + rs.getString("CourseID") + ": " + rs.getString("Title"));
        }
    }
}

    // Step 5: Show how many electives are still needed
    if (!electOk) {
        System.out.printf("Missing %d elective(s).\n", numElect - electTaken);
    }

    return false;
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

public static void dropTakingCourse(String perm, String courseID, Connection conn) throws SQLException {
    String sql = "DELETE FROM ADMIN.TAKING WHERE StudentPerm = ? AND CourseID = ?";
    try (PreparedStatement stmt = conn.prepareStatement(sql)) {
        stmt.setString(1, perm);
        stmt.setString(2, courseID);

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

public static OracleConnection connectAsUser(String user, String password) {
    try {
        Properties info = new Properties();
        info.put(OracleConnection.CONNECTION_PROPERTY_USER_NAME, user);
        info.put(OracleConnection.CONNECTION_PROPERTY_PASSWORD, password);
        info.put(OracleConnection.CONNECTION_PROPERTY_DEFAULT_ROW_PREFETCH, "20");

        // Set Oracle Wallet path
        System.setProperty("oracle.net.tns_admin", "C:/Users/eli/Downloads/project/demo/wallet");

        OracleDataSource ods = new OracleDataSource();
        ods.setURL("jdbc:oracle:thin:@cs174adb_tp");
        ods.setConnectionProperties(info);

        OracleConnection conn = (OracleConnection) ods.getConnection();
        System.out.println("Connection successful.");
        return conn;
    } catch (SQLException e) {
        System.out.println("Failed to connect as user '" + user);
        return null;
    }
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

public static void listStudentsInCourse(String CourseID, Connection conn) {
    String query =
        "SELECT s.perm, s.name " +
        "FROM ADMIN.STUDENTTAKING e " +
        "JOIN ADMIN.STUDENTINFO s ON e.studentperm = s.perm " +
        "WHERE e.CourseID = ? " +
        "ORDER BY s.name";

    try (PreparedStatement stmt = conn.prepareStatement(query)) {
        stmt.setString(1, CourseID);
        ResultSet rs = stmt.executeQuery();

        boolean hasResults = false;
        System.out.println("Class List for Offering ID: " + CourseID);

        while (rs.next()) {
            hasResults = true;
            String perm = rs.getString("perm");
            String name = rs.getString("name");
            System.out.println(perm + " - " + name);
        }

        if (!hasResults) {
            System.out.println("No students enrolled in this course.");
        }

    } catch (SQLException e) {
        System.out.println("Error retrieving class list.");
        e.printStackTrace();
    }
}

public static void generateTranscript(String studentPerm, Connection conn) {
    // Step 0: Check if student exists
    String checkStudentSql = "SELECT 1 FROM ADMIN.STUDENTINFO WHERE perm = ?";
    try (PreparedStatement checkStmt = conn.prepareStatement(checkStudentSql)) {
        checkStmt.setString(1, studentPerm);
        ResultSet rs = checkStmt.executeQuery();
        if (!rs.next()) {
            System.out.println("Student with perm " + studentPerm + " does not exist.");
            return;
        }
    } catch (SQLException e) {
        System.out.println("Error checking student existence.");
        e.printStackTrace();
        return;
    }

    // Step 1: Generate transcript if student exists
    String query =
        "SELECT e.TAKENYEAR, e.quarter, e.Tcourseid, e.grade " +
        "FROM ADMIN.STUDENTTAKEN e " +
        "WHERE e.studentperm = ? AND e.grade IS NOT NULL " +
        "ORDER BY e.TAKENYEAR, " +
        "         CASE e.quarter " +
        "            WHEN 'F' THEN 3 " +
        "            WHEN 'W' THEN 1 " +
        "            WHEN 'S' THEN 2 " +
        "         END";

    try (PreparedStatement stmt = conn.prepareStatement(query)) {
        stmt.setString(1, studentPerm);
        ResultSet rs = stmt.executeQuery();

        boolean hasResults = false;
        System.out.println("Transcript for student: " + studentPerm);

        while (rs.next()) {
            hasResults = true;
            int year = rs.getInt("takenyear");
            String quarter = rs.getString("Quarter");
            String course = rs.getString("tcourseID");
            String grade = rs.getString("grade");

            System.out.printf("  %s %d - %s : %s%n", quarter, year, course, grade);
        }

        if (!hasResults) {
            System.out.println("  No completed courses with grades found.");
        }

    } catch (SQLException e) {
        System.out.println("Error generating transcript.");
        e.printStackTrace();
    }
}

public static void enterGrades(String courseID, int year, String quarter, String fileName, Connection conn) {
    String verifyTermSql =
        "SELECT 1 FROM ADMIN.Term WHERE termyear = ? AND quarter = ?";
    String verifyOfferingSql =
        "SELECT 1 FROM ADMIN.QuarterOffered WHERE CourseID = ? AND Quarter = ?";
    String insertSql =
        "INSERT INTO ADMIN.TAKEN (studentperm, tcourseid, quarter, takenyear, grade) " +
        "VALUES (?, ?, ?, ?, ?)";

    try (
        PreparedStatement termStmt = conn.prepareStatement(verifyTermSql);
        PreparedStatement verifyStmt = conn.prepareStatement(verifyOfferingSql);
        BufferedReader reader = new BufferedReader(new FileReader(fileName))
    ) {
        // Step 0: Verify term exists
        termStmt.setInt(1, year);
        termStmt.setString(2, quarter.toUpperCase());
        ResultSet termRs = termStmt.executeQuery();
        if (!termRs.next()) {
            System.out.println(" Term " + quarter.toUpperCase() + " " + year + " not in system.");
            return;
        }

        // Step 1: Verify course is offered in the given quarter
        verifyStmt.setString(1, courseID);
        verifyStmt.setString(2, quarter.toUpperCase());
        ResultSet rs = verifyStmt.executeQuery();
        if (!rs.next()) {
            System.out.println(" Course " + courseID + " not offered in quarter " + quarter.toUpperCase());
            return;
        }

        // Step 2: Insert grades
        try (PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
            String line;
            int insertedCount = 0;

            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;

                String[] parts = line.split("\\s+");
                if (parts.length != 2) {
                    System.out.println(" Invalid line format: " + line);
                    continue;
                }

                String perm = parts[0];
                String grade = parts[1].toUpperCase();

                insertStmt.setString(1, perm);
                insertStmt.setString(2, courseID);
                insertStmt.setString(3, quarter.toUpperCase());
                insertStmt.setInt(4, year);
                insertStmt.setString(5, grade);

                try {
                    insertStmt.executeUpdate();
                    insertedCount++;
                } catch (SQLException e) {
                    System.out.println(" Failed to insert for " + perm + ": " + e.getMessage());
                    checkIfExists(conn, perm, courseID, quarter, year);
                }
            }

         
        }

    } catch (FileNotFoundException e) {

    } catch (IOException e) {
        e.printStackTrace();
    } catch (SQLException e) {
        e.printStackTrace();
    }
}


private static void checkIfExists(Connection conn, String perm, String courseID, String quarter, int year) {
    String sql = "SELECT * FROM ADMIN.STUDENTTAKEN WHERE studentperm = ? AND tcourseid = ? AND quarter = ? AND takenyear = ?";
    try (PreparedStatement stmt = conn.prepareStatement(sql)) {
        stmt.setString(1, perm);
        stmt.setString(2, courseID);
        stmt.setString(3, quarter.toUpperCase());
        stmt.setInt(4, year);
        ResultSet rs = stmt.executeQuery();
        if (rs.next()) {
            System.out.println(" Existing record found for " + perm + ": grade = " + rs.getString("grade"));
        } else {
            System.out.println(" No existing record found.");
        }
    } catch (SQLException ex) {
        System.out.println("  Failed to run existence check: " + ex.getMessage());
    }
}




public static void requestGradeMailer(Connection conn, int year, String quarter, String inputperm, String courseID) {
    StringBuilder query = new StringBuilder(
        "SELECT s.perm, s.name, t.tcourseid, t.coursetitle, t.takenyear, t.quarter, t.grade " +
        "FROM ADMIN.STUDENTTAKEN t " +
        "JOIN ADMIN.STUDENTINFO s ON t.studentperm = s.perm " +
        "WHERE t.grade IS NOT NULL ");

    if (year != 0) {
        query.append("AND t.takenyear = ? ");
    }
    if (!"ANY".equalsIgnoreCase(quarter)) {
        query.append("AND t.quarter = ? ");
    }
    if (inputperm != null && !inputperm.isEmpty()) {
        query.append("AND s.perm = ? ");
    }
    if (courseID != null && !courseID.isEmpty()) {
        query.append("AND t.tcourseid = ? ");
    }

    query.append("ORDER BY s.name");

    try (PreparedStatement stmt = conn.prepareStatement(query.toString())) {
        int paramIndex = 1;

        if (year != 0) {
            stmt.setInt(paramIndex++, year);
        }
        if (!"ANY".equalsIgnoreCase(quarter)) {
            stmt.setString(paramIndex++, quarter);
        }
        if (inputperm != null && !inputperm.isEmpty()) {
            stmt.setString(paramIndex++, inputperm);
        }
        if (courseID != null && !courseID.isEmpty()) {
            stmt.setString(paramIndex++, courseID);
        }

        ResultSet rs = stmt.executeQuery();

        String currentPerm = null;
        String currentName = null;

        while (rs.next()) {
            String perm = rs.getString("perm");
            String name = rs.getString("name");
            String courseTitle = rs.getString("coursetitle");
            String grade = rs.getString("grade");

            // If new student, print header
            if (currentPerm == null || !perm.equals(currentPerm)) {
                if (currentPerm != null) {
                    System.out.println();
                }
                System.out.println(name + " " + perm);
                currentPerm = perm;
                currentName = name;
            }

            System.out.println("    " + courseTitle + ": " + grade);
        }

    } catch (SQLException e) {
        System.out.println("Error generating grade mailer.");
        e.printStackTrace();
    }
}





}

