package com.unified.model;

import java.util.Date;
import java.util.Set;

/**
 * CourseChannel class representing classroom group chats for specific courses.
 * Extends the abstract Channel class for academic course communication.
 */
public class CourseChannel extends Channel {
    private String courseId;
    private String courseCode;
    private String courseName;
    private String instructorId;
    private String semester;
    private int year;
    private boolean allowStudentMessages;

    /**
     * Constructor for creating a new course channel.
     * @param courseId The unique course ID
     * @param courseCode The course code (e.g., "CS101")
     * @param courseName The full course name
     * @param instructorId The ID of the course instructor
     * @param semester The semester (e.g., "Fall", "Spring", "Summer")
     * @param year The academic year
     * @param allowStudentMessages Whether students can send messages
     */
    public CourseChannel(String courseId, String courseCode, String courseName,
                        String instructorId, String semester, int year, boolean allowStudentMessages) {
        super(courseCode + " - " + courseName, 
              "Course discussion channel for " + courseCode + " (" + semester + " " + year + ")", 
              instructorId);
        this.courseId = courseId;
        this.courseCode = courseCode;
        this.courseName = courseName;
        this.instructorId = instructorId;
        this.semester = semester;
        this.year = year;
        this.allowStudentMessages = allowStudentMessages;
    }

    /**
     * Constructor for loading existing course channel from database.
     * @param channelId The existing channel ID
     * @param channelName The channel name
     * @param description The channel description
     * @param creatorId The creator ID
     * @param participantIds The set of participant IDs
     * @param createdAt The creation date
     * @param isActive Whether the channel is active
     * @param courseId The unique course ID
     * @param courseCode The course code
     * @param courseName The full course name
     * @param instructorId The ID of the course instructor
     * @param semester The semester
     * @param year The academic year
     * @param allowStudentMessages Whether students can send messages
     */
    public CourseChannel(String channelId, String channelName, String description,
                        String creatorId, Set<String> participantIds, Date createdAt,
                        boolean isActive, String courseId, String courseCode, String courseName,
                        String instructorId, String semester, int year, boolean allowStudentMessages) {
        super(channelId, channelName, description, creatorId, participantIds, createdAt, isActive);
        this.courseId = courseId;
        this.courseCode = courseCode;
        this.courseName = courseName;
        this.instructorId = instructorId;
        this.semester = semester;
        this.year = year;
        this.allowStudentMessages = allowStudentMessages;
    }

    @Override
    public String getChannelType() {
        return "COURSE";
    }

    @Override
    public boolean sendMessage(Message message) {
        // Check if the sender is the instructor or if student messages are allowed
        if (!message.getSenderId().equals(instructorId) && !allowStudentMessages) {
            return false; // Students cannot send messages in this course channel
        }
        return super.sendMessage(message);
    }

    /**
     * Checks if a user is the instructor of this course.
     * @param userId The user ID to check
     * @return true if the user is the instructor, false otherwise
     */
    public boolean isInstructor(String userId) {
        return instructorId.equals(userId);
    }

    /**
     * Gets the academic term as a formatted string.
     * @return The formatted academic term (e.g., "Fall 2024")
     */
    public String getAcademicTerm() {
        return semester + " " + year;
    }

    /**
     * Gets the course identifier as a formatted string.
     * @return The formatted course identifier (e.g., "CS101 - Introduction to Programming")
     */
    public String getCourseIdentifier() {
        return courseCode + " - " + courseName;
    }

    // Getters and setters
    public String getCourseId() {
        return courseId;
    }

    public void setCourseId(String courseId) {
        this.courseId = courseId;
    }

    public String getCourseCode() {
        return courseCode;
    }

    public void setCourseCode(String courseCode) {
        this.courseCode = courseCode;
    }

    public String getCourseName() {
        return courseName;
    }

    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }

    public String getInstructorId() {
        return instructorId;
    }

    public void setInstructorId(String instructorId) {
        this.instructorId = instructorId;
    }

    public String getSemester() {
        return semester;
    }

    public void setSemester(String semester) {
        this.semester = semester;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public boolean isAllowStudentMessages() {
        return allowStudentMessages;
    }

    public void setAllowStudentMessages(boolean allowStudentMessages) {
        this.allowStudentMessages = allowStudentMessages;
    }

    @Override
    public String toString() {
        return "CourseChannel{" +
                "channelId='" + getChannelId() + '\'' +
                ", courseId='" + courseId + '\'' +
                ", courseCode='" + courseCode + '\'' +
                ", courseName='" + courseName + '\'' +
                ", instructorId='" + instructorId + '\'' +
                ", semester='" + semester + '\'' +
                ", year=" + year +
                ", participantCount=" + getParticipantCount() +
                ", messageCount=" + getMessageCount() +
                ", allowStudentMessages=" + allowStudentMessages +
                ", isActive=" + isActive() +
                '}';
    }
} 