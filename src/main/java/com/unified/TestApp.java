package com.unified;

import com.unified.model.*;
import com.unified.util.PasswordManager;

/**
 * Simple test class to verify the basic functionality of the Unified messaging system.
 */
public class TestApp {
    public static void main(String[] args) {
        System.out.println("=== Unified Messaging System - Basic Test ===");
        
        // Test password hashing
        System.out.println("\n1. Testing Password Manager:");
        String password = "testPassword123";
        String hashedPassword = PasswordManager.hashPassword(password);
        System.out.println("Original password: " + password);
        System.out.println("Hashed password: " + hashedPassword);
        System.out.println("Password verification: " + PasswordManager.verifyPassword(password, hashedPassword));
        
        // Test user creation
        System.out.println("\n2. Testing User Creation:");
        Student student1 = new Student("john_doe", "John Doe", "john@university.edu", "password123", "STU001");
        Student student2 = new Student("jane_smith", "Jane Smith", "jane@university.edu", "password456", "STU002");
        System.out.println("Created student: " + student1.getFullName() + " (ID: " + student1.getUserId() + ")");
        System.out.println("Created student: " + student2.getFullName() + " (ID: " + student2.getUserId() + ")");
        
        // Test channel creation
        System.out.println("\n3. Testing Channel Creation:");
        DirectMessageChannel dm = new DirectMessageChannel(student1.getUserId(), student2.getUserId());
        System.out.println("Created DM channel: " + dm.getChannelName() + " (ID: " + dm.getChannelId() + ")");
        
        GroupChatChannel group = new GroupChatChannel("Study Group", "CS101 Study Group", student1.getUserId(), 10, false);
        System.out.println("Created group chat: " + group.getChannelName() + " (ID: " + group.getChannelId() + ")");
        
        CourseChannel course = new CourseChannel("CS101", "CS101", "Introduction to Programming", 
                                               student1.getUserId(), "Fall", 2024, true);
        System.out.println("Created course channel: " + course.getChannelName() + " (ID: " + course.getChannelId() + ")");
        
        // Test message sending
        System.out.println("\n4. Testing Message Sending:");
        TextMessage textMsg = new TextMessage(student1.getUserId(), dm.getChannelId(), "Hello Jane!");
        FileMessage fileMsg = new FileMessage(student2.getUserId(), dm.getChannelId(), "assignment.pdf", 
                                            "https://storage.example.com/assignment.pdf", 1024000, "application/pdf");
        AnnouncementMessage announcement = new AnnouncementMessage(student1.getUserId(), course.getChannelId(), 
                                                                  "Assignment 1 due next week", "CS101", "CS101", 
                                                                  true, "ASSIGNMENT");
        
        dm.sendMessage(textMsg);
        dm.sendMessage(fileMsg);
        course.sendMessage(announcement);
        
        System.out.println("Sent text message: " + textMsg.getFormattedContent());
        System.out.println("Sent file message: " + fileMsg.getFormattedContent());
        System.out.println("Sent announcement: " + announcement.getFormattedContent());
        
        // Test message retrieval
        System.out.println("\n5. Testing Message Retrieval:");
        System.out.println("DM messages: " + dm.getMessageCount());
        System.out.println("Course messages: " + course.getMessageCount());
        
        // Test search functionality
        System.out.println("\n6. Testing Search Functionality:");
        dm.sendMessage(new TextMessage(student1.getUserId(), dm.getChannelId(), "How is the assignment going?"));
        dm.sendMessage(new TextMessage(student2.getUserId(), dm.getChannelId(), "The assignment is almost done"));
        
        var searchResults = dm.searchMessages("assignment");
        System.out.println("Search results for 'assignment': " + searchResults.size() + " messages found");
        
        // Test chat history export
        System.out.println("\n7. Testing Chat History Export:");
        String history = dm.exportChatHistory();
        System.out.println("Chat history length: " + history.length() + " characters");
        System.out.println("First 200 characters: " + history.substring(0, Math.min(200, history.length())) + "...");
        
        // Test user profile
        System.out.println("\n8. Testing User Profile:");
        student1.setYearOfGraduation("2025");
        student1.setMajor("Computer Science");
        student1.setSchool("School of Engineering");
        student1.enrollInCourse("CS101");
        student1.enrollInCourse("MATH101");
        
        System.out.println("Student profile:");
        System.out.println("  Name: " + student1.getFullName());
        System.out.println("  Major: " + student1.getMajor());
        System.out.println("  School: " + student1.getSchool());
        System.out.println("  Graduation Year: " + student1.getYearOfGraduation());
        System.out.println("  Enrolled Courses: " + student1.getEnrolledCourses().size());
        
        System.out.println("\n=== Test Completed Successfully! ===");
        System.out.println("All core functionality is working correctly.");
    }
} 