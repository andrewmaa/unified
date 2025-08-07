package com.unified;

import com.unified.model.*;
import com.unified.util.PasswordManager;

import java.util.*;
import java.io.*;

/**
 * Main application class for the Unified messaging system.
 * Acts as the entry point and coordinator for the whole application.
 */
public class App {
    private static final Scanner scanner = new Scanner(System.in);
    private static User currentUser = null;
    private static Map<String, User> users = new HashMap<>();
    private static Map<String, Channel> channels = new HashMap<>();
    private static boolean isRunning = true;

    public static void main(String[] args) {
        System.out.println("=== Welcome to Unified - University Messaging System ===");
        
        while (isRunning) {
            if (currentUser == null) {
                showLoginMenu();
            } else {
                showMainMenu();
            }
        }
        
        System.out.println("Thank you for using Unified!");
        scanner.close();
    }

    /**
     * Displays the login menu and handles user authentication.
     */
    private static void showLoginMenu() {
        System.out.println("\n=== Login Menu ===");
        System.out.println("1. Login");
        System.out.println("2. Register");
        System.out.println("3. Exit");
        System.out.print("Enter your choice: ");

        int choice = getIntInput();
        switch (choice) {
            case 1:
                login();
                break;
            case 2:
                register();
                break;
            case 3:
                isRunning = false;
                break;
            default:
                System.out.println("Invalid choice. Please try again.");
        }
    }

    /**
     * Displays the main menu after successful login.
     */
    private static void showMainMenu() {
        System.out.println("\n=== Main Menu ===");
        System.out.println("Welcome, " + currentUser.getFullName() + "!");
        System.out.println("1. View Messages");
        System.out.println("2. Send Message");
        System.out.println("3. Create Channel");
        System.out.println("4. Join Channel");
        System.out.println("5. Search Messages");
        System.out.println("6. Export Chat History");
        System.out.println("7. View Profile");
        System.out.println("8. Edit Profile");
        System.out.println("9. Logout");
        System.out.print("Enter your choice: ");

        int choice = getIntInput();
        switch (choice) {
            case 1:
                viewMessages();
                break;
            case 2:
                sendMessage();
                break;
            case 3:
                createChannel();
                break;
            case 4:
                joinChannel();
                break;
            case 5:
                searchMessages();
                break;
            case 6:
                exportChatHistory();
                break;
            case 7:
                viewProfile();
                break;
            case 8:
                editProfile();
                break;
            case 9:
                logout();
                break;
            default:
                System.out.println("Invalid choice. Please try again.");
        }
    }

    /**
     * Handles user login.
     */
    private static void login() {
        System.out.print("Enter username: ");
        String username = scanner.nextLine();
        System.out.print("Enter password: ");
        String password = scanner.nextLine();

        User user = findUserByUsername(username);
        if (user != null && user.verifyPassword(password)) {
            currentUser = user;
            currentUser.setOnline(true);
            System.out.println("Login successful!");
        } else {
            System.out.println("Invalid username or password.");
        }
    }

    /**
     * Handles user registration.
     */
    private static void register() {
        System.out.print("Enter username: ");
        String username = scanner.nextLine();
        
        if (findUserByUsername(username) != null) {
            System.out.println("Username already exists.");
            return;
        }

        System.out.print("Enter full name: ");
        String fullName = scanner.nextLine();
        System.out.print("Enter email: ");
        String email = scanner.nextLine();
        System.out.print("Enter password: ");
        String password = scanner.nextLine();
        System.out.print("Enter student ID: ");
        String studentId = scanner.nextLine();

        Student student = new Student(username, fullName, email, password, studentId);
        users.put(student.getUserId(), student);
        currentUser = student;
        currentUser.setOnline(true);
        
        System.out.println("Registration successful!");
    }

