package com.unified;

import com.unified.model.*;
import com.unified.util.PasswordManager;

import java.util.*;
import java.io.*;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;

import com.unified.server.CloudStore;

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

    private static final Gson GSON = new Gson();
    private static final Type MAP_STRING_OBJECT =
            new TypeToken<Map<String, Object>>() {}.getType();

    // DTOs for REST payloads
    static class RegisterDto { String username, fullName, email, password, studentId; }
    static class LoginDto    { String username, password; }

    public static void main(String[] args) throws IOException {
        loadUsers();

        // HTTP Server
        int port = Integer.parseInt(System.getenv().getOrDefault("PORT", "8080"));
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/api/health/firestore", ex -> {
            if (handleCorsPreflight(ex)) return;
            var res = CloudStore.ping();
            writeJson(ex, res.ok ? 200 : 500, Map.of(
                "ok", res.ok, "projectId", CloudStore.getProjectId(),
                "id", res.id, "error", res.error
            ));
        });
        
        // Root probe endpoint
        server.createContext("/", new HttpHandler() {
            @Override
            public void handle(HttpExchange exchange) throws IOException {
                allowCors(exchange);
                byte[] body = "Unified server is running".getBytes(StandardCharsets.UTF_8);
                exchange.sendResponseHeaders(200, body.length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(body);
                }
            }
        });

        // ======== REST API ========

        // POST /api/register
        server.createContext("/api/register", ex -> {
            if (handleCorsPreflight(ex)) return;
            try {
                String body = new String(ex.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
                RegisterDto dto = GSON.fromJson(body, RegisterDto.class);

                // Build a Student domain object (password hashing should be handled in Student/PasswordManager)
                Student s = new Student(dto.username, dto.fullName, dto.email, dto.password, dto.studentId);

                // Save locally (JSON) and to Firestore
                users.put(s.getUserId(), s);
                var r = CloudStore.saveUser(s);
                if (!r.ok) {
                    writeJson(ex, 500, Map.of("ok", false, "error", r.error));
                    return;
                }
                writeJson(ex, 200, Map.of("ok", true, "userId", s.getUserId()));

                try { saveUsers(); } catch (IOException ignore) {}

                writeJson(ex, 200, Map.of("ok", true, "userId", s.getUserId()));
            } catch (Exception e) {
                writeJson(ex, 500, Map.of("ok", false, "error", String.valueOf(e)));
            }
        });

        // POST /api/login
        server.createContext("/api/login", ex -> {
            if (handleCorsPreflight(ex)) return;
            try {
                String body = new String(ex.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
                LoginDto dto = GSON.fromJson(body, LoginDto.class);

                // Fetch student by username from Firestore
                Student s = CloudStore.getStudentByUsername(dto.username);
                if (s != null && s.verifyPassword(dto.password)) {
                    writeJson(ex, 200, Map.of("ok", true, "userId", s.getUserId(), "fullName", s.getFullName()));
                } else {
                    writeJson(ex, 401, Map.of("ok", false, "error", "Invalid credentials"));
                }
            } catch (Exception e) {
                writeJson(ex, 500, Map.of("ok", false, "error", String.valueOf(e)));
            }
        });

        // GET /api/channels?userId=...
        // POST /api/channels {ownerId,name,type,participants:[...],description?,maxParticipants?,isPrivate?}
        server.createContext("/api/channels", ex -> {
            if (handleCorsPreflight(ex)) return;
            try {
                if ("GET".equalsIgnoreCase(ex.getRequestMethod())) {
                    String userId = query(ex, "userId");
                    var list = CloudStore.listChannelsByUser(userId);
                    writeJson(ex, 200, Map.of("ok", true, "channels", list));
                } else if ("POST".equalsIgnoreCase(ex.getRequestMethod())) {
                    String body = new String(ex.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
                    Map<String,Object> doc = GSON.fromJson(body, MAP_STRING_OBJECT);
                    doc.putIfAbsent("participants", new ArrayList<String>());
                    String id = CloudStore.createChannel(doc);
                    writeJson(ex, 200, Map.of("ok", true, "channelId", id));
                } else {
                    writeJson(ex, 405, Map.of("ok", false, "error", "Method not allowed"));
                }
            } catch (Exception e) {
                writeJson(ex, 500, Map.of("ok", false, "error", String.valueOf(e)));
            }
        });

        // GET /api/messages?channelId=...
        // POST /api/messages {channelId,senderId,content,type?}
        server.createContext("/api/messages", ex -> {
            if (handleCorsPreflight(ex)) return;
            try {
                if ("GET".equalsIgnoreCase(ex.getRequestMethod())) {
                    String channelId = query(ex, "channelId");
                    var msgs = CloudStore.listMessages(channelId);
                    writeJson(ex, 200, Map.of("ok", true, "messages", msgs));
                } else if ("POST".equalsIgnoreCase(ex.getRequestMethod())) {
                    String body = new String(ex.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
                    Map<String,Object> req = GSON.fromJson(body, MAP_STRING_OBJECT);

                    String channelId = (String) req.get("channelId");
                    Map<String,Object> msgDoc = new HashMap<>();
                    msgDoc.put("senderId", req.get("senderId"));
                    msgDoc.put("content",  req.get("content"));
                    msgDoc.put("type",     req.getOrDefault("type","text"));

                    String messageId = CloudStore.addMessage(channelId, msgDoc);
                    writeJson(ex, 200, Map.of("ok", true, "messageId", messageId));
                } else {
                    writeJson(ex, 405, Map.of("ok", false, "error", "Method not allowed"));
                }
            } catch (Exception e) {
                writeJson(ex, 500, Map.of("ok", false, "error", String.valueOf(e)));
            }
        });

        // Start server
        server.setExecutor(null);
        server.start();
        System.out.println("📡 HTTP API server started on port " + port);

        // CLI flow (kept for local testing)
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

    // ===== Persistence: local JSON (dev only) =====

    private static void loadUsers() {
        Path path = Paths.get("users.json");
        if (Files.exists(path)) {
            try {
                String json = Files.readString(path, StandardCharsets.UTF_8);
                Type t = new TypeToken<Map<String, User>>(){}.getType();

                Gson gson = new GsonBuilder()
                    .registerTypeAdapter(User.class, new UserDeserializer())
                    .create();

                Map<String, User> loaded = gson.fromJson(json, t);
                if (loaded != null) users = loaded;
            } catch (IOException e) {
                System.err.println("Failed to load users.json. Using empty user list.");
            }
        }
    }

    private static final String USER_STORE = "users.json";
    private static void saveUsers() throws IOException {
        String json = new Gson().toJson(users);
        Files.writeString(Paths.get(USER_STORE), json, StandardCharsets.UTF_8);
    }

    /**
     * Custom deserializer for User class that can handle Student objects.
     * Assumes JSON persisted fields exist accordingly.
     */
    private static class UserDeserializer implements JsonDeserializer<User> {
        @Override
        public User deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) {
            JsonObject obj = json.getAsJsonObject();
            if (obj.has("studentId")) {
                String userId = obj.get("userId").getAsString();
                String username = obj.get("username").getAsString();
                String fullName = obj.get("fullName").getAsString();
                String email = obj.get("email").getAsString();
                String hashedPassword = obj.get("hashedPassword").getAsString();
                String studentId = obj.get("studentId").getAsString();
                return new Student(userId, username, fullName, email, hashedPassword, studentId);
            }
            throw new RuntimeException("Unknown user type in JSON");
        }
    }

    // ===== CLI UI =====

    private static void showLoginMenu() {
        System.out.println("\n=== Login Menu ===");
        System.out.println("1. Login");
        System.out.println("2. Register");
        System.out.println("3. Exit");
        System.out.print("Enter your choice: ");

        int choice = getIntInput();
        switch (choice) {
            case 1: login(); break;
            case 2: register(); break;
            case 3: isRunning = false; break;
            default: System.out.println("Invalid choice. Please try again.");
        }
    }

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
            case 1: viewMessages(); break;
            case 2: sendMessage(); break;
            case 3: createChannel(); break;
            case 4: joinChannel(); break;
            case 5: searchMessages(); break;
            case 6: exportChatHistory(); break;
            case 7: viewProfile(); break;
            case 8: editProfile(); break;
            case 9: logout(); break;
            default: System.out.println("Invalid choice. Please try again.");
        }
    }

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

        try { saveUsers(); } catch (IOException e) {
            System.err.println("Failed to save registration: " + e.getMessage());
        }

        // Firestore synchronize (best-effort, non-blocking for CLI UX)
        try {
            var r = CloudStore.saveUser(student);
            if (!r.ok) throw new RuntimeException(r.error);
        } catch (Exception e) {
            System.err.println("Failed to persist to Firestore: " + e);
            System.out.println("Registration aborted due to cloud write failure.");
            return; // stop here so不会误导
        }
        

        System.out.println("Registration successful!");
    }

    private static void viewMessages() {
        System.out.println("\n=== Your Messages ===");
        List<Channel> userChannels = getUserChannels();
        if (userChannels.isEmpty()) {
            System.out.println("You are not part of any channels.");
            return;
        }
        for (int i = 0; i < userChannels.size(); i++) {
            Channel channel = userChannels.get(i);
            List<Message> unread = channel.getUnreadMessages(currentUser.getUserId());
            System.out.printf("%d. %s (%d unread messages)\n",
                    i + 1, channel.getChannelName(), unread.size());
        }
        System.out.print("Enter channel number to view messages (0 to go back): ");
        int choice = getIntInput();
        if (choice > 0 && choice <= userChannels.size()) {
            viewChannelMessages(userChannels.get(choice - 1));
        }
    }

    private static void viewChannelMessages(Channel channel) {
        System.out.println("\n=== " + channel.getChannelName() + " ===");
        List<Message> messages = channel.getMessages();
        if (messages.isEmpty()) {
            System.out.println("No messages in this channel.");
            return;
        }
        for (Message msg : messages) {
            String sender = getSenderName(msg.getSenderId());
            String align = msg.getSenderId().equals(currentUser.getUserId()) ? "RIGHT" : "LEFT";
            System.out.printf("[%s] %s (%s): %s\n",
                    msg.getTimestamp(), sender, align, msg.getFormattedContent());
        }
        channel.markAllMessagesAsRead(currentUser.getUserId());
    }

    private static void sendMessage() {
        System.out.println("\n=== Send Message ===");
        List<Channel> userChannels = getUserChannels();
        if (userChannels.isEmpty()) {
            System.out.println("You are not part of any channels.");
            return;
        }
        for (int i = 0; i < userChannels.size(); i++) {
            System.out.printf("%d. %s\n", i + 1, userChannels.get(i).getChannelName());
        }
        System.out.print("Enter channel number: ");
        int choice = getIntInput();
        if (choice > 0 && choice <= userChannels.size()) {
            Channel ch = userChannels.get(choice - 1);
            System.out.print("Enter your message: ");
            String content = scanner.nextLine();
            TextMessage msg = new TextMessage(currentUser.getUserId(),
                                              ch.getChannelId(), content);
            if (ch.sendMessage(msg)) {
                System.out.println("Message sent successfully!");
            } else {
                System.out.println("Failed to send message.");
            }
        }
    }

    private static void createChannel() {
        System.out.println("\n=== Create Channel ===");
        System.out.println("1. Direct Message");
        System.out.println("2. Group Chat");
        System.out.println("3. Course Channel");
        System.out.print("Enter channel type: ");
        int choice = getIntInput();
        switch (choice) {
            case 1: createDirectMessage(); break;
            case 2: createGroupChat(); break;
            case 3: createCourseChannel(); break;
            default: System.out.println("Invalid choice.");
        }
    }

    private static void createDirectMessage() {
        System.out.print("Enter username of the other user: ");
        String uname = scanner.nextLine();
        User other = findUserByUsername(uname);
        if (other == null) {
            System.out.println("User not found.");
            return;
        }
        DirectMessageChannel dm = new DirectMessageChannel(currentUser.getUserId(),
                                                           other.getUserId());
        channels.put(dm.getChannelId(), dm);
        currentUser.joinChannel(dm.getChannelId());
        other.joinChannel(dm.getChannelId());
        System.out.println("Direct message channel created!");
    }

    private static void createGroupChat() {
        System.out.print("Enter group name: ");
        String name = scanner.nextLine();
        System.out.print("Enter group description: ");
        String desc = scanner.nextLine();
        System.out.print("Enter maximum participants: ");
        int max = getIntInput();
        System.out.print("Is private? (y/n): ");
        boolean priv = scanner.nextLine().toLowerCase().startsWith("y");
        GroupChatChannel grp = new GroupChatChannel(name, desc,
                currentUser.getUserId(), max, priv);
        channels.put(grp.getChannelId(), grp);
        currentUser.joinChannel(grp.getChannelId());
        System.out.println("Group chat created!");
    }

    private static void createCourseChannel() {
        System.out.print("Enter course ID: ");
        String id = scanner.nextLine();
        System.out.print("Enter course code (e.g., CS101): ");
        String code = scanner.nextLine();
        System.out.print("Enter course name: ");
        String name = scanner.nextLine();
        System.out.print("Enter semester: ");
        String sem = scanner.nextLine();
        System.out.print("Enter year: ");
        int year = getIntInput();
        System.out.print("Allow student messages? (y/n): ");
        boolean allow = scanner.nextLine().toLowerCase().startsWith("y");
        CourseChannel cc = new CourseChannel(id, code, name,
                currentUser.getUserId(), sem, year, allow);
        channels.put(cc.getChannelId(), cc);
        currentUser.joinChannel(cc.getChannelId());
        System.out.println("Course channel created!");
    }

    private static void joinChannel() {
        System.out.println("\n=== Join Channel ===");
        List<Channel> avail = getAvailableChannels();
        if (avail.isEmpty()) {
            System.out.println("No channels available to join.");
            return;
        }
        for (int i = 0; i < avail.size(); i++) {
            System.out.printf("%d. %s (%s)\n",
                    i + 1, avail.get(i).getChannelName(), avail.get(i).getChannelType());
        }
        System.out.print("Enter channel number: ");
        int choice = getIntInput();
        if (choice > 0 && choice <= avail.size()) {
            Channel sel = avail.get(choice - 1);
            if (sel.addParticipant(currentUser.getUserId())) {
                currentUser.joinChannel(sel.getChannelId());
                System.out.println("Successfully joined the channel!");
            } else {
                System.out.println("Failed to join the channel.");
            }
        }
    }

    private static void searchMessages() {
        System.out.println("\n=== Search Messages ===");
        System.out.print("Enter search keyword: ");
        String kw = scanner.nextLine();
        List<Message> results = new ArrayList<>();
        for (Channel ch : getUserChannels()) {
            results.addAll(ch.searchMessages(kw));
        }
        if (results.isEmpty()) {
            System.out.println("No messages found matching your search.");
        } else {
            System.out.println("Search results:");
            for (Message m : results) {
                System.out.printf("[%s] %s in %s: %s\n",
                        m.getTimestamp(),
                        getSenderName(m.getSenderId()),
                        getChannelName(m.getChannelId()),
                        m.getFormattedContent());
            }
        }
    }

    private static void exportChatHistory() {
        System.out.println("\n=== Export Chat History ===");
        List<Channel> uc = getUserChannels();
        if (uc.isEmpty()) {
            System.out.println("You are not part of any channels.");
            return;
        }
        for (int i = 0; i < uc.size(); i++) {
            System.out.printf("%d. %s\n", i + 1, uc.get(i).getChannelName());
        }
        System.out.print("Enter channel number: ");
        int choice = getIntInput();
        if (choice > 0 && choice <= uc.size()) {
            Channel sel = uc.get(choice - 1);
            String hist = sel.exportChatHistory();
            String fn = "chat_history_" + sel.getChannelId() + ".txt";
            try (PrintWriter pw = new PrintWriter(new FileWriter(fn))) {
                pw.write(hist);
                System.out.println("Chat history exported to " + fn);
            } catch (IOException e) {
                System.out.println("Failed to export chat history: " + e.getMessage());
            }
        }
    }

    private static void viewProfile() {
        System.out.println("\n=== Your Profile ===");
        System.out.println("Username: " + currentUser.getUsername());
        System.out.println("Full Name: " + currentUser.getFullName());
        System.out.println("Email: " + currentUser.getEmail());
        System.out.println("Year of Graduation: " + currentUser.getYearOfGraduation());
        System.out.println("Major: " + currentUser.getMajor());
        System.out.println("School: " + currentUser.getSchool());
        if (currentUser instanceof Student) {
            Student st = (Student) currentUser;
            System.out.println("Student ID: " + st.getStudentId());
            System.out.println("Enrolled Courses: " + st.getEnrolledCourses().size());
        }
    }

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
                String cp = scanner.nextLine();
                if (currentUser.verifyPassword(cp)) {
                    System.out.print("Enter new password: ");
                    String np = scanner.nextLine();
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

    private static void logout() {
        if (currentUser != null) {
            currentUser.setOnline(false);
            currentUser = null;
        }
        System.out.println("Logged out successfully.");
    }

    // —— Helper Methods ——
    private static int getIntInput() {
        while (!scanner.hasNextInt()) {
            System.out.println("Please enter a valid number.");
            scanner.next();
        }
        int num = scanner.nextInt();
        scanner.nextLine();
        return num;
    }

    private static User findUserByUsername(String username) {
        return users.values().stream()
                .filter(u -> u.getUsername().equals(username))
                .findFirst().orElse(null);
    }

    private static List<Channel> getUserChannels() {
        List<Channel> list = new ArrayList<>();
        for (Channel c : channels.values()) {
            if (c.isParticipant(currentUser.getUserId())) {
                list.add(c);
            }
        }
        return list;
    }

    private static List<Channel> getAvailableChannels() {
        List<Channel> list = new ArrayList<>();
        for (Channel c : channels.values()) {
            if (!c.isParticipant(currentUser.getUserId())) {
                list.add(c);
            }
        }
        return list;
    }

    private static String getSenderName(String senderId) {
        User u = users.get(senderId);
        return u != null ? u.getFullName() : "Unknown User";
    }

    private static String getChannelName(String channelId) {
        Channel c = channels.get(channelId);
        return c != null ? c.getChannelName() : "Unknown Channel";
    }

    private static void allowCors(HttpExchange ex) {
        ex.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        ex.getResponseHeaders().add("Access-Control-Allow-Headers", "content-type");
        ex.getResponseHeaders().add("Access-Control-Allow-Methods", "GET,POST,OPTIONS");
    }

    private static boolean handleCorsPreflight(HttpExchange ex) throws IOException {
        if ("OPTIONS".equalsIgnoreCase(ex.getRequestMethod())) {
            allowCors(ex);
            ex.sendResponseHeaders(204, -1);
            ex.close();
            return true;
        }
        allowCors(ex);
        return false;
    }

    private static void writeJson(HttpExchange ex, int code, Object obj) throws IOException {
        byte[] out = GSON.toJson(obj).getBytes(StandardCharsets.UTF_8);
        ex.getResponseHeaders().add("Content-Type", "application/json; charset=UTF-8");
        ex.sendResponseHeaders(code, out.length);
        try (OutputStream os = ex.getResponseBody()) {
            os.write(out);
        }
    }

    private static String query(HttpExchange ex, String key) {
        String q = ex.getRequestURI().getQuery();
        if (q == null) return null;
        for (String p : q.split("&")) {
            String[] kv = p.split("=", 2);
            if (kv.length == 2 && kv[0].equals(key)) {
                return URLDecoder.decode(kv[1], StandardCharsets.UTF_8);
            }
        }
        return null;
    }
}
