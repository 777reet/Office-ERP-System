import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.sql.*;

public class LeavePanel extends JPanel {
    private static final Color SUCCESS = new Color(16, 185, 129);
    private static final Color DANGER  = new Color(239, 68, 68);
    private static final Color PRIMARY = new Color(139, 92, 246);
    private static final Color TEXT    = new Color(240, 240, 255);
    private static final Color MUTED   = new Color(160, 160, 200);

    private JTable table;
    private DefaultTableModel tableModel;
    private JTextField tfEmpId, tfLeaveType, tfStart, tfEnd;
    private JComboBox<String> cbStatus;
    private int selectedId = -1;

    public LeavePanel() {
        setLayout(new BorderLayout(15, 15));
        setOpaque(false);
        setBorder(new EmptyBorder(25, 25, 25, 25));
        add(buildHeader(), BorderLayout.NORTH);
        add(buildTable(),  BorderLayout.CENTER);
        add(buildForm(),   BorderLayout.EAST);
        loadData();
    }

    private JPanel buildHeader() {
        JPanel p = new JPanel(new BorderLayout(10, 0));
        p.setOpaque(false);
        JLabel lbl = new JLabel("• Leave Management");
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lbl.setForeground(TEXT);
        p.add(lbl, BorderLayout.WEST);
        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btns.setOpaque(false);
        JButton btnApp = UIHelper.primaryButton("• Approve", SUCCESS);
        JButton btnRej = UIHelper.primaryButton("• Reject",  DANGER);
        btnApp.addActionListener(e -> updateStatus("Approved"));
        btnRej.addActionListener(e -> updateStatus("Rejected"));
        btns.add(btnApp); btns.add(btnRej);
        p.add(btns, BorderLayout.EAST);
        return p;
    }

