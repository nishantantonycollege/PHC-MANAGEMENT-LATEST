import java.awt.*;
import javax.swing.*;

public class DashboardFrame extends JFrame {
    public DashboardFrame() {
        setTitle("PHC Management System - Dashboard");
        setSize(560, 480);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        JLabel titleLabel = new JLabel("PHC Management System", JLabel.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 22));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(12, 0, 0, 0));
        JLabel welcomeLabel = new JLabel("Logged in as: " + CurrentUser.getUsername() + " (" + CurrentUser.getRole() + ")", JLabel.CENTER);

        JButton patientsButton = new JButton("Patients");
        JButton doctorsButton = new JButton("Doctors");
        JButton appointmentsButton = new JButton("Appointments");
        JButton medicinesButton = new JButton("Pharmacy");
        JButton equipmentButton = new JButton("Equipment");
        JButton employeesButton = new JButton("Employees");
        JButton reportsButton = new JButton("Reports");
        JButton logoutButton = new JButton("Logout");

        JPanel topPanel = new JPanel(new GridLayout(2, 1));
        topPanel.add(titleLabel);
        topPanel.add(welcomeLabel);
        JPanel buttonPanel = new JPanel(new GridLayout(4, 2, 12, 12));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(15, 35, 25, 35));
        buttonPanel.add(patientsButton);
        buttonPanel.add(doctorsButton);
        buttonPanel.add(appointmentsButton);
        buttonPanel.add(medicinesButton);
        buttonPanel.add(equipmentButton);
        buttonPanel.add(employeesButton);
        buttonPanel.add(reportsButton);
        buttonPanel.add(logoutButton);
        add(topPanel, BorderLayout.NORTH);
        add(buttonPanel, BorderLayout.CENTER);

        employeesButton.setEnabled(CurrentUser.isManager());
        reportsButton.setEnabled(CurrentUser.isManager());
        patientsButton.addActionListener(e -> new PatientFrame().setVisible(true));
        doctorsButton.addActionListener(e -> new DoctorFrame().setVisible(true));
        appointmentsButton.addActionListener(e -> new AppointmentFrame().setVisible(true));
        medicinesButton.addActionListener(e -> new MedicineFrame().setVisible(true));
        equipmentButton.addActionListener(e -> new EquipmentFrame().setVisible(true));
        employeesButton.addActionListener(e -> new EmployeeFrame().setVisible(true));
        reportsButton.addActionListener(e -> new ReportsFrame().setVisible(true));
        logoutButton.addActionListener(e -> {
            int choice = JOptionPane.showConfirmDialog(this, "Do you want to logout?", "Logout", JOptionPane.YES_NO_OPTION);
            if (choice == JOptionPane.YES_OPTION) {
                CurrentUser.clear();
                new LoginFrame().setVisible(true);
                dispose();
            }
        });
    }
}