    /**
     * Displays messages for the current user.
     */
    private static void viewMessages() {
        System.out.println("\n=== Your Messages ===");
        
        List<Channel> userChannels = getUserChannels();
        if (userChannels.isEmpty()) {
            System.out.println("You are not part of any channels.");
            return;
        }

        for (int i = 0; i < userChannels.size(); i++) {
            Channel channel = userChannels.get(i);
            List<Message> unreadMessages = channel.getUnreadMessages(currentUser.getUserId());
            System.out.printf("%d. %s (%d unread messages)\n", 
                    i + 1, channel.getChannelName(), unreadMessages.size());
        }

        System.out.print("Enter channel number to view messages (0 to go back): ");
        int choice = getIntInput();
        
        if (choice > 0 && choice <= userChannels.size()) {
            Channel selectedChannel = userChannels.get(choice - 1);
            viewChannelMessages(selectedChannel);
        }
    }

    /**
     * Displays messages in a specific channel.
     */
    private static void viewChannelMessages(Channel channel) {
        System.out.println("\n=== " + channel.getChannelName() + " ===");
        
        List<Message> messages = channel.getMessages();
        if (messages.isEmpty()) {
            System.out.println("No messages in this channel.");
            return;
        }

        for (Message message : messages) {
            String senderName = getSenderName(message.getSenderId());
            String alignment = message.getSenderId().equals(currentUser.getUserId()) ? "RIGHT" : "LEFT";
            System.out.printf("[%s] %s (%s): %s\n", 
                    message.getTimestamp(), senderName, alignment, message.getFormattedContent());
        }

        // Mark messages as read
        channel.markAllMessagesAsRead(currentUser.getUserId());
    }

    /**
     * Handles sending a new message.
     */
    private static void sendMessage() {
        System.out.println("\n=== Send Message ===");
        
        List<Channel> userChannels = getUserChannels();
        if (userChannels.isEmpty()) {
            System.out.println("You are not part of any channels.");
            return;
        }

        for (int i = 0; i < userChannels.size(); i++) {
            Channel channel = userChannels.get(i);
            System.out.printf("%d. %s\n", i + 1, channel.getChannelName());
        }

        System.out.print("Enter channel number: ");
        int choice = getIntInput();
        
        if (choice > 0 && choice <= userChannels.size()) {
            Channel selectedChannel = userChannels.get(choice - 1);
            System.out.print("Enter your message: ");
            String content = scanner.nextLine();
            
            TextMessage message = new TextMessage(currentUser.getUserId(), 
                                                selectedChannel.getChannelId(), content);
            
            if (selectedChannel.sendMessage(message)) {
                System.out.println("Message sent successfully!");
            } else {
                System.out.println("Failed to send message.");
            }
        }
    }

    /**
     * Handles creating a new channel.
     */
    private static void createChannel() {
        System.out.println("\n=== Create Channel ===");
        System.out.println("1. Direct Message");
        System.out.println("2. Group Chat");
        System.out.println("3. Course Channel");
        System.out.print("Enter channel type: ");

        int choice = getIntInput();
        switch (choice) {
            case 1:
                createDirectMessage();
                break;
            case 2:
                createGroupChat();
                break;
            case 3:
                createCourseChannel();
                break;
            default:
                System.out.println("Invalid choice.");
        }
    }

    /**
     * Creates a direct message channel.
     */
    private static void createDirectMessage() {
        System.out.print("Enter username of the other user: ");
        String username = scanner.nextLine();
        
        User otherUser = findUserByUsername(username);
        if (otherUser == null) {
            System.out.println("User not found.");
            return;
        }

        DirectMessageChannel dm = new DirectMessageChannel(currentUser.getUserId(), otherUser.getUserId());
        channels.put(dm.getChannelId(), dm);
        
        // Add channel to both users
        currentUser.joinChannel(dm.getChannelId());
        otherUser.joinChannel(dm.getChannelId());
        
        System.out.println("Direct message channel created!");
    }

