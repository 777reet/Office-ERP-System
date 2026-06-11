import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.*;

public class EmployeePanel extends JPanel {
    private static final Color PRIMARY = new Color(99, 102, 241);
    private static final Color DANGER  = new Color(239, 68, 68);
    private static final Color SUCCESS = new Color(16, 185, 129);

    private JTable table;
    private DefaultTableModel tableModel;
    private JTextField tfName, tfEmail, tfPhone, tfSalary, tfSearch;
    private JComboBox<String> cbDept, cbDesig;
    private int selectedId = -1;

    private Map<String, java.util.List<String>> deptDesigMap = new LinkedHashMap<>();
    private Map<String, Double> desigSalaryMap = new HashMap<>();

    public EmployeePanel() {
        setLayout(new BorderLayout(15, 15));
        setOpaque(false);
        setBorder(new EmptyBorder(25, 25, 25, 25));
        loadDeptData();
        add(buildHeader(), BorderLayout.NORTH);
        add(buildCenter(), BorderLayout.CENTER);
        add(buildForm(),   BorderLayout.EAST);
        loadData("");
    }

    private void loadDeptData() {
        deptDesigMap.clear();
        desigSalaryMap.clear();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                 "SELECT department_name, designation, base_salary FROM departments ORDER BY department_name, designation");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                String dept  = rs.getString("department_name");
                String desig = rs.getString("designation");
                double sal   = rs.getDouble("base_salary");
                deptDesigMap.computeIfAbsent(dept, k -> new ArrayList<>()).add(desig);
                desigSalaryMap.put(dept + "|" + desig, sal);
            }
        } catch (SQLException e) { /* silently ignore */ }
    }

    private JPanel buildHeader() {
        JPanel p = new JPanel(new BorderLayout(10, 0));
        p.setOpaque(false);
        JLabel lbl = new JLabel("• Employee Management");
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lbl.setForeground(new Color(240, 240, 255));
        p.add(lbl, BorderLayout.WEST);
        tfSearch = UIHelper.styledField("Search by name or department...");
        tfSearch.setPreferredSize(new Dimension(250, 36));
        tfSearch.addActionListener(e -> loadData(tfSearch.getText()));
        JButton btnSearch = UIHelper.primaryButton("Search", PRIMARY);
        btnSearch.addActionListener(e -> loadData(tfSearch.getText()));
        JPanel searchRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        searchRow.setOpaque(false);
        searchRow.add(tfSearch);
        searchRow.add(btnSearch);
        p.add(searchRow, BorderLayout.EAST);
        return p;
    }

    private JScrollPane buildCenter() {
        String[] cols = {"ID","Name","Email","Phone","Department","Designation","Salary"};
        tableModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        table = UIHelper.styledTable(tableModel);
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && table.getSelectedRow() >= 0) populateForm();
        });
        JScrollPane sp = UIHelper.glassScroll(table);
        
        
        return sp;
    }

    private JPanel buildForm() {
        JPanel card = UIHelper.cardPanel();
        card.setPreferredSize(new Dimension(290, 0));
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("Employee Details");
        title.setFont(new Font("Segoe UI", Font.BOLD, 15));
        title.setForeground(new Color(240, 240, 255));
        title.setAlignmentX(LEFT_ALIGNMENT);
        card.add(title);
        card.add(Box.createVerticalStrut(15));

        tfName   = UIHelper.styledField("Full Name");
        tfEmail  = UIHelper.styledField("Email Address");
        tfPhone  = UIHelper.styledField("Phone Number");
        tfSalary = UIHelper.styledField("Salary (auto-fills)");

        cbDept  = UIHelper.styledCombo(buildDeptArray());
        cbDesig = UIHelper.styledCombo(buildDesigArray((String) cbDept.getSelectedItem()));

        cbDept.addActionListener(e -> {
            String sel = (String) cbDept.getSelectedItem();
            cbDesig.removeAllItems();
            for (String d : deptDesigMap.getOrDefault(sel, new ArrayList<>())) cbDesig.addItem(d);
            autoFillSalary();
        });
        cbDesig.addActionListener(e -> autoFillSalary());

        addFormRow(card, "Name *", tfName);
        addFormRow(card, "Email", tfEmail);
        addFormRow(card, "Phone", tfPhone);
        addComboRow(card, "Department *", cbDept);
        addComboRow(card, "Designation *", cbDesig);
        addFormRow(card, "Salary", tfSalary);

        JButton btnRefresh = UIHelper.primaryButton("• Refresh Departments", new Color(99, 102, 241));
        btnRefresh.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        btnRefresh.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnRefresh.addActionListener(e -> { loadDeptData(); refreshCombos(); });
        card.add(btnRefresh);
        card.add(Box.createVerticalStrut(12));

        JPanel btnRow = new JPanel(new GridLayout(1, 2, 8, 0));
        btnRow.setOpaque(false);
        btnRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        JButton btnSave   = UIHelper.primaryButton("• Save",   SUCCESS);
        JButton btnDelete = UIHelper.primaryButton("• Delete", DANGER);
        btnSave.addActionListener(e   -> saveEmployee());
        btnDelete.addActionListener(e -> deleteEmployee());
        btnRow.add(btnSave);
        btnRow.add(btnDelete);

        JButton btnClear = UIHelper.primaryButton("• Clear", new Color(107, 114, 128));
        btnClear.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        btnClear.addActionListener(e -> clearForm());
        card.add(btnRow);
        card.add(Box.createVerticalStrut(8));
        card.add(btnClear);
        return card;
    }

    private String[] buildDeptArray() {
        java.util.List<String> list = new ArrayList<>();
        list.add("-- Select Department --");
        list.addAll(deptDesigMap.keySet());
        return list.toArray(new String[0]);
    }

    private String[] buildDesigArray(String dept) {
        java.util.List<String> list = new ArrayList<>();
        list.add("-- Select Designation --");
        if (dept != null) list.addAll(deptDesigMap.getOrDefault(dept, new ArrayList<>()));
        return list.toArray(new String[0]);
    }

    private void autoFillSalary() {
        String dept  = (String) cbDept.getSelectedItem();
        String desig = (String) cbDesig.getSelectedItem();
        if (dept == null || desig == null) return;
        Double sal = desigSalaryMap.get(dept + "|" + desig);
        if (sal != null && sal > 0) tfSalary.setText(String.format("%.2f", sal));
    }

    private void refreshCombos() {
        String cur = (String) cbDept.getSelectedItem();
        cbDept.removeAllItems();
        for (String d : buildDeptArray()) cbDept.addItem(d);
        if (cur != null) cbDept.setSelectedItem(cur);
    }

    private void addFormRow(JPanel parent, String label, JTextField field) {
        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lbl.setForeground(new Color(160, 160, 200));
        lbl.setAlignmentX(LEFT_ALIGNMENT);
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        field.setAlignmentX(LEFT_ALIGNMENT);
        parent.add(lbl); parent.add(Box.createVerticalStrut(4));
        parent.add(field); parent.add(Box.createVerticalStrut(10));
    }

    private void addComboRow(JPanel parent, String label, JComboBox<String> combo) {
        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lbl.setForeground(new Color(160, 160, 200));
        lbl.setAlignmentX(LEFT_ALIGNMENT);
        combo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        combo.setAlignmentX(LEFT_ALIGNMENT);
        parent.add(lbl); parent.add(Box.createVerticalStrut(4));
        parent.add(combo); parent.add(Box.createVerticalStrut(10));
    }

    private void loadData(String search) {
        tableModel.setRowCount(0);
        String sql = "SELECT employee_id, name, email, phone, department, designation, salary " +
                     "FROM employees WHERE name LIKE ? OR department LIKE ? ORDER BY name";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, "%" + search + "%");
            ps.setString(2, "%" + search + "%");
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                tableModel.addRow(new Object[]{
                    rs.getInt("employee_id"), rs.getString("name"), rs.getString("email"),
                    rs.getString("phone"), rs.getString("department"),
                    rs.getString("designation"), String.format("%.2f", rs.getDouble("salary"))
                });
            }
        } catch (SQLException e) {
            UIHelper.showError(this, "Error loading employees: " + e.getMessage());
        }
    }

    private void populateForm() {
        int row = table.getSelectedRow();
        if (row < 0) return;
        selectedId = (int) tableModel.getValueAt(row, 0);
        tfName.setText((String)  tableModel.getValueAt(row, 1));
        tfEmail.setText((String) tableModel.getValueAt(row, 2));
        tfPhone.setText((String) tableModel.getValueAt(row, 3));
        String dept  = (String) tableModel.getValueAt(row, 4);
        String desig = (String) tableModel.getValueAt(row, 5);
        cbDept.setSelectedItem(dept);
        cbDesig.setSelectedItem(desig);
        tfSalary.setText(tableModel.getValueAt(row, 6).toString());
    }

    private void saveEmployee() {
        String name  = tfName.getText().trim();
        String dept  = (String) cbDept.getSelectedItem();
        String desig = (String) cbDesig.getSelectedItem();
        if (name.isEmpty())                           { UIHelper.showError(this, "Name is required."); return; }
        if (dept  == null || dept.startsWith("--"))  { UIHelper.showError(this, "Please select a Department."); return; }
        if (desig == null || desig.startsWith("--")) { UIHelper.showError(this, "Please select a Designation."); return; }
        double salary = 0;
        try { salary = Double.parseDouble(tfSalary.getText().trim()); } catch (NumberFormatException ignored) {}

        try (Connection conn = DBConnection.getConnection()) {
            if (selectedId == -1) {
                try (PreparedStatement ps = conn.prepareStatement(
                        "INSERT INTO employees (name, email, phone, department, designation, salary) VALUES (?,?,?,?,?,?)")) {
                    ps.setString(1, name); ps.setString(2, tfEmail.getText().trim());
                    ps.setString(3, tfPhone.getText().trim()); ps.setString(4, dept);
                    ps.setString(5, desig); ps.setDouble(6, salary);
                    ps.executeUpdate();
                }
                UIHelper.showSuccess(this, "Employee added successfully!");
            } else {
                try (PreparedStatement ps = conn.prepareStatement(
                        "UPDATE employees SET name=?, email=?, phone=?, department=?, designation=?, salary=? WHERE employee_id=?")) {
                    ps.setString(1, name); ps.setString(2, tfEmail.getText().trim());
                    ps.setString(3, tfPhone.getText().trim()); ps.setString(4, dept);
                    ps.setString(5, desig); ps.setDouble(6, salary); ps.setInt(7, selectedId);
                    ps.executeUpdate();
                }
                UIHelper.showSuccess(this, "Employee updated successfully!");
            }
            clearForm(); loadData("");
        } catch (SQLException e) {
            UIHelper.showError(this, "Error saving employee: " + e.getMessage());
        }
    }

    private void deleteEmployee() {
        if (selectedId == -1) { UIHelper.showError(this, "Select an employee to delete."); return; }
        if (!UIHelper.confirmDelete(this, "employee")) return;
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement("DELETE FROM employees WHERE employee_id=?")) {
            ps.setInt(1, selectedId);
            ps.executeUpdate();
            UIHelper.showSuccess(this, "Employee deleted successfully!");
            clearForm(); loadData("");
        } catch (SQLException e) {
            UIHelper.showError(this, "Error deleting employee: " + e.getMessage());
        }
    }

    private void clearForm() {
        selectedId = -1;
        tfName.setText(""); tfEmail.setText(""); tfPhone.setText(""); tfSalary.setText("");
        if (cbDept.getItemCount()  > 0) cbDept.setSelectedIndex(0);
        if (cbDesig.getItemCount() > 0) cbDesig.setSelectedIndex(0);
        table.clearSelection();
    }
}