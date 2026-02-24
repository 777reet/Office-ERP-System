import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.sql.*;

public class ProjectPanel extends JPanel {
    private static final Color SUCCESS = new Color(16, 185, 129);
    private static final Color DANGER = new Color(239, 68, 68);
    private static final Color PRIMARY = new Color(99, 102, 241);

    private JTable table;
    private DefaultTableModel tableModel;
    private JTextField tfName, tfStart, tfEnd, tfEmpId;
    private int selectedId = -1;

    public ProjectPanel() {
        setLayout(new BorderLayout(15, 15));
        setOpaque(false);
        setBorder(new EmptyBorder(25, 25, 25, 25));

        add(buildHeader(), BorderLayout.NORTH);
        add(buildTable(), BorderLayout.CENTER);
        add(buildForm(), BorderLayout.EAST);
        loadData();
    }

    private JPanel buildHeader() {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        JLabel lbl = new JLabel("📁 Project Management");
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lbl.setForeground(new Color(240, 240, 255));
        p.add(lbl, BorderLayout.WEST);
        return p;
    }

    private JScrollPane buildTable() {
        String[] cols = {"ID", "Project Name", "Start Date", "End Date", "Assigned Employee"};
        tableModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        table = UIHelper.styledTable(tableModel);
        table.getColumnModel().getColumn(1).setPreferredWidth(200);
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && table.getSelectedRow() >= 0) populateForm();
        });
        JScrollPane sp = UIHelper.glassScroll(table);
        
        
        return sp;
    }

    private JPanel buildForm() {
        JPanel card = UIHelper.cardPanel();
        card.setPreferredSize(new Dimension(260, 0));
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("Project Details");
        title.setFont(new Font("Segoe UI", Font.BOLD, 15));
        title.setForeground(new Color(240, 240, 255));
        title.setAlignmentX(LEFT_ALIGNMENT);
        card.add(title);
        card.add(Box.createVerticalStrut(15));

        tfName = UIHelper.styledField("Project Name");
        tfStart = UIHelper.styledField("YYYY-MM-DD");
        tfEnd = UIHelper.styledField("YYYY-MM-DD");
        tfEmpId = UIHelper.styledField("Employee ID (optional)");

        addRow(card, "Project Name *", tfName);
        addRow(card, "Start Date", tfStart);
        addRow(card, "End Date", tfEnd);
        addRow(card, "Assigned Employee ID", tfEmpId);

        card.add(Box.createVerticalStrut(15));
        JPanel btnRow = new JPanel(new GridLayout(1, 2, 8, 0));
        btnRow.setOpaque(false);
        btnRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        JButton btnSave = UIHelper.primaryButton("💾 Save", SUCCESS);
        JButton btnDelete = UIHelper.primaryButton("🗑 Delete", DANGER);
        btnSave.addActionListener(e -> save());
        btnDelete.addActionListener(e -> delete());
        btnRow.add(btnSave);
        btnRow.add(btnDelete);
        card.add(btnRow);
        card.add(Box.createVerticalStrut(8));
        JButton btnClear = UIHelper.primaryButton("✖ Clear", new Color(107, 114, 128));
        btnClear.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        btnClear.addActionListener(e -> clearForm());
        card.add(btnClear);

        return card;
    }

    private void addRow(JPanel parent, String label, JTextField field) {
        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lbl.setForeground(new Color(160, 160, 200));
        lbl.setAlignmentX(LEFT_ALIGNMENT);
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        field.setAlignmentX(LEFT_ALIGNMENT);
        parent.add(lbl);
        parent.add(Box.createVerticalStrut(4));
        parent.add(field);
        parent.add(Box.createVerticalStrut(10));
    }

    private void loadData() {
        tableModel.setRowCount(0);
        String sql = "SELECT p.project_id, p.project_name, p.start_date, p.end_date, " +
                     "COALESCE(e.name, 'Unassigned') as emp_name " +
                     "FROM projects p LEFT JOIN employees e ON p.assigned_employee_id = e.employee_id " +
                     "ORDER BY p.project_name";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                tableModel.addRow(new Object[]{
                    rs.getInt("project_id"), rs.getString("project_name"),
                    rs.getString("start_date"), rs.getString("end_date"),
                    rs.getString("emp_name")
                });
            }
        } catch (SQLException e) {
            UIHelper.showError(this, "Error: " + e.getMessage());
        }
    }

    private void populateForm() {
        int row = table.getSelectedRow();
        if (row < 0) return;

        selectedId = (int) tableModel.getValueAt(row, 0);
        tfName.setText((String) tableModel.getValueAt(row, 1));
        Object sd = tableModel.getValueAt(row, 2);
        Object ed = tableModel.getValueAt(row, 3);
        tfStart.setText(sd != null ? sd.toString() : "");
        tfEnd.setText(ed != null ? ed.toString() : "");

        // Fetch assigned employee id from DB
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT assigned_employee_id FROM projects WHERE project_id=?")) {
            ps.setInt(1, selectedId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                int empId = rs.getInt("assigned_employee_id");
                tfEmpId.setText(rs.wasNull() ? "" : String.valueOf(empId));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void save() {
        String name = tfName.getText().trim();
        if (name.isEmpty()) { UIHelper.showError(this, "Project Name is required."); return; }
        String start = tfStart.getText().trim();
        String end = tfEnd.getText().trim();
        String empIdStr = tfEmpId.getText().trim();
        Integer empId = null;
        if (!empIdStr.isEmpty()) {
            try { empId = Integer.parseInt(empIdStr); } catch (NumberFormatException e) {
                UIHelper.showError(this, "Invalid Employee ID."); return;
            }
        }

        try (Connection conn = DBConnection.getConnection()) {
            if (selectedId == -1) {
                String sql = "INSERT INTO projects (project_name, start_date, end_date, assigned_employee_id) VALUES (?,?,?,?)";
                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setString(1, name);
                    ps.setString(2, start.isEmpty() ? null : start);
                    ps.setString(3, end.isEmpty() ? null : end);
                    if (empId != null) ps.setInt(4, empId); else ps.setNull(4, Types.INTEGER);
                    ps.executeUpdate();
                }
                UIHelper.showSuccess(this, "Project added!");
            } else {
                String sql = "UPDATE projects SET project_name=?, start_date=?, end_date=?, assigned_employee_id=? WHERE project_id=?";
                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setString(1, name);
                    ps.setString(2, start.isEmpty() ? null : start);
                    ps.setString(3, end.isEmpty() ? null : end);
                    if (empId != null) ps.setInt(4, empId); else ps.setNull(4, Types.INTEGER);
                    ps.setInt(5, selectedId);
                    ps.executeUpdate();
                }
                UIHelper.showSuccess(this, "Project updated!");
            }
            clearForm(); loadData();
        } catch (SQLException e) {
            UIHelper.showError(this, "Error: " + e.getMessage());
        }
    }

    private void delete() {
        if (selectedId == -1) { UIHelper.showError(this, "Select a project."); return; }
        if (!UIHelper.confirmDelete(this, "project")) return;
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement("DELETE FROM projects WHERE project_id=?")) {
            ps.setInt(1, selectedId);
            ps.executeUpdate();
            UIHelper.showSuccess(this, "Deleted!");
            clearForm(); loadData();
        } catch (SQLException e) {
            UIHelper.showError(this, "Error: " + e.getMessage());
        }
    }

    private void clearForm() {
        selectedId = -1;
        tfName.setText(""); tfStart.setText(""); tfEnd.setText(""); tfEmpId.setText("");
        table.clearSelection();
    }
}