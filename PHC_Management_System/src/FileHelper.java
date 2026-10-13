import java.io.*;
import java.sql.*;
import java.util.ArrayList;

// Reads and writes records in a SQLite database (data/phc.db).
// Same methods as the old CSV version, so no other file needs to change.
// Each "file name" (e.g. patients.csv) becomes a table (e.g. patients).
public class FileHelper {

    private static final String FOLDER = "data";
    private static final String URL = "jdbc:sqlite:" + FOLDER + "/phc.db";

    private static Connection connect() throws SQLException {
        new File(FOLDER).mkdirs();
        return DriverManager.getConnection(URL);
    }

    // "patients.csv" -> "patients" (letters/numbers only, safe for SQL)
    private static String tableName(String fileName) {
        return fileName.replace(".csv", "").replaceAll("[^A-Za-z0-9_]", "");
    }

    // Read all records from a table. Each record is a String array.
    public static ArrayList<String[]> readFile(String fileName) {
        ArrayList<String[]> rows = new ArrayList<>();
        String table = tableName(fileName);

        try (Connection conn = connect()) {
            if (!tableExists(conn, table)) {
                return rows; // no table yet, so no records (same as "no file yet")
            }
            try (Statement st = conn.createStatement();
                 ResultSet rs = st.executeQuery("SELECT * FROM " + table + " ORDER BY rowid")) {
                int count = rs.getMetaData().getColumnCount();
                while (rs.next()) {
                    String[] row = new String[count];
                    for (int i = 0; i < count; i++) {
                        String v = rs.getString(i + 1);
                        row[i] = (v == null) ? "" : v;
                    }
                    rows.add(row);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return rows;
    }

    // Write all records to a table (replaces old content).
    public static void writeFile(String fileName, ArrayList<String[]> rows) {
        String table = tableName(fileName);

        try (Connection conn = connect()) {
            conn.setAutoCommit(false);
            try {
                // If the table exists with a different column count, rebuild it.
                if (!rows.isEmpty() && tableExists(conn, table)
                        && columnCount(conn, table) != rows.get(0).length) {
                    try (Statement st = conn.createStatement()) {
                        st.executeUpdate("DROP TABLE " + table);
                    }
                }

                if (!tableExists(conn, table)) {
                    if (rows.isEmpty()) {
                        conn.commit();
                        return; // nothing to store, no table needed
                    }
                    createTable(conn, table, rows.get(0).length);
                }

                try (Statement st = conn.createStatement()) {
                    st.executeUpdate("DELETE FROM " + table);
                }

                if (!rows.isEmpty()) {
                    int n = rows.get(0).length;
                    String sql = "INSERT INTO " + table + " VALUES (" + placeholders(n) + ")";
                    try (PreparedStatement ps = conn.prepareStatement(sql)) {
                        for (String[] row : rows) {
                            for (int i = 0; i < n; i++) {
                                ps.setString(i + 1, row[i]);
                            }
                            ps.addBatch();
                        }
                        ps.executeBatch();
                    }
                }
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Count how many records are in a table.
    public static int countRecords(String fileName) {
        String table = tableName(fileName);
        try (Connection conn = connect()) {
            if (!tableExists(conn, table)) {
                return 0;
            }
            try (Statement st = conn.createStatement();
                 ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM " + table)) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        }
    }

    // ---------- helpers ----------

    private static boolean tableExists(Connection conn, String table) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT 1 FROM sqlite_master WHERE type='table' AND name=?")) {
            ps.setString(1, table);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    private static int columnCount(Connection conn, String table) throws SQLException {
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT * FROM " + table + " LIMIT 0")) {
            return rs.getMetaData().getColumnCount();
        }
    }

    private static void createTable(Connection conn, String table, int columns) throws SQLException {
        StringBuilder sb = new StringBuilder("CREATE TABLE " + table + " (");
        for (int i = 0; i < columns; i++) {
            sb.append(i == 0 ? "" : ", ").append("col").append(i).append(" TEXT");
        }
        sb.append(")");
        try (Statement st = conn.createStatement()) {
            st.executeUpdate(sb.toString());
        }
    }

    private static String placeholders(int n) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < n; i++) {
            sb.append(i == 0 ? "?" : ",?");
        }
        return sb.toString();
    }
}