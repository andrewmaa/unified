package com.unified.client;

import com.unified.model.*;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import java.awt.*;
import java.io.File;
import java.util.List;

class MainFrame extends JFrame {
    private final ClientController controller;

    private final DefaultListModel<Channel> channelModel = new DefaultListModel<>();
    private final JList<Channel> channelList = new JList<>(channelModel);

    private final JTextArea chatArea = new JTextArea();
    private final JTextField input = new JTextField();
    private final JButton send = new JButton("Send");

    private final JButton createBtn = new JButton("New Channel");
    private final JButton joinBtn = new JButton("Join Channel");
    private final JButton searchBtn = new JButton("Search");
    private final JButton exportBtn = new JButton("Export Chat");
    private final JButton profileBtn = new JButton("Profile");
    private final JButton logoutBtn = new JButton("Logout");
    private final JButton sendFileBtn = new JButton("Send File");
    private final JButton announceBtn = new JButton("Announcement");

    MainFrame(ClientController controller) {
        super("Unified - Chats");
        this.controller = controller;
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1000, 640));
        buildUI();
        wireEvents();
        loadChannels();
        pack();
    }

    private void buildUI() {
        // Main split pane with modern styling
        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        split.setResizeWeight(0.28);
        split.setDividerSize(8);
        split.setContinuousLayout(true);
        split.setBorder(null);

        // Left panel - Channel list and controls
        JPanel left = createLeftPanel();
        
        // Right panel - Chat area and input
        JPanel right = createRightPanel();

        split.setLeftComponent(left);
        split.setRightComponent(right);
        setContentPane(split);
    }
    
    private JPanel createLeftPanel() {
        JPanel left = new JPanel(new BorderLayout(8, 8));
        left.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 5));
        left.setBackground(new Color(248, 248, 248));
        
        // Channel list header
        JLabel channelHeader = new JLabel("Channels");
        channelHeader.setFont(new Font("Segoe UI", Font.BOLD, 16));
        channelHeader.setForeground(new Color(51, 98, 140));
        channelHeader.setBorder(BorderFactory.createEmptyBorder(5, 8, 10, 8));
        left.add(channelHeader, BorderLayout.NORTH);

        // Enhanced channel list
        channelList.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof Channel) {
                    Channel ch = (Channel) value;
                    String name = controller.getChannelDisplayName(ch);
                    int unread = controller.getUnreadCount(ch);
                    setText(unread > 0 ? (name + "  (" + unread + ")") : name);
                    setFont(new Font("Segoe UI", Font.PLAIN, 13));
                    setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
                    
                    if (unread > 0) {
                        setFont(new Font("Segoe UI", Font.BOLD, 13));
                    }
                }
                return c;
            }
        });
        
        JScrollPane channelScroll = new JScrollPane(channelList);
        channelScroll.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220, 220, 220), 1),
            BorderFactory.createEmptyBorder(0, 0, 0, 0)
        ));
        channelScroll.getVerticalScrollBar().setUnitIncrement(16);
        left.add(channelScroll, BorderLayout.CENTER);

        // Styled button panel
        JPanel buttonPanel = createButtonPanel();
        left.add(buttonPanel, BorderLayout.SOUTH);
        
        return left;
    }
    
    private JPanel createButtonPanel() {
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        buttonPanel.setBackground(new Color(248, 248, 248));
        
        // Group buttons by category
        JPanel channelOps = new JPanel(new GridLayout(2, 1, 4, 4));
        channelOps.setBackground(new Color(248, 248, 248));
        channelOps.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200)), "Channels"));
        
        styleButton(createBtn, new Color(76, 175, 80), Color.WHITE);
        styleButton(joinBtn, new Color(33, 150, 243), Color.WHITE);
        channelOps.add(createBtn);
        channelOps.add(joinBtn);
        
        JPanel chatOps = new JPanel(new GridLayout(3, 1, 4, 4));
        chatOps.setBackground(new Color(248, 248, 248));
        chatOps.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200)), "Chat"));
        
        styleButton(searchBtn, new Color(156, 39, 176), Color.WHITE);
        styleButton(exportBtn, new Color(255, 152, 0), Color.WHITE);
        styleButton(sendFileBtn, new Color(96, 125, 139), Color.WHITE);
        chatOps.add(searchBtn);
        chatOps.add(exportBtn);
        chatOps.add(sendFileBtn);
        
        JPanel userOps = new JPanel(new GridLayout(3, 1, 4, 4));
        userOps.setBackground(new Color(248, 248, 248));
        userOps.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200)), "User"));
        
        styleButton(announceBtn, new Color(255, 193, 7), Color.BLACK);
        styleButton(profileBtn, new Color(158, 158, 158), Color.WHITE);
        styleButton(logoutBtn, new Color(244, 67, 54), Color.WHITE);
        userOps.add(announceBtn);
        userOps.add(profileBtn);
        userOps.add(logoutBtn);
        
        buttonPanel.add(channelOps);
        buttonPanel.add(Box.createVerticalStrut(8));
        buttonPanel.add(chatOps);
        buttonPanel.add(Box.createVerticalStrut(8));
        buttonPanel.add(userOps);
        
        return buttonPanel;
    }
    
    private void styleButton(JButton button, Color bgColor, Color textColor) {
        button.setBackground(bgColor);
        button.setForeground(textColor);
        button.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setOpaque(true);
        button.setPreferredSize(new Dimension(120, 28));
    }
    
    private JPanel createRightPanel() {
        JPanel right = new JPanel(new BorderLayout(8, 8));
        right.setBorder(BorderFactory.createEmptyBorder(10, 5, 10, 10));
        right.setBackground(Color.WHITE);
        
        // Chat header
        JLabel chatHeader = new JLabel("Messages");
        chatHeader.setFont(new Font("Segoe UI", Font.BOLD, 16));
        chatHeader.setForeground(new Color(51, 98, 140));
        chatHeader.setBorder(BorderFactory.createEmptyBorder(5, 8, 10, 8));
        right.add(chatHeader, BorderLayout.NORTH);

        // Enhanced chat area
        chatArea.setEditable(false);
        chatArea.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        chatArea.setLineWrap(true);
        chatArea.setWrapStyleWord(true);
        chatArea.setBackground(new Color(250, 250, 250));
        chatArea.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        JScrollPane chatScroll = new JScrollPane(chatArea);
        chatScroll.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220, 220, 220), 1),
            BorderFactory.createEmptyBorder(0, 0, 0, 0)
        ));
        chatScroll.getVerticalScrollBar().setUnitIncrement(16);
        right.add(chatScroll, BorderLayout.CENTER);

        // Enhanced input area
        JPanel inputPanel = new JPanel(new BorderLayout(8, 8));
        inputPanel.setBorder(BorderFactory.createEmptyBorder(8, 0, 0, 0));
        inputPanel.setBackground(Color.WHITE);
        
        input.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        input.setPreferredSize(new Dimension(0, 32));
        input.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
            BorderFactory.createEmptyBorder(6, 10, 6, 10)
        ));
        
        send.setFont(new Font("Segoe UI", Font.BOLD, 13));
        send.setPreferredSize(new Dimension(80, 32));
        send.setBackground(new Color(51, 98, 140));
        send.setForeground(Color.WHITE);
        send.setFocusPainted(false);
        send.setBorderPainted(false);
        send.setOpaque(true);
        
        inputPanel.add(input, BorderLayout.CENTER);
        inputPanel.add(send, BorderLayout.EAST);
        right.add(inputPanel, BorderLayout.SOUTH);
        
        return right;
    }

    private void wireEvents() {
        channelList.addListSelectionListener(this::onChannelSelected);
        send.addActionListener(e -> onSend());
        input.addActionListener(e -> onSend());

        createBtn.addActionListener(e -> onCreateChannel());
        joinBtn.addActionListener(e -> onJoinChannel());
        searchBtn.addActionListener(e -> onSearch());
        exportBtn.addActionListener(e -> onExport());
        profileBtn.addActionListener(e -> onProfile());
        logoutBtn.addActionListener(e -> onLogout());
        sendFileBtn.addActionListener(e -> onSendFile());
        announceBtn.addActionListener(e -> onAnnounce());
    }

    private void loadChannels() {
        channelModel.clear();
        List<Channel> list = controller.getUserChannels();
        for (Channel c : list) channelModel.addElement(c);
        if (!list.isEmpty()) channelList.setSelectedIndex(0);
    }

    private void refreshMessages(Channel c) {
        chatArea.setText("");
        if (c == null) return;
        List<Message> msgs = controller.getMessages(c);
        User cur = controller.getCurrentUser();
        
        for (Message m : msgs) {
            boolean mine = cur != null && m.getSenderId().equals(cur.getUserId());
            String name = controller.getUserDisplayName(m.getSenderId());
            String timestamp = m.getTimestamp().toString().replace("T", " ");
            String content = m.getFormattedContent();
            
            // Format message with better visual separation
            if (mine) {
                chatArea.append(String.format("▶ %s (You) - %s\n", name, timestamp));
                chatArea.append(String.format("  %s\n\n", content));
            } else {
                chatArea.append(String.format("◀ %s - %s\n", name, timestamp));
                chatArea.append(String.format("  %s\n\n", content));
            }
        }
        
        controller.markAllRead(c);
        channelList.repaint();
        
        // Auto-scroll to bottom
        SwingUtilities.invokeLater(() -> {
            chatArea.setCaretPosition(chatArea.getDocument().getLength());
        });
    }

    private void onChannelSelected(ListSelectionEvent e) {
        if (e.getValueIsAdjusting()) return;
        Channel c = channelList.getSelectedValue();
        refreshMessages(c);
        input.setEnabled(controller.canSendIn(c));
        send.setEnabled(controller.canSendIn(c));
    }

    private void onSend() {
        Channel c = channelList.getSelectedValue();
        if (c == null) return;
        String text = input.getText().trim();
        if (text.isEmpty()) return;
        if (!controller.canSendIn(c)) {
            JOptionPane.showMessageDialog(this, "You don't have permission to send in this channel", "Info", JOptionPane.WARNING_MESSAGE);
            return;
        }
        boolean ok = controller.sendTextMessage(c, text);
        if (ok) {
            input.setText("");
            refreshMessages(c);
        }
    }

    private void onCreateChannel() {
        String[] opts = {"Direct Message", "Group Chat", "Course Channel"};
        int idx = JOptionPane.showOptionDialog(this, "Select channel type", "New Channel",
                JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null, opts, opts[0]);
        if (idx == 0) {
            String other = JOptionPane.showInputDialog(this, "Enter the other username:");
            if (other == null || other.trim().isEmpty()) return;
            Channel ch = controller.createDirectMessage(other.trim());
            if (ch == null) {
                JOptionPane.showMessageDialog(this, "User not found", "Failed", JOptionPane.ERROR_MESSAGE);
                return;
            }
            channelModel.addElement(ch);
            channelList.setSelectedValue(ch, true);
        } else if (idx == 1) {
            JTextField name = new JTextField();
            JTextField desc = new JTextField();
            JSpinner max = new JSpinner(new SpinnerNumberModel(10, 2, 1000, 1));
            JCheckBox priv = new JCheckBox("Private (invite only)");
            JPanel p = new JPanel(new GridLayout(0, 1, 6, 6));
            p.add(new JLabel("Name:")); p.add(name);
            p.add(new JLabel("Description:")); p.add(desc);
            p.add(new JLabel("Max participants:")); p.add(max);
            p.add(priv);
            int res = JOptionPane.showConfirmDialog(this, p, "New Group Chat", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            if (res != JOptionPane.OK_OPTION) return;
            Channel ch = controller.createGroupChat(name.getText().trim(), desc.getText().trim(), (Integer) max.getValue(), priv.isSelected());
            if (ch != null) {
                channelModel.addElement(ch);
                channelList.setSelectedValue(ch, true);
            }
        } else if (idx == 2) {
            JTextField cid = new JTextField();
            JTextField code = new JTextField();
            JTextField name = new JTextField();
            JTextField sem = new JTextField("Fall");
            JSpinner year = new JSpinner(new SpinnerNumberModel(2025, 2000, 2100, 1));
            JCheckBox allow = new JCheckBox("Allow student messages", true);
            JPanel p = new JPanel(new GridLayout(0, 1, 6, 6));
            p.add(new JLabel("Course ID:")); p.add(cid);
            p.add(new JLabel("Course code:")); p.add(code);
            p.add(new JLabel("Course name:")); p.add(name);
            p.add(new JLabel("Semester:")); p.add(sem);
            p.add(new JLabel("Year:")); p.add(year);
            p.add(allow);
            int res = JOptionPane.showConfirmDialog(this, p, "New Course Channel", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            if (res != JOptionPane.OK_OPTION) return;
            Channel ch = controller.createCourseChannel(cid.getText().trim(), code.getText().trim(), name.getText().trim(), sem.getText().trim(), (Integer) year.getValue(), allow.isSelected());
            if (ch != null) {
                channelModel.addElement(ch);
                channelList.setSelectedValue(ch, true);
            }
        }
    }

    private void onJoinChannel() {
        List<Channel> avail = controller.getAvailableChannels();
        if (avail.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No channels available", "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        Channel sel = (Channel) JOptionPane.showInputDialog(this, "Select a channel to join:", "Join Channel",
                JOptionPane.PLAIN_MESSAGE, null, avail.toArray(), avail.get(0));
        if (sel == null) return;
        if (controller.joinChannel(sel)) {
            if (!channelModel.contains(sel)) channelModel.addElement(sel);
            channelList.setSelectedValue(sel, true);
        }
    }

    private void onSearch() {
        String kw = JOptionPane.showInputDialog(this, "Enter keyword:");
        if (kw == null || kw.trim().isEmpty()) return;
        List<Message> rs = controller.searchMessages(kw.trim());
        if (rs.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No messages found", "Search Results", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        JTextArea area = new JTextArea(20, 60);
        area.setEditable(false);
        for (Message m : rs) {
            String channelName = controller.getChannelDisplayName(findChannelById(m.getChannelId()));
            String name = controller.getUserDisplayName(m.getSenderId());
            area.append(String.format("[%s] (%s) %s: %s\n", m.getTimestamp(), channelName, name, m.getFormattedContent()));
        }
        JOptionPane.showMessageDialog(this, new JScrollPane(area), "Search Results", JOptionPane.PLAIN_MESSAGE);
    }

    private Channel findChannelById(String id) {
        for (int i = 0; i < channelModel.size(); i++) {
            Channel c = channelModel.get(i);
            if (c.getChannelId().equals(id)) return c;
        }
        return null;
    }

    private void onExport() {
        Channel c = channelList.getSelectedValue();
        if (c == null) return;
        JFileChooser fc = new JFileChooser();
        fc.setSelectedFile(new File("chat_" + c.getChannelId() + ".txt"));
        int res = fc.showSaveDialog(this);
        if (res == JFileChooser.APPROVE_OPTION) {
            boolean ok = controller.exportChatHistory(c, fc.getSelectedFile());
            JOptionPane.showMessageDialog(this, ok ? "Exported" : "Export failed", "Export Chat", ok ? JOptionPane.INFORMATION_MESSAGE : JOptionPane.ERROR_MESSAGE);
        }
    }

    private void onProfile() {
        User u = controller.getCurrentUser();
        if (u == null) return;
        JTextField name = new JTextField(u.getFullName());
        JTextField email = new JTextField(u.getEmail());
        JTextField year = new JTextField(u.getYearOfGraduation());
        JTextField major = new JTextField(u.getMajor());
        JTextField school = new JTextField(u.getSchool());
        JPanel p = new JPanel(new GridLayout(0, 1, 6, 6));
        p.add(new JLabel("Full name:")); p.add(name);
        p.add(new JLabel("Email:")); p.add(email);
        p.add(new JLabel("Graduation year:")); p.add(year);
        p.add(new JLabel("Major:")); p.add(major);
        p.add(new JLabel("School:")); p.add(school);
        int res = JOptionPane.showConfirmDialog(this, p, "My Profile", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (res != JOptionPane.OK_OPTION) return;
        controller.updateProfile(name.getText().trim(), email.getText().trim(), year.getText().trim(), major.getText().trim(), school.getText().trim());
        JOptionPane.showMessageDialog(this, "Saved", "Profile", JOptionPane.INFORMATION_MESSAGE);
    }

    private void onLogout() {
        controller.logout();
        SwingUtilities.invokeLater(() -> {
            dispose();
            LoginFrame lf = new LoginFrame(controller);
            lf.setLocationRelativeTo(null);
            lf.setVisible(true);
        });
    }

    private void onSendFile() {
        Channel c = channelList.getSelectedValue();
        if (c == null) return;
        JFileChooser fc = new JFileChooser();
        int res = fc.showOpenDialog(this);
        if (res != JFileChooser.APPROVE_OPTION) return;
        File f = fc.getSelectedFile();
        String type = JOptionPane.showInputDialog(this, "MIME type:", "application/octet-stream");
        if (type == null || type.isBlank()) type = "application/octet-stream";
        boolean ok = controller.sendFileMessage(c, f.getName(), f.toURI().toString(), f.length(), type);
        if (ok) refreshMessages(c);
    }

    private void onAnnounce() {
        Channel c = channelList.getSelectedValue();
        if (!(c instanceof CourseChannel)) {
            JOptionPane.showMessageDialog(this, "Only course channels support announcement", "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        CourseChannel cc = (CourseChannel) c;
        JTextArea content = new JTextArea(5, 30);
        JCheckBox important = new JCheckBox("Important");
        String[] types = {"ASSIGNMENT", "EXAM", "DEADLINE", "GRADE", "GENERAL"};
        JComboBox<String> type = new JComboBox<>(types);
        JPanel p = new JPanel(new BorderLayout(6, 6));
        p.add(new JScrollPane(content), BorderLayout.CENTER);
        JPanel south = new JPanel(new FlowLayout(FlowLayout.LEFT));
        south.add(important);
        south.add(new JLabel("Type:"));
        south.add(type);
        p.add(south, BorderLayout.SOUTH);
        int res = JOptionPane.showConfirmDialog(this, p, "Announcement", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (res != JOptionPane.OK_OPTION) return;
        boolean ok = controller.sendAnnouncement(cc, content.getText().trim(), important.isSelected(), (String) type.getSelectedItem());
        if (ok) refreshMessages(cc);
    }
}
