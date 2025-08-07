package com.unified.model;

/**
 * AnnouncementMessage class representing course announcements in the Unified messaging system.
 * Extends the abstract Message class for academic announcements and notifications.
 */
public class AnnouncementMessage extends Message {
    private String courseId;
    private String courseName;
    private boolean isImportant;
    private String announcementType; // "ASSIGNMENT", "EXAM", "GENERAL", etc.

    /**
     * Constructor for creating a new announcement message.
     * @param senderId The ID of the message sender (usually instructor)
     * @param channelId The ID of the channel where the announcement is posted
     * @param content The announcement content
     * @param courseId The ID of the course this announcement relates to
     * @param courseName The name of the course
     * @param isImportant Whether this is an important announcement
     * @param announcementType The type of announcement
     */
    public AnnouncementMessage(String senderId, String channelId, String content,
                              String courseId, String courseName, boolean isImportant,
                              String announcementType) {
        super(senderId, channelId, content);
        this.courseId = courseId;
        this.courseName = courseName;
        this.isImportant = isImportant;
        this.announcementType = announcementType;
    }

    /**
     * Constructor for loading existing announcement message from database.
     * @param messageId The existing message ID
     * @param senderId The sender ID
     * @param channelId The channel ID
     * @param content The message content
     * @param timestamp The message timestamp
     * @param isRead Whether the message has been read
     * @param courseId The ID of the course this announcement relates to
     * @param courseName The name of the course
     * @param isImportant Whether this is an important announcement
     * @param announcementType The type of announcement
     */
    public AnnouncementMessage(String messageId, String senderId, String channelId, String content,
                               java.time.LocalDateTime timestamp, boolean isRead, String courseId,
                               String courseName, boolean isImportant, String announcementType) {
        super(messageId, senderId, channelId, content, timestamp, isRead);
        this.courseId = courseId;
        this.courseName = courseName;
        this.isImportant = isImportant;
        this.announcementType = announcementType;
    }

    @Override
    public String getMessageType() {
        return "ANNOUNCEMENT";
    }

    @Override
    public String getFormattedContent() {
        String prefix = isImportant ? "🚨 " : "📢 ";
        String typeIcon = getTypeIcon();
        return String.format("%s%s [%s] %s: %s", 
                prefix, 
                typeIcon, 
                courseName, 
                announcementType, 
                getContent());
    }

    /**
     * Gets the appropriate icon for the announcement type.
     * @return The icon string for the announcement type
     */
    private String getTypeIcon() {
        switch (announcementType.toUpperCase()) {
            case "ASSIGNMENT":
                return "📝";
            case "EXAM":
                return "📚";
            case "DEADLINE":
                return "⏰";
            case "GRADE":
                return "📊";
            case "GENERAL":
                return "ℹ️";
            default:
                return "📢";
        }
    }

    // Getters and setters
    public String getCourseId() {
        return courseId;
    }

    public void setCourseId(String courseId) {
        this.courseId = courseId;
    }

    public String getCourseName() {
        return courseName;
    }

    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }

    public boolean isImportant() {
        return isImportant;
    }

    public void setImportant(boolean important) {
        isImportant = important;
    }

    public String getAnnouncementType() {
        return announcementType;
    }

    public void setAnnouncementType(String announcementType) {
        this.announcementType = announcementType;
    }

    @Override
    public String toString() {
        return "AnnouncementMessage{" +
                "messageId='" + getMessageId() + '\'' +
                ", senderId='" + getSenderId() + '\'' +
                ", channelId='" + getChannelId() + '\'' +
                ", courseId='" + courseId + '\'' +
                ", courseName='" + courseName + '\'' +
                ", isImportant=" + isImportant +
                ", announcementType='" + announcementType + '\'' +
                ", content='" + getContent() + '\'' +
                ", timestamp=" + getTimestamp() +
                ", isRead=" + isRead() +
                '}';
    }
} 