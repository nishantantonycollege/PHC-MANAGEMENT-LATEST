import java.awt.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import javax.swing.*;

public class LoginFrame extends JFrame {
    private JTextField usernameField;
    private JPasswordField passwordField;
    private int failedAttempts = 0;
    private long lockedUntil = 0;

    // Demo account password hashes (SHA-256), not plain-text passwords.
    private static final String MANAGER_PASSWORD_HASH = "240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9";
    private static final String EMPLOYEE_PASSWORD_HASH = "10176e7b7b24d317acfcf8d2064cfd2f24e154f7b5a96603077d5ef813d6a6b6";
    public LoginFrame() {
        setTitle("PHC Management System - Login");
        setSize(400, 270);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        JLabel titleLabel = new JLabel("PHC Management System", JLabel.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(15, 0, 0, 0));

        JPanel formPanel = new JPanel(new GridLayout(2, 2, 10, 10));
        formPanel.setBorder(BorderFactory.createEmptyBorder(15, 30, 10, 30));
        usernameField = new JTextField();
        passwordField = new JPasswordField();
        formPanel.add(new JLabel("Username:"));
        formPanel.add(usernameField);
        formPanel.add(new JLabel("Password:"));
        formPanel.add(passwordField);

        JButton loginButton = new JButton("Login");
        JLabel hintLabel = new JLabel("Manager: admin / admin123", JLabel.CENTER);
        JLabel employeeHint = new JLabel("Employee: staff / staff123", JLabel.CENTER);
        JPanel bottomPanel = new JPanel(new GridLayout(3, 1, 4, 4));
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(0, 70, 12, 70));
        bottomPanel.add(loginButton);
        bottomPanel.add(hintLabel);
        bottomPanel.add(employeeHint);

        add(titleLabel, BorderLayout.NORTH);
        add(formPanel, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);
        getRootPane().setDefaultButton(loginButton);
        loginButton.addActionListener(e -> login());
    }

    private void login() {
        long now = System.currentTimeMillis();
        if (now < lockedUntil) {
            long seconds = Math.max(1, (lockedUntil - now) / 1000);
            JOptionPane.showMessageDialog(this, "Too many failed attempts. Try again in " + seconds + " seconds.");
            return;
        }

        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());
        String role = null;
        String passwordHash = hashPassword(password);
        if (username.equals("admin") && passwordHash.equals(MANAGER_PASSWORD_HASH)) role = "Manager";
        if (username.equals("staff") && passwordHash.equals(EMPLOYEE_PASSWORD_HASH)) role = "Employee";

        if (role != null) {
            CurrentUser.set(username, role);
            failedAttempts = 0;
            new DashboardFrame().setVisible(true);
            dispose();
        } else {
            failedAttempts++;
            if (failedAttempts >= 3) {
                lockedUntil = System.currentTimeMillis() + 30000;
                failedAttempts = 0;
                JOptionPane.showMessageDialog(this, "Three failed attempts. Login is locked for 30 seconds.", "Login Locked", JOptionPane.WARNING_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Invalid username or password.", "Login Failed", JOptionPane.ERROR_MESSAGE);
            }
            passwordField.setText("");
        }
    }

    private String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest(password.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            StringBuilder result = new StringBuilder();
            for (byte b : bytes) result.append(String.format("%02x", b));
            return result.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is not available.", e);
        }
    }
}
