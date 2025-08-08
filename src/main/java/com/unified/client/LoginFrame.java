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
        setMinimumSize(new Dimension(380, 240));
        buildUI();
        wireEvents();
        pack();
    }

    private void buildUI() {
        JPanel root = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 8, 6, 8);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.LINE_END;
        root.add(new JLabel("Username:"), gbc);
        gbc.gridy++;
        root.add(new JLabel("Password:"), gbc);

        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.LINE_START;
        root.add(usernameField, gbc);
        gbc.gridy++;
        root.add(passwordField, gbc);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttons.add(registerButton);
        buttons.add(loginButton);

        gbc.gridx = 0;
        gbc.gridy++;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.LINE_END;
        root.add(buttons, gbc);

        setContentPane(root);
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

        JPanel p = new JPanel(new GridLayout(0, 1, 6, 6));
        p.add(new JLabel("Username:"));
        p.add(u);
        p.add(new JLabel("Full name:"));
        p.add(n);
        p.add(new JLabel("Email:"));
        p.add(em);
        p.add(new JLabel("Password:"));
        p.add(pw);
        p.add(new JLabel("Student ID:"));
        p.add(sid);

        int res = JOptionPane.showConfirmDialog(this, p, "Register", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
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
