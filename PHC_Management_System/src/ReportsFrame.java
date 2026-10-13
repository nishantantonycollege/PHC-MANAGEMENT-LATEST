import java.awt.*;
import javax.swing.*;

public class ReportsFrame extends JFrame {
    private JLabel patientsLabel, doctorsLabel, appointmentsLabel, medicinesLabel, equipmentLabel, employeesLabel;

    public ReportsFrame() {
        setTitle("PHC Reports");
        setSize(420, 360);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));
        JLabel titleLabel = new JLabel("PHC Summary Report", JLabel.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(15, 0, 0, 0));
        patientsLabel = new JLabel(); doctorsLabel = new JLabel(); appointmentsLabel = new JLabel();
        medicinesLabel = new JLabel(); equipmentLabel = new JLabel(); employeesLabel = new JLabel();
        JPanel panel = new JPanel(new GridLayout(6, 1, 5, 5));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 60, 10, 40));
        panel.add(patientsLabel); panel.add(doctorsLabel); panel.add(appointmentsLabel);
        panel.add(medicinesLabel); panel.add(equipmentLabel); panel.add(employeesLabel);
        JButton refreshButton = new JButton("Refresh");
        JPanel bottom = new JPanel(); bottom.add(refreshButton);
        add(titleLabel, BorderLayout.NORTH); add(panel, BorderLayout.CENTER); add(bottom, BorderLayout.SOUTH);
        refreshButton.addActionListener(e -> loadCounts());
        loadCounts();
    }
    private void loadCounts() {
        patientsLabel.setText("Patients: " + FileHelper.countRecords("patients.csv"));
        doctorsLabel.setText("Doctors: " + FileHelper.countRecords("doctors.csv"));
        appointmentsLabel.setText("Appointments: " + FileHelper.countRecords("appointments.csv"));
        medicinesLabel.setText("Medicine records: " + FileHelper.countRecords("medicines.csv"));
        equipmentLabel.setText("Equipment records: " + FileHelper.countRecords("equipment.csv"));
        employeesLabel.setText("Employees: " + FileHelper.countRecords("employees.csv"));
    }
}
