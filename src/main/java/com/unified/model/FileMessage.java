package com.unified.model;

/**
 * FileMessage class representing file attachment messages in the Unified messaging system.
 * Extends the abstract Message class for file sharing functionality.
 */
public class FileMessage extends Message {
    private String fileName;
    private String fileUrl;
    private long fileSize;
    private String fileType;

    /**
     * Constructor for creating a new file message.
     * @param senderId The ID of the message sender
     * @param channelId The ID of the channel where the message is sent
     * @param fileName The name of the attached file
     * @param fileUrl The URL where the file is stored
     * @param fileSize The size of the file in bytes
     * @param fileType The MIME type of the file
     */
    public FileMessage(String senderId, String channelId, String fileName, 
                      String fileUrl, long fileSize, String fileType) {
        super(senderId, channelId, "File: " + fileName);
        this.fileName = fileName;
        this.fileUrl = fileUrl;
        this.fileSize = fileSize;
        this.fileType = fileType;
    }

    /**
     * Constructor for loading existing file message from database.
     * @param messageId The existing message ID
     * @param senderId The sender ID
     * @param channelId The channel ID
     * @param content The message content
     * @param timestamp The message timestamp
     * @param isRead Whether the message has been read
     * @param fileName The name of the attached file
     * @param fileUrl The URL where the file is stored
     * @param fileSize The size of the file in bytes
     * @param fileType The MIME type of the file
     */
    public FileMessage(String messageId, String senderId, String channelId, String content,
                       java.time.LocalDateTime timestamp, boolean isRead, String fileName,
                       String fileUrl, long fileSize, String fileType) {
        super(messageId, senderId, channelId, content, timestamp, isRead);
        this.fileName = fileName;
        this.fileUrl = fileUrl;
        this.fileSize = fileSize;
        this.fileType = fileType;
    }

    @Override
    public String getMessageType() {
        return "FILE";
    }

    @Override
    public String getFormattedContent() {
        return String.format("📎 %s (%s, %s)", 
                fileName, 
                formatFileSize(fileSize), 
                fileType);
    }

    /**
     * Formats file size in human-readable format.
     * @param bytes The file size in bytes
     * @return Formatted file size string
     */
    private String formatFileSize(long bytes) {
        if (bytes < 1024) {
            return bytes + " B";
        } else if (bytes < 1024 * 1024) {
            return String.format("%.1f KB", bytes / 1024.0);
        } else if (bytes < 1024 * 1024 * 1024) {
            return String.format("%.1f MB", bytes / (1024.0 * 1024.0));
        } else {
            return String.format("%.1f GB", bytes / (1024.0 * 1024.0 * 1024.0));
        }
    }

    // Getters and setters
    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFileUrl() {
        return fileUrl;
    }

    public void setFileUrl(String fileUrl) {
        this.fileUrl = fileUrl;
    }

    public long getFileSize() {
        return fileSize;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    @Override
    public String toString() {
        return "FileMessage{" +
                "messageId='" + getMessageId() + '\'' +
                ", senderId='" + getSenderId() + '\'' +
                ", channelId='" + getChannelId() + '\'' +
                ", fileName='" + fileName + '\'' +
                ", fileUrl='" + fileUrl + '\'' +
                ", fileSize=" + fileSize +
                ", fileType='" + fileType + '\'' +
                ", timestamp=" + getTimestamp() +
                ", isRead=" + isRead() +
                '}';
    }
} 