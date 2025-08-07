package com.unified.model;

import java.util.Set;

/**
 * Student class extending the User class with student-specific functionality.
 * Represents a student user in the Unified messaging system.
 */
public class Student extends User {
    private String studentId;
    private Set<String> enrolledCourses;

    /**
     * Constructor for creating a new student.
     * @param username The unique username
     * @param fullName The student's full name
     * @param email The student's email address
     * @param password The plain text password (will be hashed)
     * @param studentId The student's university ID
     */
    public Student(String username, String fullName, String email, String password, String studentId) {
        super(username, fullName, email, password);
        this.studentId = studentId;
        this.enrolledCourses = new java.util.HashSet<>();
    }

    /**
     * Constructor for loading existing student from database.
     * @param userId The existing user ID
     * @param username The username
     * @param fullName The full name
     * @param email The email
     * @param hashedPassword The already hashed password
     * @param studentId The student's university ID
     */
    public Student(String userId, String username, String fullName, String email, 
                   String hashedPassword, String studentId) {
        super(userId, username, fullName, email, hashedPassword);
        this.studentId = studentId;
        this.enrolledCourses = new java.util.HashSet<>();
    }

    /**
     * Enrolls the student in a course.
     * @param courseId The course ID to enroll in
     */
    public void enrollInCourse(String courseId) {
        this.enrolledCourses.add(courseId);
    }

    /**
     * Unenrolls the student from a course.
     * @param courseId The course ID to unenroll from
     */
    public void unenrollFromCourse(String courseId) {
        this.enrolledCourses.remove(courseId);
    }

    /**
     * Checks if the student is enrolled in a specific course.
     * @param courseId The course ID to check
     * @return true if enrolled, false otherwise
     */
    public boolean isEnrolledInCourse(String courseId) {
        return this.enrolledCourses.contains(courseId);
    }

    /**
     * Gets all courses the student is enrolled in.
     * @return Set of course IDs
     */
    public Set<String> getEnrolledCourses() {
        return new java.util.HashSet<>(enrolledCourses);
    }

    // Getters and setters
    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    @Override
    public String toString() {
        return "Student{" +
                "userId='" + getUserId() + '\'' +
                ", username='" + getUsername() + '\'' +
                ", fullName='" + getFullName() + '\'' +
                ", email='" + getEmail() + '\'' +
                ", studentId='" + studentId + '\'' +
                ", yearOfGraduation='" + getYearOfGraduation() + '\'' +
                ", major='" + getMajor() + '\'' +
                ", school='" + getSchool() + '\'' +
                ", enrolledCourses=" + enrolledCourses +
                ", isOnline=" + isOnline() +
                '}';
    }
} 