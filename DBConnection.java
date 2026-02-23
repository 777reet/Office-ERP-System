import java.sql.*;

public class DBConnection {
    private static final String URL = "jdbc:mysql://localhost:3306/ERP?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    private static final String USER = "root";
    private static final String PASSWORD = "reet";

    public static Connection getConnection() throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            try {
                Class.forName("com.mysql.jdbc.Driver");
            } catch (ClassNotFoundException ex) {
                throw new SQLException("MySQL JDBC Driver not found. Please add mysql-connector-java.jar to classpath.", ex);
            }
        }
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    public static void initializeTables() {
        String[] ddl = {
            // Employees
            "CREATE TABLE IF NOT EXISTS employees (" +
            "  employee_id INT AUTO_INCREMENT PRIMARY KEY," +
            "  name VARCHAR(100) NOT NULL," +
            "  email VARCHAR(100)," +
            "  phone VARCHAR(20)," +
            "  department VARCHAR(100)," +
            "  designation VARCHAR(100)," +
            "  salary DECIMAL(12,2)" +
            ")",

            // Finance
            "CREATE TABLE IF NOT EXISTS finance (" +
            "  finance_id INT AUTO_INCREMENT PRIMARY KEY," +
            "  type ENUM('Income','Expense') NOT NULL," +
            "  amount DECIMAL(12,2) NOT NULL," +
            "  date DATE NOT NULL," +
            "  description TEXT" +
            ")",

            // Attendance
            "CREATE TABLE IF NOT EXISTS attendance (" +
            "  attendance_id INT AUTO_INCREMENT PRIMARY KEY," +
            "  employee_id INT NOT NULL," +
            "  date DATE NOT NULL," +
            "  status ENUM('Present','Absent','Leave') NOT NULL," +
            "  FOREIGN KEY (employee_id) REFERENCES employees(employee_id) ON DELETE CASCADE" +
            ")",

            // Departments
            "CREATE TABLE IF NOT EXISTS departments (" +
            "  dept_id INT AUTO_INCREMENT PRIMARY KEY," +
            "  department_name VARCHAR(100) NOT NULL," +
            "  designation VARCHAR(100)," +
            "  base_salary DECIMAL(12,2)" +
            ")",

            // Projects
            "CREATE TABLE IF NOT EXISTS projects (" +
            "  project_id INT AUTO_INCREMENT PRIMARY KEY," +
            "  project_name VARCHAR(200) NOT NULL," +
            "  start_date DATE," +
            "  end_date DATE," +
            "  assigned_employee_id INT," +
            "  FOREIGN KEY (assigned_employee_id) REFERENCES employees(employee_id) ON DELETE SET NULL" +
            ")",

            // Leaves
            "CREATE TABLE IF NOT EXISTS leaves (" +
            "  leave_id INT AUTO_INCREMENT PRIMARY KEY," +
            "  employee_id INT NOT NULL," +
            "  leave_type VARCHAR(50)," +
            "  start_date DATE," +
            "  end_date DATE," +
            "  status ENUM('Pending','Approved','Rejected') DEFAULT 'Pending'," +
            "  FOREIGN KEY (employee_id) REFERENCES employees(employee_id) ON DELETE CASCADE" +
            ")"
        };

        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            for (String sql : ddl) {
                stmt.executeUpdate(sql);
            }
            System.out.println("Tables initialized successfully.");
        } catch (SQLException e) {
            System.err.println("Error initializing tables: " + e.getMessage());
            e.printStackTrace();
        }
    }
}