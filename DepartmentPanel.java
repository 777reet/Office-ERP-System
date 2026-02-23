import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.sql.*;

public class DepartmentPanel extends JPanel {
    private static final Color SUCCESS = new Color(16, 185, 129);
    private static final Color DANGER = new Color(239, 68, 68);
    private static final Color PRIMARY = new Color(99, 102, 241);

    private JTable table;
    private DefaultTableModel tableModel;
    private JTextField tfDeptName, tfDesig, tfBaseSalary;
    private int selectedId = -1;

    public DepartmentPanel() {
        setLayout(new BorderLayout(15, 15));
        setBackground(new Color(245, 247, 252));
        setBorder(new EmptyBorder(25, 25, 25, 25));

        add(buildHeader(), BorderLayout.NORTH);
        add(buildTable(), BorderLayout.CENTER);
        add(buildForm(), BorderLayout.EAST);
        loadData();
    }

    private JPanel buildHeader() {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        JLabel lbl = new JLabel("🏢 Department & Designation Management");
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lbl.setForeground(new Color(30, 30, 60));
        p.add(lbl, BorderLayout.WEST);
        return p;
    }

    private JScrollPane buildTable() {
        String[] cols = {"Dept ID", "Department Name", "Designation", "Base Salary (₹)"};
        tableModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        table = UIHelper.styledTable(tableModel);
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && table.getSelectedRow() >= 0) populateForm();
        });
        JScrollPane sp = new JScrollPane(table);
        sp.setBorder(UIHelper.cardBorder());
        sp.getViewport().setBackground(Color.WHITE);
        return sp;
    }

    private JPanel buildForm() {
        JPanel card = UIHelper.cardPanel();
        card.setPreferredSize(new Dimension(260, 0));
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("Department Details");
        title.setFont(new Font("Segoe UI", Font.BOLD, 15));
        title.setForeground(new Color(30, 30, 60));
        title.setAlignmentX(LEFT_ALIGNMENT);
        card.add(title);
        card.add(Box.createVerticalStrut(15));

        tfDeptName = UIHelper.styledField("Department Name");
        tfDesig = UIHelper.styledField("Designation");
        tfBaseSalary = UIHelper.styledField("Base Salary");

        addRow(card, "Department Name *", tfDeptName);
        addRow(card, "Designation", tfDesig);
        addRow(card, "Base Salary", tfBaseSalary);

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

        // Info note
        card.add(Box.createVerticalStrut(20));
        JTextArea note = new JTextArea("Tip: Each department can have multiple designations. Add one entry per designation.");
        note.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        note.setForeground(new Color(120, 130, 160));
        note.setOpaque(false);
        note.setEditable(false);
        note.setLineWrap(true);
        note.setWrapStyleWord(true);
        note.setAlignmentX(LEFT_ALIGNMENT);
        note.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));
        card.add(note);

        return card;
    }

    private void addRow(JPanel parent, String label, JTextField field) {
        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lbl.setForeground(new Color(100, 110, 140));
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
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT * FROM departments ORDER BY department_name");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                tableModel.addRow(new Object[]{
                    rs.getInt("dept_id"), rs.getString("department_name"),
                    rs.getString("designation"), String.format("%.2f", rs.getDouble("base_salary"))
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
        tfDeptName.setText((String) tableModel.getValueAt(row, 1));
        tfDesig.setText((String) tableModel.getValueAt(row, 2));
        tfBaseSalary.setText(tableModel.getValueAt(row, 3).toString());
    }

    private void save() {
        String deptName = tfDeptName.getText().trim();
        if (deptName.isEmpty()) { UIHelper.showError(this, "Department Name is required."); return; }
        double baseSalary = 0;
        try { baseSalary = Double.parseDouble(tfBaseSalary.getText().trim()); } catch (NumberFormatException e) {}

        try (Connection conn = DBConnection.getConnection()) {
            if (selectedId == -1) {
                try (PreparedStatement ps = conn.prepareStatement("INSERT INTO departments (department_name, designation, base_salary) VALUES (?,?,?)")) {
                    ps.setString(1, deptName); ps.setString(2, tfDesig.getText().trim());
                    ps.setDouble(3, baseSalary);
                    ps.executeUpdate();
                }
                UIHelper.showSuccess(this, "Department added!");
            } else {
                try (PreparedStatement ps = conn.prepareStatement("UPDATE departments SET department_name=?, designation=?, base_salary=? WHERE dept_id=?")) {
                    ps.setString(1, deptName); ps.setString(2, tfDesig.getText().trim());
                    ps.setDouble(3, baseSalary); ps.setInt(4, selectedId);
                    ps.executeUpdate();
                }
                UIHelper.showSuccess(this, "Department updated!");
            }
            clearForm(); loadData();
        } catch (SQLException e) {
            UIHelper.showError(this, "Error: " + e.getMessage());
        }
    }

    private void delete() {
        if (selectedId == -1) { UIHelper.showError(this, "Select a department."); return; }
        if (!UIHelper.confirmDelete(this, "department")) return;
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement("DELETE FROM departments WHERE dept_id=?")) {
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
        tfDeptName.setText(""); tfDesig.setText(""); tfBaseSalary.setText("");
        table.clearSelection();
    }
}
