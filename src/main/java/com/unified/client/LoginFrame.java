package com.unified.client;

import javax.swing.*;
import java.awt.*;

class LoginFrame extends JFrame {
    private final ClientController controller;

    private final JTextField usernameField = new JTextField(16);
    private final JPasswordField passwordField = new JPasswordField(16);
    private final JButton loginButton = new JButton("Login");
    private final JButton registerButton = new JButton("Register");

    LoginFrame(ClientController controller) {
        super("Unified - Login");
        this.controller = controller;
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(450, 320));
        setResizable(false);
        buildUI();
        wireEvents();
        pack();
    }

    private void buildUI() {
        // Main container with padding
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));
        mainPanel.setBackground(Color.WHITE);
        
        // Title panel
        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        titlePanel.setBackground(Color.WHITE);
        JLabel titleLabel = new JLabel("Welcome to Unified");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(new Color(51, 98, 140));
        titlePanel.add(titleLabel);
        
        // Form panel
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(Color.WHITE);
        formPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220, 220, 220), 1),
            BorderFactory.createEmptyBorder(25, 25, 25, 25)
        ));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(12, 12, 12, 12);
        
        // Username field
        gbc.gridx = 0; gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.LINE_END;
        JLabel usernameLabel = new JLabel("Username:");
        usernameLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        formPanel.add(usernameLabel, gbc);
        
        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.LINE_START;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        usernameField.setPreferredSize(new Dimension(200, 28));
        usernameField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        formPanel.add(usernameField, gbc);
        
        // Password field
        gbc.gridx = 0; gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.LINE_END;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        JLabel passwordLabel = new JLabel("Password:");
        passwordLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        formPanel.add(passwordLabel, gbc);
        
        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.LINE_START;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        passwordField.setPreferredSize(new Dimension(200, 28));
        passwordField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        formPanel.add(passwordField, gbc);
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 15));
        buttonPanel.setBackground(Color.WHITE);
        
        // Style buttons
        registerButton.setPreferredSize(new Dimension(100, 32));
        registerButton.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        
        loginButton.setPreferredSize(new Dimension(100, 32));
        loginButton.setFont(new Font("Segoe UI", Font.BOLD, 13));
        
        buttonPanel.add(registerButton);
        buttonPanel.add(loginButton);
        
        gbc.gridx = 0; gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        formPanel.add(buttonPanel, gbc);
        
        // Add panels to main container
        mainPanel.add(titlePanel, BorderLayout.NORTH);
        mainPanel.add(formPanel, BorderLayout.CENTER);
        
        setContentPane(mainPanel);
    }

    private void wireEvents() {
        loginButton.addActionListener(e -> attemptLogin());
        registerButton.addActionListener(e -> showRegistration());
        getRootPane().setDefaultButton(loginButton);
    }

    private void attemptLogin() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());
        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter username and password", "Missing info", JOptionPane.WARNING_MESSAGE);
            return;
        }
        boolean ok = controller.login(username, password);
        if (!ok) {
            JOptionPane.showMessageDialog(this, "Invalid username or password", "Login failed", JOptionPane.ERROR_MESSAGE);
            return;
        }
        openMain();
    }

    private void showRegistration() {
        JTextField u = new JTextField();
        JTextField n = new JTextField();
        JTextField em = new JTextField();
        JPasswordField pw = new JPasswordField();
        JTextField sid = new JTextField();

        // Style the fields
        Font fieldFont = new Font("Segoe UI", Font.PLAIN, 13);
        u.setFont(fieldFont);
        n.setFont(fieldFont);
        em.setFont(fieldFont);
        pw.setFont(fieldFont);
        sid.setFont(fieldFont);
        
        u.setPreferredSize(new Dimension(250, 28));
        n.setPreferredSize(new Dimension(250, 28));
        em.setPreferredSize(new Dimension(250, 28));
        pw.setPreferredSize(new Dimension(250, 28));
        sid.setPreferredSize(new Dimension(250, 28));

        JPanel p = new JPanel(new GridBagLayout());
        p.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        p.setBackground(Color.WHITE);
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.anchor = GridBagConstraints.WEST;
        
        Font labelFont = new Font("Segoe UI", Font.PLAIN, 14);
        
        gbc.gridx = 0; gbc.gridy = 0;
        JLabel usernameLabel = new JLabel("Username:");
        usernameLabel.setFont(labelFont);
        p.add(usernameLabel, gbc);
        gbc.gridx = 1;
        p.add(u, gbc);
        
        gbc.gridx = 0; gbc.gridy = 1;
        JLabel nameLabel = new JLabel("Full name:");
        nameLabel.setFont(labelFont);
        p.add(nameLabel, gbc);
        gbc.gridx = 1;
        p.add(n, gbc);
        
        gbc.gridx = 0; gbc.gridy = 2;
        JLabel emailLabel = new JLabel("Email:");
        emailLabel.setFont(labelFont);
        p.add(emailLabel, gbc);
        gbc.gridx = 1;
        p.add(em, gbc);
        
        gbc.gridx = 0; gbc.gridy = 3;
        JLabel passwordLabel = new JLabel("Password:");
        passwordLabel.setFont(labelFont);
        p.add(passwordLabel, gbc);
        gbc.gridx = 1;
        p.add(pw, gbc);
        
        gbc.gridx = 0; gbc.gridy = 4;
        JLabel sidLabel = new JLabel("Student ID:");
        sidLabel.setFont(labelFont);
        p.add(sidLabel, gbc);
        gbc.gridx = 1;
        p.add(sid, gbc);

        int res = JOptionPane.showConfirmDialog(this, p, "Register New Account", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (res != JOptionPane.OK_OPTION) return;

        String err = controller.register(u.getText().trim(), n.getText().trim(), em.getText().trim(), new String(pw.getPassword()), sid.getText().trim());
        if (err != null) {
            JOptionPane.showMessageDialog(this, err, "Registration failed", JOptionPane.ERROR_MESSAGE);
            return;
        }
        JOptionPane.showMessageDialog(this, "Registration successful. You are now logged in.", "Success", JOptionPane.INFORMATION_MESSAGE);
        openMain();
    }

    private void openMain() {
        SwingUtilities.invokeLater(() -> {
            dispose();
            MainFrame main = new MainFrame(controller);
            main.setLocationRelativeTo(null);
            main.setVisible(true);
        });
    }
}
