package com.unified.model;

import java.util.UUID;
import java.util.HashSet;
import java.util.Set;
import com.unified.util.PasswordManager;
/**
 * Abstract User class representing a user in the Unified messaging system.
 * Contains fundamental user information and methods for authentication and messaging.
 */
public abstract class User {
    private final String userId;
    private String username;
    private String fullName;
    private String email;
    private String hashedPassword;
    private String yearOfGraduation;
    private String major;
    private String school;
    private Set<String> channelIds;
    private boolean isOnline;

    /**
     * Constructor for creating a new user.
     * @param username The unique username
     * @param fullName The user's full name
     * @param email The user's email address
     * @param password The plain text password (will be hashed)
     */
    public User(String username, String fullName, String email, String password) {
        this.userId = UUID.randomUUID().toString();
        this.username = username;
        this.fullName = fullName;
        this.email = email;
        this.hashedPassword = PasswordManager.hashPassword(password);
        this.channelIds = new HashSet<>();
        this.isOnline = false;
    }

    /**
     * Constructor for loading existing user from database.
     * @param userId The existing user ID
     * @param username The username
     * @param fullName The full name
     * @param email The email
     * @param hashedPassword The already hashed password
     */
    public User(String userId, String username, String fullName, String email, String hashedPassword) {
        this.userId = userId;
        this.username = username;
        this.fullName = fullName;
        this.email = email;
        this.hashedPassword = hashedPassword;
        this.channelIds = new HashSet<>();
        this.isOnline = false;
    }

    /**
     * Verifies if the provided password matches the stored hashed password.
     * @param password The plain text password to verify
     * @return true if password matches, false otherwise
     */
    public boolean verifyPassword(String password) {
        return PasswordManager.verifyPassword(password, this.hashedPassword);
    }

    /**
     * Adds a channel to the user's list of channels.
     * @param channelId The ID of the channel to add
     */
    public void joinChannel(String channelId) {
        this.channelIds.add(channelId);
    }

    /**
     * Removes a channel from the user's list of channels.
     * @param channelId The ID of the channel to remove
     */
    public void leaveChannel(String channelId) {
        this.channelIds.remove(channelId);
    }

    /**
     * Checks if the user is a member of a specific channel.
     * @param channelId The channel ID to check
     * @return true if user is a member, false otherwise
     */
    public boolean isMemberOfChannel(String channelId) {
        return this.channelIds.contains(channelId);
    }

    /**
     * Sets the user's online status.
     * @param online true if user is online, false otherwise
     */
    public void setOnline(boolean online) {
        this.isOnline = online;
    }

    // Getters and setters
    public String getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getYearOfGraduation() {
        return yearOfGraduation;
    }

    public void setYearOfGraduation(String yearOfGraduation) {
        this.yearOfGraduation = yearOfGraduation;
    }

    public String getMajor() {
        return major;
    }

    public void setMajor(String major) {
        this.major = major;
    }

    public String getSchool() {
        return school;
    }

    public void setSchool(String school) {
        this.school = school;
    }

    public Set<String> getChannelIds() {
        return new HashSet<>(channelIds);
    }

    public boolean isOnline() {
        return isOnline;
    }

    @Override
    public String toString() {
        return "User{" +
                "userId='" + userId + '\'' +
                ", username='" + username + '\'' +
                ", fullName='" + fullName + '\'' +
                ", email='" + email + '\'' +
                ", yearOfGraduation='" + yearOfGraduation + '\'' +
                ", major='" + major + '\'' +
                ", school='" + school + '\'' +
                ", isOnline=" + isOnline +
                '}';
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        User user = (User) obj;
        return userId.equals(user.userId);
    }

    @Override
    public int hashCode() {
        return userId.hashCode();
    }
} 