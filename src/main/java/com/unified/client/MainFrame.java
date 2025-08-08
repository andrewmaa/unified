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
        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        split.setResizeWeight(0.25);

        JPanel left = new JPanel(new BorderLayout(6, 6));
        channelList.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof Channel) {
                    Channel ch = (Channel) value;
                    String name = controller.getChannelDisplayName(ch);
                    int unread = controller.getUnreadCount(ch);
                    setText(unread > 0 ? (name + "  (" + unread + ")") : name);
                }
                return c;
            }
        });
        left.add(new JScrollPane(channelList), BorderLayout.CENTER);

        JPanel leftBtns = new JPanel(new GridLayout(0, 1, 6, 6));
        leftBtns.add(createBtn);
        leftBtns.add(joinBtn);
        leftBtns.add(searchBtn);
        leftBtns.add(exportBtn);
        leftBtns.add(sendFileBtn);
        leftBtns.add(announceBtn);
        leftBtns.add(profileBtn);
        leftBtns.add(logoutBtn);
        left.add(leftBtns, BorderLayout.SOUTH);

        JPanel right = new JPanel(new BorderLayout(6, 6));
        chatArea.setEditable(false);
        right.add(new JScrollPane(chatArea), BorderLayout.CENTER);

        JPanel inputBar = new JPanel(new BorderLayout(8, 8));
        inputBar.add(input, BorderLayout.CENTER);
        inputBar.add(send, BorderLayout.EAST);
        right.add(inputBar, BorderLayout.SOUTH);

        split.setLeftComponent(left);
        split.setRightComponent(right);
        setContentPane(split);
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
            String align = mine ? "(Me)" : "";
            String name = controller.getUserDisplayName(m.getSenderId());
            chatArea.append(String.format("[%s] %s%s: %s\n", m.getTimestamp(), name, align, m.getFormattedContent()));
        }
        controller.markAllRead(c);
        channelList.repaint();
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