    private JScrollPane buildTable() {
        String[] cols = {"ID", "Employee ID", "Employee Name", "Leave Type", "From", "To", "Status"};
        tableModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        table = UIHelper.styledTable(tableModel);

        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object val, boolean sel, boolean foc, int row, int col) {
                super.getTableCellRendererComponent(t, val, sel, foc, row, col);
                if (sel) {
                    setBackground(new Color(139, 92, 246, 90));
                    setForeground(Color.WHITE);
                } else {
                    setBackground(row % 2 == 0 ? new Color(255,255,255,10) : new Color(255,255,255,5));
                    if (col == 6) {
                        String status = tableModel.getRowCount() > row ? (String) tableModel.getValueAt(row, 6) : "";
                        if ("Approved".equals(status))     setForeground(new Color(52, 211, 153));
                        else if ("Rejected".equals(status)) setForeground(new Color(251, 113, 133));
                        else                               setForeground(new Color(251, 191, 36));
                        setFont(new Font("Segoe UI", Font.BOLD, 13));
                    } else {
                        setForeground(new Color(220, 220, 245));
                        setFont(new Font("Segoe UI", Font.PLAIN, 13));
                    }
                }
                setOpaque(true);
                setBorder(new EmptyBorder(0, 14, 0, 14));
                return this;
            }
        });

        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && table.getSelectedRow() >= 0) populateForm();
        });
        return UIHelper.glassScroll(table);
    }

    private JPanel buildForm() {
        JPanel card = UIHelper.glassPanel();
        card.setPreferredSize(new Dimension(260, 0));
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("Leave Application");
        title.setFont(new Font("Segoe UI", Font.BOLD, 15));
        title.setForeground(TEXT); title.setAlignmentX(LEFT_ALIGNMENT);
        card.add(title); card.add(Box.createVerticalStrut(15));

        tfEmpId    = UIHelper.styledField("Employee ID");
        tfLeaveType= UIHelper.styledField("Sick / Casual / Annual");
        tfStart    = UIHelper.styledField("YYYY-MM-DD");
        tfEnd      = UIHelper.styledField("YYYY-MM-DD");
        cbStatus   = UIHelper.styledCombo(new String[]{"Pending","Approved","Rejected"});

        addRow(card, "Employee ID *", tfEmpId);
        addRow(card, "Leave Type",    tfLeaveType);
        addRow(card, "Start Date *",  tfStart);
        addRow(card, "End Date *",    tfEnd);
        addRowC(card, "Status",       cbStatus);

        card.add(Box.createVerticalStrut(15));
        JPanel btnRow = new JPanel(new GridLayout(1, 2, 8, 0));
        btnRow.setOpaque(false);
        btnRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        JButton btnSave   = UIHelper.primaryButton("• Save",   SUCCESS);
        JButton btnDelete = UIHelper.primaryButton("• Delete", DANGER);
        btnSave.addActionListener(e   -> save());
        btnDelete.addActionListener(e -> delete());
        btnRow.add(btnSave); btnRow.add(btnDelete);
        card.add(btnRow);
        card.add(Box.createVerticalStrut(8));
        JButton btnClear = UIHelper.primaryButton("• Clear", new Color(80, 80, 110));
        btnClear.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        btnClear.addActionListener(e -> clearForm());
        card.add(btnClear);
        return card;
    }

    private void addRow(JPanel parent, String label, JTextField field) {
        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lbl.setForeground(MUTED); lbl.setAlignmentX(LEFT_ALIGNMENT);
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        field.setAlignmentX(LEFT_ALIGNMENT);
        parent.add(lbl); parent.add(Box.createVerticalStrut(4));
        parent.add(field); parent.add(Box.createVerticalStrut(10));
    }

    private void addRowC(JPanel parent, String label, JComponent field) {
        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lbl.setForeground(MUTED); lbl.setAlignmentX(LEFT_ALIGNMENT);
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        field.setAlignmentX(LEFT_ALIGNMENT);
        parent.add(lbl); parent.add(Box.createVerticalStrut(4));
        parent.add(field); parent.add(Box.createVerticalStrut(10));
    }

    private void loadData() {
        tableModel.setRowCount(0);
        String sql = "SELECT l.leave_id, l.employee_id, e.name, l.leave_type, l.start_date, l.end_date, l.status " +
                     "FROM leaves l LEFT JOIN employees e ON l.employee_id=e.employee_id ORDER BY l.start_date DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                tableModel.addRow(new Object[]{
                    rs.getInt("leave_id"), rs.getInt("employee_id"), rs.getString("name"),
                    rs.getString("leave_type"), rs.getString("start_date"),
                    rs.getString("end_date"), rs.getString("status")
                });
            }
        } catch (SQLException e) { UIHelper.showError(this, "Error: " + e.getMessage()); }
    }

    private void populateForm() {
        int row = table.getSelectedRow();
        if (row < 0) return;
        selectedId = (int) tableModel.getValueAt(row, 0);
        tfEmpId.setText(tableModel.getValueAt(row, 1).toString());
        tfLeaveType.setText((String) tableModel.getValueAt(row, 3));
        Object sd = tableModel.getValueAt(row, 4);
        Object ed = tableModel.getValueAt(row, 5);
        tfStart.setText(sd != null ? sd.toString() : "");
        tfEnd.setText(ed != null ? ed.toString() : "");
        cbStatus.setSelectedItem(tableModel.getValueAt(row, 6));
    }

    private void save() {
        String empIdStr = tfEmpId.getText().trim();
        String start = tfStart.getText().trim(), end = tfEnd.getText().trim();
        if (empIdStr.isEmpty() || start.isEmpty() || end.isEmpty()) { UIHelper.showError(this, "Employee ID, Start and End Date required."); return; }
        int empId;
        try { empId = Integer.parseInt(empIdStr); } catch (NumberFormatException e) { UIHelper.showError(this, "Invalid Employee ID."); return; }
        try (Connection conn = DBConnection.getConnection()) {
            if (selectedId == -1) {
                try (PreparedStatement ps = conn.prepareStatement("INSERT INTO leaves (employee_id, leave_type, start_date, end_date, status) VALUES (?,?,?,?,?)")) {
                    ps.setInt(1, empId); ps.setString(2, tfLeaveType.getText().trim());
                    ps.setString(3, start); ps.setString(4, end); ps.setString(5, (String)cbStatus.getSelectedItem());
                    ps.executeUpdate();
                }
                UIHelper.showSuccess(this, "Leave submitted!");
            } else {
                try (PreparedStatement ps = conn.prepareStatement("UPDATE leaves SET employee_id=?, leave_type=?, start_date=?, end_date=?, status=? WHERE leave_id=?")) {
                    ps.setInt(1, empId); ps.setString(2, tfLeaveType.getText().trim());
                    ps.setString(3, start); ps.setString(4, end); ps.setString(5, (String)cbStatus.getSelectedItem()); ps.setInt(6, selectedId);
                    ps.executeUpdate();
                }
                UIHelper.showSuccess(this, "Updated!");
            }
            clearForm(); loadData();
        } catch (SQLException e) { UIHelper.showError(this, "Error: " + e.getMessage()); }
    }

    private void updateStatus(String status) {
        if (selectedId == -1) { UIHelper.showError(this, "Select a leave application first."); return; }
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement("UPDATE leaves SET status=? WHERE leave_id=?")) {
            ps.setString(1, status); ps.setInt(2, selectedId);
            ps.executeUpdate();
            UIHelper.showSuccess(this, "Leave " + status.toLowerCase() + "!");
            clearForm(); loadData();
        } catch (SQLException e) { UIHelper.showError(this, "Error: " + e.getMessage()); }
    }

    private void delete() {
        if (selectedId == -1) { UIHelper.showError(this, "Select a record."); return; }
        if (!UIHelper.confirmDelete(this, "leave record")) return;
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement("DELETE FROM leaves WHERE leave_id=?")) {
            ps.setInt(1, selectedId);
            ps.executeUpdate();
            UIHelper.showSuccess(this, "Deleted!"); clearForm(); loadData();
        } catch (SQLException e) { UIHelper.showError(this, "Error: " + e.getMessage()); }
    }

    private void clearForm() {
        selectedId = -1;
        tfEmpId.setText(""); tfLeaveType.setText(""); tfStart.setText(""); tfEnd.setText("");
        cbStatus.setSelectedIndex(0); table.clearSelection();
    }
}