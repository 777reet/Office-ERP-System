import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.sql.*;

public class AttendancePanel extends JPanel {
    private static final Color SUCCESS = new Color(16, 185, 129);
    private static final Color DANGER = new Color(239, 68, 68);

    private JTable table;
    private DefaultTableModel tableModel;
    private JTextField tfEmployeeId, tfDate;
    private JComboBox<String> cbStatus;
    private int selectedId = -1;

    public AttendancePanel() {
        setLayout(new BorderLayout(15, 15));
        setBackground(new Color(245, 247, 252));
        setBorder(new EmptyBorder(25, 25, 25, 25));

        add(buildHeader(), BorderLayout.NORTH);
        add(buildTable(), BorderLayout.CENTER);
        add(buildForm(), BorderLayout.EAST);
        loadData();
    }

    private JPanel buildHeader() {
        JPanel p = new JPanel(new BorderLayout(10, 0));
        p.setOpaque(false);
        JLabel lbl = new JLabel("📅 Attendance Management");
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lbl.setForeground(new Color(30, 30, 60));
        p.add(lbl, BorderLayout.WEST);

        JButton btnToday = UIHelper.primaryButton("Mark Today", new Color(99, 102, 241));
        btnToday.addActionListener(e -> {
            java.time.LocalDate today = java.time.LocalDate.now();
            tfDate.setText(today.toString());
        });
        p.add(btnToday, BorderLayout.EAST);
        return p;
    }

    private JScrollPane buildTable() {
        String[] cols = {"ID", "Employee ID", "Employee Name", "Date", "Status"};
        tableModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        table = UIHelper.styledTable(tableModel);

        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object val, boolean sel, boolean foc, int row, int col) {
                Component c = super.getTableCellRendererComponent(t, val, sel, foc, row, col);
                if (!sel) {
                    String status = (String) tableModel.getValueAt(row, 4);
                    if ("Present".equals(status)) c.setBackground(new Color(240, 253, 244));
                    else if ("Absent".equals(status)) c.setBackground(new Color(255, 242, 242));
                    else c.setBackground(new Color(255, 251, 235));
                }
                setBorder(new EmptyBorder(0, 12, 0, 12));
                return c;
            }
        });

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

        JLabel title = new JLabel("Attendance Entry");
        title.setFont(new Font("Segoe UI", Font.BOLD, 15));
        title.setForeground(new Color(30, 30, 60));
        title.setAlignmentX(LEFT_ALIGNMENT);
        card.add(title);
        card.add(Box.createVerticalStrut(15));

        tfEmployeeId = UIHelper.styledField("Employee ID");
        tfDate = UIHelper.styledField("YYYY-MM-DD");
        cbStatus = UIHelper.styledCombo(new String[]{"Present", "Absent", "Leave"});

        addRow(card, "Employee ID *", tfEmployeeId);
        addRow(card, "Date *", tfDate);
        addRow(card, "Status *", cbStatus);

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

        // Legend
        card.add(Box.createVerticalStrut(20));
        JLabel legend = new JLabel("Legend:");
        legend.setFont(new Font("Segoe UI", Font.BOLD, 12));
        legend.setAlignmentX(LEFT_ALIGNMENT);
        card.add(legend);
        addLegend(card, "●", "Present", new Color(16, 185, 129));
        addLegend(card, "●", "Absent", new Color(239, 68, 68));
        addLegend(card, "●", "Leave", new Color(245, 158, 11));

        return card;
    }

    private void addLegend(JPanel parent, String dot, String text, Color color) {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 2));
        row.setOpaque(false);
        row.setAlignmentX(LEFT_ALIGNMENT);
        JLabel dotLbl = new JLabel(dot);
        dotLbl.setForeground(color);
        dotLbl.setFont(new Font("Segoe UI", Font.BOLD, 16));
        JLabel txtLbl = new JLabel(text);
        txtLbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        txtLbl.setForeground(new Color(100, 110, 140));
        row.add(dotLbl);
        row.add(txtLbl);
        parent.add(row);
    }

    private void addRow(JPanel parent, String label, JComponent field) {
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
        String sql = "SELECT a.attendance_id, a.employee_id, e.name, a.date, a.status " +
                     "FROM attendance a LEFT JOIN employees e ON a.employee_id = e.employee_id " +
                     "ORDER BY a.date DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                tableModel.addRow(new Object[]{
                    rs.getInt("attendance_id"), rs.getInt("employee_id"),
                    rs.getString("name"), rs.getString("date"), rs.getString("status")
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
        tfEmployeeId.setText(tableModel.getValueAt(row, 1).toString());
        tfDate.setText(tableModel.getValueAt(row, 3).toString());
        cbStatus.setSelectedItem(tableModel.getValueAt(row, 4));
    }

    private void save() {
        String empIdStr = tfEmployeeId.getText().trim();
        String date = tfDate.getText().trim();
        String status = (String) cbStatus.getSelectedItem();
        if (empIdStr.isEmpty() || date.isEmpty()) { UIHelper.showError(this, "Employee ID and Date required."); return; }
        int empId;
        try { empId = Integer.parseInt(empIdStr); } catch (NumberFormatException e) { UIHelper.showError(this, "Invalid Employee ID."); return; }

        try (Connection conn = DBConnection.getConnection()) {
            if (selectedId == -1) {
                try (PreparedStatement ps = conn.prepareStatement("INSERT INTO attendance (employee_id, date, status) VALUES (?,?,?)")) {
                    ps.setInt(1, empId); ps.setString(2, date); ps.setString(3, status);
                    ps.executeUpdate();
                }
                UIHelper.showSuccess(this, "Attendance marked!");
            } else {
                try (PreparedStatement ps = conn.prepareStatement("UPDATE attendance SET employee_id=?, date=?, status=? WHERE attendance_id=?")) {
                    ps.setInt(1, empId); ps.setString(2, date); ps.setString(3, status);
                    ps.setInt(4, selectedId);
                    ps.executeUpdate();
                }
                UIHelper.showSuccess(this, "Attendance updated!");
            }
            clearForm(); loadData();
        } catch (SQLException e) {
            UIHelper.showError(this, "Error: " + e.getMessage());
        }
    }

    private void delete() {
        if (selectedId == -1) { UIHelper.showError(this, "Select a record."); return; }
        if (!UIHelper.confirmDelete(this, "attendance record")) return;
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement("DELETE FROM attendance WHERE attendance_id=?")) {
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
        tfEmployeeId.setText(""); tfDate.setText("");
        cbStatus.setSelectedIndex(0);
        table.clearSelection();
    }
}
