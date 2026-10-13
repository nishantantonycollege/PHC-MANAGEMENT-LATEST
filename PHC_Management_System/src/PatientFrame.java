public class PatientFrame extends ManageFrame {
    public PatientFrame() {
        super("Patient Management", new String[] {"ID", "Name", "Age", "Gender", "Phone", "Address", "Registration Date"}, "patients.csv", new int[] {2});
    }
}
