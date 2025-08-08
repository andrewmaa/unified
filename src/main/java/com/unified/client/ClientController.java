package com.unified.client;

import com.unified.model.*;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

public class ClientController {
    private static final String USERS_CSV = "users.csv";

    private final Map<String, User> users = new HashMap<>();
    private final Map<String, Channel> channels = new LinkedHashMap<>();
    private User currentUser;

    public ClientController() {
        loadUsersCsv();
    }

    public boolean login(String username, String password) {
        User u = findUserByUsername(username);
        if (u != null && u.verifyPassword(password)) {
            currentUser = u;
            currentUser.setOnline(true);
            return true;
        }
        return false;
    }

    public String register(String username, String fullName, String email, String password, String studentId) {
        if (findUserByUsername(username) != null) {
            return "Username already exists";
        }
        Student s = new Student(username, fullName, email, password, studentId);
        users.put(s.getUserId(), s);
        currentUser = s;
        currentUser.setOnline(true);
        saveUsersCsv();
        return null;
    }

    public void logout() {
        if (currentUser != null) currentUser.setOnline(false);
        currentUser = null;
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public User findUserByUsername(String username) {
        for (User u : users.values()) {
            if (u.getUsername().equals(username)) return u;
        }
        return null;
    }

    public String getUserDisplayName(String userId) {
        User u = users.get(userId);
        if (u == null) return "Unknown user";
        String name = u.getFullName();
        return (name == null || name.isBlank()) ? u.getUsername() : name;
    }

    public List<Channel> getUserChannels() {
        if (currentUser == null) return List.of();
        return channels.values().stream()
                .filter(c -> c.isParticipant(currentUser.getUserId()))
                .collect(Collectors.toList());
    }

    public List<Channel> getAvailableChannels() {
        if (currentUser == null) return List.of();
        return channels.values().stream()
                .filter(c -> !c.isParticipant(currentUser.getUserId()))
                .collect(Collectors.toList());
    }

    public String getChannelDisplayName(Channel c) {
        if (c instanceof DirectMessageChannel) {
            DirectMessageChannel dm = (DirectMessageChannel) c;
            String otherId = dm.getOtherUserId(currentUser.getUserId());
            if (otherId != null) return "DM · " + getUserDisplayName(otherId);
        }
        return c.getChannelName();
    }

    public Channel createDirectMessage(String otherUsername) {
        if (currentUser == null) return null;
        User other = findUserByUsername(otherUsername);
        if (other == null) return null;
        DirectMessageChannel dm = new DirectMessageChannel(currentUser.getUserId(), other.getUserId());
        channels.put(dm.getChannelId(), dm);
        currentUser.joinChannel(dm.getChannelId());
        other.joinChannel(dm.getChannelId());
        return dm;
    }

    public Channel createGroupChat(String name, String desc, int max, boolean isPrivate) {
        if (currentUser == null) return null;
        GroupChatChannel grp = new GroupChatChannel(name, desc, currentUser.getUserId(), max, isPrivate);
        channels.put(grp.getChannelId(), grp);
        currentUser.joinChannel(grp.getChannelId());
        return grp;
    }

    public Channel createCourseChannel(String courseId, String code, String name, String sem, int year, boolean allowStudent) {
        if (currentUser == null) return null;
        CourseChannel cc = new CourseChannel(courseId, code, name, currentUser.getUserId(), sem, year, allowStudent);
        channels.put(cc.getChannelId(), cc);
        currentUser.joinChannel(cc.getChannelId());
        return cc;
    }

    public boolean joinChannel(Channel c) {
        if (currentUser == null || c == null) return false;
        boolean ok = c.addParticipant(currentUser.getUserId());
        if (ok) currentUser.joinChannel(c.getChannelId());
        return ok;
    }

    public boolean canSendIn(Channel c) {
        if (currentUser == null || c == null) return false;
        if (c instanceof CourseChannel) {
            CourseChannel cc = (CourseChannel) c;
            if (!cc.isAllowStudentMessages() && !cc.isInstructor(currentUser.getUserId())) {
                return false;
            }
        }
        return c.isParticipant(currentUser.getUserId());
    }

    public boolean sendTextMessage(Channel c, String content) {
        if (currentUser == null || c == null) return false;
        TextMessage m = new TextMessage(currentUser.getUserId(), c.getChannelId(), content);
        return c.sendMessage(m);
    }

    public boolean sendFileMessage(Channel c, String fileName, String url, long size, String type) {
        if (currentUser == null || c == null) return false;
        FileMessage m = new FileMessage(currentUser.getUserId(), c.getChannelId(), fileName, url, size, type);
        return c.sendMessage(m);
    }

    public boolean sendAnnouncement(CourseChannel c, String content, boolean important, String type) {
        if (currentUser == null || c == null) return false;
        AnnouncementMessage m = new AnnouncementMessage(currentUser.getUserId(), c.getChannelId(), content,
                c.getCourseId(), c.getCourseIdentifier(), important, type);
        return c.sendMessage(m);
    }

    public List<Message> getMessages(Channel c) {
        return (c == null) ? List.of() : c.getMessages();
    }

    public void markAllRead(Channel c) {
        if (currentUser != null && c != null) c.markAllMessagesAsRead(currentUser.getUserId());
    }

    public int getUnreadCount(Channel c) {
        if (currentUser == null || c == null) return 0;
        return c.getUnreadMessages(currentUser.getUserId()).size();
    }

    public List<Message> searchMessages(String keyword) {
        if (currentUser == null) return List.of();
        List<Message> res = new ArrayList<>();
        for (Channel ch : getUserChannels()) {
            res.addAll(ch.searchMessages(keyword));
        }
        return res;
    }

    public boolean exportChatHistory(Channel c, File target) {
        if (c == null || target == null) return false;
        String hist = c.exportChatHistory();
        try (Writer w = new OutputStreamWriter(new FileOutputStream(target), StandardCharsets.UTF_8)) {
            w.write(hist);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    public void updateProfile(String fullName, String email, String year, String major, String school) {
        if (currentUser == null) return;
        currentUser.setFullName(fullName);
        currentUser.setEmail(email);
        currentUser.setYearOfGraduation(year);
        currentUser.setMajor(major);
        currentUser.setSchool(school);
        saveUsersCsv();
    }

    private void loadUsersCsv() {
        Path p = Paths.get(USERS_CSV);
        if (!Files.exists(p)) return;
        try (BufferedReader br = Files.newBufferedReader(p, StandardCharsets.UTF_8)) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.isBlank() || line.startsWith("#")) continue;
                String[] parts = line.split("\t");
                if (parts.length < 6) continue;
                String username = parts[0];
                String fullName = parts[1];
                String email = parts[2];
                String hash = parts[3];
                String studentId = parts[4];
                String userId = parts[5];
                Student s = new Student(userId, username, fullName, email, hash, studentId);
                users.put(s.getUserId(), s);
            }
        } catch (IOException ignored) {}
    }

    private void saveUsersCsv() {
        Path p = Paths.get(USERS_CSV);
        try (BufferedWriter bw = Files.newBufferedWriter(p, StandardCharsets.UTF_8)) {
            bw.write("# username\tfullName\temail\thash\tstudentId\tuserId\n");
            for (User u : users.values()) {
                String hash = "";
                try {
                    var f = u.getClass().getSuperclass().getDeclaredField("hashedPassword");
                    f.setAccessible(true);
                    Object v = f.get(u);
                    if (v != null) hash = v.toString();
                } catch (Exception ignored) {}
                String studentId = (u instanceof Student) ? ((Student) u).getStudentId() : "";
                String line = String.join("\t",
                        safe(u.getUsername()),
                        safe(u.getFullName()),
                        safe(u.getEmail()),
                        safe(hash),
                        safe(studentId),
                        safe(u.getUserId())
                );
                bw.write(line);
                bw.write('\n');
            }
        } catch (IOException ignored) {}
    }

    private static String safe(String s) {
        return s == null ? "" : s.replace('\t', ' ');
    }
}
