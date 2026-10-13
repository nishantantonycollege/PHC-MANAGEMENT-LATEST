import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;

// A simple reusable form and table for the main PHC records.
public class ManageFrame extends JFrame {
    private String[] columns;
    private String fileName;
    private int[] numberColumns;
    private JTextField[] fields;
    private DefaultTableModel model;
    private JTable table;
    private TableRowSorter<DefaultTableModel> sorter;
    private JComboBox<String> searchColumn;
    private JTextField searchField;

    public ManageFrame(String title, String[] columns, String fileName, int[] numberColumns) {
        this.columns = columns;
        this.fileName = fileName;
        this.numberColumns = numberColumns;
        setTitle(title);
        setSize(900, 500);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        JPanel formPanel = new JPanel(new GridLayout(columns.length, 2, 5, 6));
        formPanel.setBorder(BorderFactory.createTitledBorder("Enter Details"));
        fields = new JTextField[columns.length];
        for (int i = 0; i < columns.length; i++) {
            formPanel.add(new JLabel(columns[i] + ":"));
            fields[i] = new JTextField();
            formPanel.add(fields[i]);
        }
        JButton addButton = new JButton("Add");
        JButton updateButton = new JButton("Update Selected");
        JButton clearButton = new JButton("Clear");
        JButton deleteButton = new JButton("Delete Selected");
        deleteButton.setEnabled(CurrentUser.isManager());
        JPanel buttonPanel = new JPanel(new GridLayout(4, 1, 5, 5));
        buttonPanel.add(addButton);
        buttonPanel.add(updateButton);
        buttonPanel.add(clearButton);
        buttonPanel.add(deleteButton);
        JPanel leftPanel = new JPanel(new BorderLayout(8, 8));
        leftPanel.setPreferredSize(new Dimension(280, 0));
        leftPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 0));
        leftPanel.add(new JScrollPane(formPanel), BorderLayout.CENTER);
        leftPanel.add(buttonPanel, BorderLayout.SOUTH);

        model = new DefaultTableModel(columns, 0) {
            public boolean isCellEditable(int row, int column) { return false; }
        };
        table = new JTable(model);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        sorter = new TableRowSorter<>(model);
        table.setRowSorter(sorter);
        String[] searchOptions = new String[columns.length + 1];
        searchOptions[0] = "All Columns";
        for (int i = 0; i < columns.length; i++) searchOptions[i + 1] = columns[i];
        searchColumn = new JComboBox<>(searchOptions);
        searchField = new JTextField(15);
        JButton clearSearchButton = new JButton("Clear Search");
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.add(new JLabel("Search:"));
        searchPanel.add(searchColumn);
        searchPanel.add(searchField);
        searchPanel.add(clearSearchButton);
        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 10));
        tablePanel.add(searchPanel, BorderLayout.NORTH);
        tablePanel.add(new JScrollPane(table), BorderLayout.CENTER);
        add(leftPanel, BorderLayout.WEST);
        add(tablePanel, BorderLayout.CENTER);

        loadData();
        addButton.addActionListener(e -> addRecord());
        updateButton.addActionListener(e -> updateRecord());
        clearButton.addActionListener(e -> clearFields());
        deleteButton.addActionListener(e -> deleteRecord());
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && table.getSelectedRow() >= 0) fillFieldsFromSelectedRow();
        });
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { applySearch(); }
            public void removeUpdate(DocumentEvent e) { applySearch(); }
            public void changedUpdate(DocumentEvent e) { applySearch(); }
        });
        searchColumn.addActionListener(e -> applySearch());
        clearSearchButton.addActionListener(e -> searchField.setText(""));
    }

    private void applySearch() {
        String text = searchField.getText().trim();
        if (text.isEmpty()) { sorter.setRowFilter(null); return; }
        String pattern = "(?i)" + java.util.regex.Pattern.quote(text);
        int selected = searchColumn.getSelectedIndex();
        sorter.setRowFilter(selected == 0 ? RowFilter.regexFilter(pattern) : RowFilter.regexFilter(pattern, selected - 1));
    }

    private String[] readForm() {
        String[] values = new String[columns.length];
        for (int i = 0; i < columns.length; i++) {
            values[i] = fields[i].getText().trim().replace(",", " ");
            if (values[i].isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please enter " + columns[i] + ".");
                return null;
            }
        }
        for (int col : numberColumns) {
            try {
                double value = Double.parseDouble(values[col]);
                if (value < 0) throw new NumberFormatException();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, columns[col] + " must be a non-negative number.");
                return null;
            }
        }
        for (int i = 0; i < columns.length; i++) {
            String name = columns[i].toLowerCase();
            if (name.contains("date") || name.equals("expiry") || name.equals("next due")) {
                try { LocalDate.parse(values[i]); }
                catch (DateTimeParseException ex) {
                    JOptionPane.showMessageDialog(this, columns[i] + " must use YYYY-MM-DD format.");
                    return null;
                }
            }
        }
        return values;
    }

    private void addRecord() {
        String[] values = readForm();
        if (values == null) return;
        for (int r = 0; r < model.getRowCount(); r++) {
            if (model.getValueAt(r, 0).toString().equalsIgnoreCase(values[0])) {
                JOptionPane.showMessageDialog(this, "This ID already exists."); return;
            }
        }
        if (isAppointmentConflict(values, -1)) return;
        model.addRow(values);
        saveData();
        clearFields();
        JOptionPane.showMessageDialog(this, "Record added successfully.");
    }

    private void updateRecord() {
        int viewRow = table.getSelectedRow();
        if (viewRow < 0) { JOptionPane.showMessageDialog(this, "Select a row to update."); return; }
        int row = table.convertRowIndexToModel(viewRow);
        String[] values = readForm();
        if (values == null) return;
        for (int r = 0; r < model.getRowCount(); r++) {
            if (r != row && model.getValueAt(r, 0).toString().equalsIgnoreCase(values[0])) {
                JOptionPane.showMessageDialog(this, "This ID already exists."); return;
            }
        }
        if (isAppointmentConflict(values, row)) return;
        for (int c = 0; c < columns.length; c++) model.setValueAt(values[c], row, c);
        saveData();
        clearFields();
        table.clearSelection();
        JOptionPane.showMessageDialog(this, "Record updated successfully.");
    }

    private boolean isAppointmentConflict(String[] values, int ignoredRow) {
        if (!fileName.equals("appointments.csv") || columns.length < 5) return false;
        for (int r = 0; r < model.getRowCount(); r++) {
            if (r == ignoredRow) continue;
            boolean sameDoctor = model.getValueAt(r, 2).toString().equalsIgnoreCase(values[2]);
            boolean sameDate = model.getValueAt(r, 3).toString().equals(values[3]);
            boolean sameTime = model.getValueAt(r, 4).toString().equalsIgnoreCase(values[4]);
            boolean cancelled = columns.length > 5 && model.getValueAt(r, 5).toString().equalsIgnoreCase("Cancelled");
            if (sameDoctor && sameDate && sameTime && !cancelled) {
                JOptionPane.showMessageDialog(this, "This doctor already has an appointment at that date and time.");
                return true;
            }
        }
        return false;
    }

    private void deleteRecord() {
        if (!CurrentUser.isManager()) {
            JOptionPane.showMessageDialog(this, "Only the Manager can delete records."); return;
        }
        int viewRow = table.getSelectedRow();
        if (viewRow < 0) { JOptionPane.showMessageDialog(this, "Please select a row to delete."); return; }
        int choice = JOptionPane.showConfirmDialog(this, "Delete the selected record?", "Confirm Delete", JOptionPane.YES_NO_OPTION);
        if (choice == JOptionPane.YES_OPTION) {
            model.removeRow(table.convertRowIndexToModel(viewRow));
            saveData(); clearFields();
        }
    }

    private void fillFieldsFromSelectedRow() {
        int row = table.convertRowIndexToModel(table.getSelectedRow());
        for (int c = 0; c < fields.length; c++) fields[c].setText(model.getValueAt(row, c).toString());
    }
    private void clearFields() { for (JTextField field : fields) field.setText(""); table.clearSelection(); }
    private void loadData() {
        for (String[] row : FileHelper.readFile(fileName)) if (row.length == columns.length) model.addRow(row);
    }
    private void saveData() {
        ArrayList<String[]> rows = new ArrayList<>();
        for (int r = 0; r < model.getRowCount(); r++) {
            String[] row = new String[columns.length];
            for (int c = 0; c < columns.length; c++) row[c] = String.valueOf(model.getValueAt(r, c));
            rows.add(row);
        }
        FileHelper.writeFile(fileName, rows);
    }
}
