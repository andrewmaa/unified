package com.unified.model;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Abstract Channel class representing a communication channel in the Unified messaging system.
 * Holds a list of participants and messages, with functions for sending messages and managing membership.
 */
public abstract class Channel {
    private final String channelId;
    private String channelName;
    private String description;
    private final Set<String> participantIds;
    private final List<Message> messages;
    private final String creatorId;
    private final Date createdAt;
    private boolean isActive;

    /**
     * Constructor for creating a new channel.
     * @param channelName The name of the channel
     * @param description The description of the channel
     * @param creatorId The ID of the user who created the channel
     */
    public Channel(String channelName, String description, String creatorId) {
        this.channelId = UUID.randomUUID().toString();
        this.channelName = channelName;
        this.description = description;
        this.creatorId = creatorId;
        this.participantIds = new HashSet<>();
        this.messages = new ArrayList<>();
        this.createdAt = new Date();
        this.isActive = true;
        
        // Add creator as first participant
        this.participantIds.add(creatorId);
    }

    /**
     * Constructor for loading existing channel from database.
     * @param channelId The existing channel ID
     * @param channelName The channel name
     * @param description The channel description
     * @param creatorId The creator ID
     * @param participantIds The set of participant IDs
     * @param createdAt The creation date
     * @param isActive Whether the channel is active
     */
    public Channel(String channelId, String channelName, String description, String creatorId,
                   Set<String> participantIds, Date createdAt, boolean isActive) {
        this.channelId = channelId;
        this.channelName = channelName;
        this.description = description;
        this.creatorId = creatorId;
        this.participantIds = new HashSet<>(participantIds);
        this.messages = new ArrayList<>();
        this.createdAt = createdAt;
        this.isActive = isActive;
    }

    /**
     * Adds a participant to the channel.
     * @param userId The ID of the user to add
     * @return true if added successfully, false if already a member
     */
    public boolean addParticipant(String userId) {
        if (!isActive) {
            return false;
        }
        return participantIds.add(userId);
    }

    /**
     * Removes a participant from the channel.
     * @param userId The ID of the user to remove
     * @return true if removed successfully, false if not a member
     */
    public boolean removeParticipant(String userId) {
        if (userId.equals(creatorId)) {
            return false; // Cannot remove creator
        }
        return participantIds.remove(userId);
    }

    /**
     * Checks if a user is a participant in this channel.
     * @param userId The user ID to check
     * @return true if user is a participant, false otherwise
     */
    public boolean isParticipant(String userId) {
        return participantIds.contains(userId);
    }

    /**
     * Sends a message to the channel.
     * @param message The message to send
     * @return true if message was sent successfully, false otherwise
     */
    public boolean sendMessage(Message message) {
        if (!isActive || !isParticipant(message.getSenderId())) {
            return false;
        }
        messages.add(message);
        return true;
    }

    /**
     * Gets all messages in the channel.
     * @return List of all messages
     */
    public List<Message> getMessages() {
        return new ArrayList<>(messages);
    }

    /**
     * Gets unread messages for a specific user.
     * @param userId The user ID to check unread messages for
     * @return List of unread messages
     */
    public List<Message> getUnreadMessages(String userId) {
        return messages.stream()
                .filter(message -> !message.isRead() && !message.getSenderId().equals(userId))
                .collect(Collectors.toList());
    }

    /**
     * Marks all messages as read for a specific user.
     * @param userId The user ID
     */
    public void markAllMessagesAsRead(String userId) {
        messages.stream()
                .filter(message -> !message.isRead() && !message.getSenderId().equals(userId))
                .forEach(Message::markAsRead);
    }

    /**
     * Searches messages in the channel by keyword.
     * @param keyword The keyword to search for
     * @return List of messages containing the keyword
     */
    public List<Message> searchMessages(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return new ArrayList<>();
        }
        
        String lowerKeyword = keyword.toLowerCase();
        return messages.stream()
                .filter(message -> message.getContent().toLowerCase().contains(lowerKeyword))
                .collect(Collectors.toList());
    }

    /**
     * Exports chat history to a string format.
     * @return The chat history as a formatted string
     */
    public String exportChatHistory() {
        StringBuilder history = new StringBuilder();
        history.append("=== Chat History for ").append(channelName).append(" ===\n");
        history.append("Created: ").append(createdAt).append("\n");
        history.append("Participants: ").append(participantIds.size()).append("\n\n");
        
        for (Message message : messages) {
            history.append(message.exportToString()).append("\n");
        }
        
        return history.toString();
    }

    /**
     * Gets the channel type for display purposes.
     * @return A string representing the channel type
     */
    public abstract String getChannelType();

    // Getters and setters
    public String getChannelId() {
        return channelId;
    }

    public String getChannelName() {
        return channelName;
    }

    public void setChannelName(String channelName) {
        this.channelName = channelName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Set<String> getParticipantIds() {
        return new HashSet<>(participantIds);
    }

    public String getCreatorId() {
        return creatorId;
    }

    public Date getCreatedAt() {
        return new Date(createdAt.getTime());
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public int getMessageCount() {
        return messages.size();
    }

    public int getParticipantCount() {
        return participantIds.size();
    }

    @Override
    public String toString() {
        return "Channel{" +
                "channelId='" + channelId + '\'' +
                ", channelName='" + channelName + '\'' +
                ", description='" + description + '\'' +
                ", channelType='" + getChannelType() + '\'' +
                ", participantCount=" + participantIds.size() +
                ", messageCount=" + messages.size() +
                ", creatorId='" + creatorId + '\'' +
                ", createdAt=" + createdAt +
                ", isActive=" + isActive +
                '}';
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Channel channel = (Channel) obj;
        return channelId.equals(channel.channelId);
    }

    @Override
    public int hashCode() {
        return channelId.hashCode();
    }
} 