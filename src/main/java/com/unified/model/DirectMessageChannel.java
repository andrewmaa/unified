package com.unified.model;

import java.util.Date;
import java.util.Set;

/**
 * DirectMessageChannel class representing one-on-one messaging between two users.
 * Extends the abstract Channel class for direct communication.
 */
public class DirectMessageChannel extends Channel {
    private final String user1Id;
    private final String user2Id;

    /**
     * Constructor for creating a new direct message channel.
     * @param user1Id The ID of the first user
     * @param user2Id The ID of the second user
     */
    public DirectMessageChannel(String user1Id, String user2Id) {
        super("DM_" + user1Id + "_" + user2Id, 
              "Direct message between users", 
              user1Id);
        this.user1Id = user1Id;
        this.user2Id = user2Id;
        
        // Add both users as participants
        addParticipant(user2Id);
    }

    /**
     * Constructor for loading existing direct message channel from database.
     * @param channelId The existing channel ID
     * @param channelName The channel name
     * @param description The channel description
     * @param creatorId The creator ID
     * @param participantIds The set of participant IDs
     * @param createdAt The creation date
     * @param isActive Whether the channel is active
     * @param user1Id The ID of the first user
     * @param user2Id The ID of the second user
     */
    public DirectMessageChannel(String channelId, String channelName, String description,
                               String creatorId, Set<String> participantIds, Date createdAt,
                               boolean isActive, String user1Id, String user2Id) {
        super(channelId, channelName, description, creatorId, participantIds, createdAt, isActive);
        this.user1Id = user1Id;
        this.user2Id = user2Id;
    }

    @Override
    public String getChannelType() {
        return "DIRECT_MESSAGE";
    }

    /**
     * Gets the other user's ID in this direct message.
     * @param currentUserId The current user's ID
     * @return The other user's ID, or null if current user is not in this DM
     */
    public String getOtherUserId(String currentUserId) {
        if (currentUserId.equals(user1Id)) {
            return user2Id;
        } else if (currentUserId.equals(user2Id)) {
            return user1Id;
        }
        return null;
    }

    /**
     * Checks if this direct message channel involves a specific user.
     * @param userId The user ID to check
     * @return true if the user is part of this DM, false otherwise
     */
    public boolean involvesUser(String userId) {
        return user1Id.equals(userId) || user2Id.equals(userId);
    }

    // Getters
    public String getUser1Id() {
        return user1Id;
    }

    public String getUser2Id() {
        return user2Id;
    }

    @Override
    public String toString() {
        return "DirectMessageChannel{" +
                "channelId='" + getChannelId() + '\'' +
                ", user1Id='" + user1Id + '\'' +
                ", user2Id='" + user2Id + '\'' +
                ", participantCount=" + getParticipantCount() +
                ", messageCount=" + getMessageCount() +
                ", isActive=" + isActive() +
                '}';
    }
} 