    /**
     * Creates a group chat channel.
     */
    private static void createGroupChat() {
        System.out.print("Enter group name: ");
        String name = scanner.nextLine();
        System.out.print("Enter group description: ");
        String description = scanner.nextLine();
        System.out.print("Enter maximum participants: ");
        int maxParticipants = getIntInput();
        System.out.print("Is private? (y/n): ");
        boolean isPrivate = scanner.nextLine().toLowerCase().startsWith("y");

        GroupChatChannel group = new GroupChatChannel(name, description, currentUser.getUserId(), 
                                                    maxParticipants, isPrivate);
        channels.put(group.getChannelId(), group);
        currentUser.joinChannel(group.getChannelId());
        
        System.out.println("Group chat created!");
    }

    /**
     * Creates a course channel.
     */
    private static void createCourseChannel() {
        System.out.print("Enter course ID: ");
        String courseId = scanner.nextLine();
        System.out.print("Enter course code (e.g., CS101): ");
        String courseCode = scanner.nextLine();
        System.out.print("Enter course name: ");
        String courseName = scanner.nextLine();
        System.out.print("Enter semester: ");
        String semester = scanner.nextLine();
        System.out.print("Enter year: ");
        int year = getIntInput();
        System.out.print("Allow student messages? (y/n): ");
        boolean allowStudentMessages = scanner.nextLine().toLowerCase().startsWith("y");

        CourseChannel course = new CourseChannel(courseId, courseCode, courseName, 
                                               currentUser.getUserId(), semester, year, allowStudentMessages);
        channels.put(course.getChannelId(), course);
        currentUser.joinChannel(course.getChannelId());
        
        System.out.println("Course channel created!");
    }

    /**
     * Handles joining an existing channel.
     */
    private static void joinChannel() {
        System.out.println("\n=== Join Channel ===");
        
        List<Channel> availableChannels = getAvailableChannels();
        if (availableChannels.isEmpty()) {
            System.out.println("No channels available to join.");
            return;
        }

        for (int i = 0; i < availableChannels.size(); i++) {
            Channel channel = availableChannels.get(i);
            System.out.printf("%d. %s (%s)\n", i + 1, channel.getChannelName(), channel.getChannelType());
        }

        System.out.print("Enter channel number: ");
        int choice = getIntInput();
        
        if (choice > 0 && choice <= availableChannels.size()) {
            Channel selectedChannel = availableChannels.get(choice - 1);
            if (selectedChannel.addParticipant(currentUser.getUserId())) {
                currentUser.joinChannel(selectedChannel.getChannelId());
                System.out.println("Successfully joined the channel!");
            } else {
                System.out.println("Failed to join the channel.");
            }
        }
    }

    /**
     * Handles searching messages.
     */
    private static void searchMessages() {
        System.out.println("\n=== Search Messages ===");
        System.out.print("Enter search keyword: ");
        String keyword = scanner.nextLine();

        List<Channel> userChannels = getUserChannels();
        List<Message> searchResults = new ArrayList<>();

        for (Channel channel : userChannels) {
            searchResults.addAll(channel.searchMessages(keyword));
        }

        if (searchResults.isEmpty()) {
            System.out.println("No messages found matching your search.");
        } else {
            System.out.println("Search results:");
            for (Message message : searchResults) {
                String senderName = getSenderName(message.getSenderId());
                String channelName = getChannelName(message.getChannelId());
                System.out.printf("[%s] %s in %s: %s\n", 
                        message.getTimestamp(), senderName, channelName, message.getFormattedContent());
            }
        }
    }

    /**
     * Handles exporting chat history.
     */
    private static void exportChatHistory() {
        System.out.println("\n=== Export Chat History ===");
        
        List<Channel> userChannels = getUserChannels();
        if (userChannels.isEmpty()) {
            System.out.println("You are not part of any channels.");
            return;
        }

        for (int i = 0; i < userChannels.size(); i++) {
            Channel channel = userChannels.get(i);
            System.out.printf("%d. %s\n", i + 1, channel.getChannelName());
        }

        System.out.print("Enter channel number: ");
        int choice = getIntInput();
        
        if (choice > 0 && choice <= userChannels.size()) {
            Channel selectedChannel = userChannels.get(choice - 1);
            String history = selectedChannel.exportChatHistory();
            
            String filename = "chat_history_" + selectedChannel.getChannelId() + ".txt";
            try (PrintWriter writer = new PrintWriter(new FileWriter(filename))) {
                writer.write(history);
                System.out.println("Chat history exported to " + filename);
            } catch (IOException e) {
                System.out.println("Failed to export chat history: " + e.getMessage());
            }
        }
    }

