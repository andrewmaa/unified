package com.unified.model;

/**
 * TextMessage class representing plain text messages in the Unified messaging system.
 * Extends the abstract Message class for basic text communication.
 */
public class TextMessage extends Message {

    /**
     * Constructor for creating a new text message.
     * @param senderId The ID of the message sender
     * @param channelId The ID of the channel where the message is sent
     * @param content The text content of the message
     */
    public TextMessage(String senderId, String channelId, String content) {
        super(senderId, channelId, content);
    }

    /**
     * Constructor for loading existing text message from database.
     * @param messageId The existing message ID
     * @param senderId The sender ID
     * @param channelId The channel ID
     * @param content The message content
     * @param timestamp The message timestamp
     * @param isRead Whether the message has been read
     */
    public TextMessage(String messageId, String senderId, String channelId, String content, 
                       java.time.LocalDateTime timestamp, boolean isRead) {
        super(messageId, senderId, channelId, content, timestamp, isRead);
    }

    @Override
    public String getMessageType() {
        return "TEXT";
    }

    @Override
    public String getFormattedContent() {
        return getContent();
    }
} 