package com.unified.model;

import java.util.Date;
import java.util.Set;

/**
 * GroupChatChannel class representing group messaging with multiple participants.
 * Extends the abstract Channel class for group communication.
 */
public class GroupChatChannel extends Channel {
    private int maxParticipants;
    private boolean isPrivate;

    /**
     * Constructor for creating a new group chat channel.
     * @param channelName The name of the group chat
     * @param description The description of the group chat
     * @param creatorId The ID of the user who created the group
     * @param maxParticipants The maximum number of participants allowed
     * @param isPrivate Whether the group is private (invite-only)
     */
    public GroupChatChannel(String channelName, String description, String creatorId,
                           int maxParticipants, boolean isPrivate) {
        super(channelName, description, creatorId);
        this.maxParticipants = maxParticipants;
        this.isPrivate = isPrivate;
    }

    /**
     * Constructor for loading existing group chat channel from database.
     * @param channelId The existing channel ID
     * @param channelName The channel name
     * @param description The channel description
     * @param creatorId The creator ID
     * @param participantIds The set of participant IDs
     * @param createdAt The creation date
     * @param isActive Whether the channel is active
     * @param maxParticipants The maximum number of participants allowed
     * @param isPrivate Whether the group is private
     */
    public GroupChatChannel(String channelId, String channelName, String description,
                           String creatorId, Set<String> participantIds, Date createdAt,
                           boolean isActive, int maxParticipants, boolean isPrivate) {
        super(channelId, channelName, description, creatorId, participantIds, createdAt, isActive);
        this.maxParticipants = maxParticipants;
        this.isPrivate = isPrivate;
    }

    @Override
    public String getChannelType() {
        return "GROUP_CHAT";
    }

    @Override
    public boolean addParticipant(String userId) {
        if (getParticipantCount() >= maxParticipants) {
            return false; // Group is full
        }
        return super.addParticipant(userId);
    }

    /**
     * Checks if the group is full.
     * @return true if the group has reached maximum capacity, false otherwise
     */
    public boolean isFull() {
        return getParticipantCount() >= maxParticipants;
    }

    /**
     * Gets the remaining capacity of the group.
     * @return The number of additional participants that can join
     */
    public int getRemainingCapacity() {
        return Math.max(0, maxParticipants - getParticipantCount());
    }

    // Getters and setters
    public int getMaxParticipants() {
        return maxParticipants;
    }

    public void setMaxParticipants(int maxParticipants) {
        this.maxParticipants = maxParticipants;
    }

    public boolean isPrivate() {
        return isPrivate;
    }

    public void setPrivate(boolean isPrivate) {
        this.isPrivate = isPrivate;
    }

    @Override
    public String toString() {
        return "GroupChatChannel{" +
                "channelId='" + getChannelId() + '\'' +
                ", channelName='" + getChannelName() + '\'' +
                ", description='" + getDescription() + '\'' +
                ", participantCount=" + getParticipantCount() +
                ", maxParticipants=" + maxParticipants +
                ", messageCount=" + getMessageCount() +
                ", creatorId='" + getCreatorId() + '\'' +
                ", isPrivate=" + isPrivate +
                ", isActive=" + isActive() +
                '}';
    }
} 