    /**
     * Displays the current user's profile.
     */
    private static void viewProfile() {
        System.out.println("\n=== Your Profile ===");
        System.out.println("Username: " + currentUser.getUsername());
        System.out.println("Full Name: " + currentUser.getFullName());
        System.out.println("Email: " + currentUser.getEmail());
        System.out.println("Year of Graduation: " + currentUser.getYearOfGraduation());
        System.out.println("Major: " + currentUser.getMajor());
        System.out.println("School: " + currentUser.getSchool());
        
        if (currentUser instanceof Student) {
            Student student = (Student) currentUser;
            System.out.println("Student ID: " + student.getStudentId());
            System.out.println("Enrolled Courses: " + student.getEnrolledCourses().size());
        }
    }

    /**
     * Handles editing the current user's profile.
     */
    private static void editProfile() {
        System.out.println("\n=== Edit Profile ===");
        System.out.println("1. Change Full Name");
        System.out.println("2. Change Email");
        System.out.println("3. Change Password");
        System.out.println("4. Set Year of Graduation");
        System.out.println("5. Set Major");
        System.out.println("6. Set School");
        System.out.print("Enter your choice: ");

        int choice = getIntInput();
        switch (choice) {
            case 1:
                System.out.print("Enter new full name: ");
                currentUser.setFullName(scanner.nextLine());
                break;
            case 2:
                System.out.print("Enter new email: ");
                currentUser.setEmail(scanner.nextLine());
                break;
            case 3:
                System.out.print("Enter current password: ");
                String currentPassword = scanner.nextLine();
                if (currentUser.verifyPassword(currentPassword)) {
                    System.out.print("Enter new password: ");
                    String newPassword = scanner.nextLine();
                    // Note: This would need to update the hashed password in the user object
                    System.out.println("Password change functionality not implemented in this basic version.");
                } else {
                    System.out.println("Incorrect current password.");
                }
                break;
            case 4:
                System.out.print("Enter year of graduation: ");
                currentUser.setYearOfGraduation(scanner.nextLine());
                break;
            case 5:
                System.out.print("Enter major: ");
                currentUser.setMajor(scanner.nextLine());
                break;
            case 6:
                System.out.print("Enter school: ");
                currentUser.setSchool(scanner.nextLine());
                break;
            default:
                System.out.println("Invalid choice.");
        }
        System.out.println("Profile updated successfully!");
    }

    /**
     * Handles user logout.
     */
    private static void logout() {
        if (currentUser != null) {
            currentUser.setOnline(false);
            currentUser = null;
        }
        System.out.println("Logged out successfully.");
    }

    // Helper methods
    private static int getIntInput() {
        while (!scanner.hasNextInt()) {
            System.out.println("Please enter a valid number.");
            scanner.next();
        }
        int input = scanner.nextInt();
        scanner.nextLine(); // Consume newline
        return input;
    }

    private static User findUserByUsername(String username) {
        return users.values().stream()
                .filter(user -> user.getUsername().equals(username))
                .findFirst()
                .orElse(null);
    }

    private static List<Channel> getUserChannels() {
        return channels.values().stream()
                .filter(channel -> channel.isParticipant(currentUser.getUserId()))
                .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
    }

    private static List<Channel> getAvailableChannels() {
        return channels.values().stream()
                .filter(channel -> !channel.isParticipant(currentUser.getUserId()))
                .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
    }

    private static String getSenderName(String senderId) {
        User sender = users.get(senderId);
        return sender != null ? sender.getFullName() : "Unknown User";
    }

    private static String getChannelName(String channelId) {
        Channel channel = channels.get(channelId);
        return channel != null ? channel.getChannelName() : "Unknown Channel";
    }
} 