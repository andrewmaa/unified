package com.unified.model;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Abstract Message class representing a message in the Unified messaging system.
 * Models the structure and behavior of messages sent within channels.
 */
public abstract class Message {
    private final String messageId;
    private final String senderId;
    private final String channelId;
    private final LocalDateTime timestamp;
    private boolean isRead;
    private String content;

    /**
     * Constructor for creating a new message.
     * @param senderId The ID of the message sender
     * @param channelId The ID of the channel where the message is sent
     * @param content The message content
     */
    public Message(String senderId, String channelId, String content) {
        this.messageId = UUID.randomUUID().toString();
        this.senderId = senderId;
        this.channelId = channelId;
        this.content = content;
        this.timestamp = LocalDateTime.now();
        this.isRead = false;
    }

    /**
     * Constructor for loading existing message from database.
     * @param messageId The existing message ID
     * @param senderId The sender ID
     * @param channelId The channel ID
     * @param content The message content
     * @param timestamp The message timestamp
     * @param isRead Whether the message has been read
     */
    public Message(String messageId, String senderId, String channelId, String content, 
                   LocalDateTime timestamp, boolean isRead) {
        this.messageId = messageId;
        this.senderId = senderId;
        this.channelId = channelId;
        this.content = content;
        this.timestamp = timestamp;
        this.isRead = isRead;
    }

    /**
     * Marks the message as read.
     */
    public void markAsRead() {
        this.isRead = true;
    }

    /**
     * Marks the message as unread.
     */
    public void markAsUnread() {
        this.isRead = false;
    }

    /**
     * Gets the message type for display purposes.
     * @return A string representing the message type
     */
    public abstract String getMessageType();

    /**
     * Gets the formatted content for display.
     * @return The formatted message content
     */
    public abstract String getFormattedContent();

    /**
     * Exports the message to a string format for chat history export.
     * @return The message in export format
     */
    public String exportToString() {
        return String.format("[%s] %s: %s", 
                timestamp.toString(), 
                senderId, 
                getFormattedContent());
    }

    // Getters and setters
    public String getMessageId() {
        return messageId;
    }

    public String getSenderId() {
        return senderId;
    }

    public String getChannelId() {
        return channelId;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public boolean isRead() {
        return isRead;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    @Override
    public String toString() {
        return "Message{" +
                "messageId='" + messageId + '\'' +
                ", senderId='" + senderId + '\'' +
                ", channelId='" + channelId + '\'' +
                ", content='" + content + '\'' +
                ", timestamp=" + timestamp +
                ", isRead=" + isRead +
                ", messageType='" + getMessageType() + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Message message = (Message) obj;
        return messageId.equals(message.messageId);
    }

    @Override
    public int hashCode() {
        return messageId.hashCode();
    }
} 