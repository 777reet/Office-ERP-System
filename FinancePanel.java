import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.sql.*;

public class FinancePanel extends JPanel {
    private static final Color SUCCESS = new Color(16, 185, 129);
    private static final Color DANGER = new Color(239, 68, 68);
    private static final Color PRIMARY = new Color(99, 102, 241);

    private JTable table;
    private DefaultTableModel tableModel;
    private JComboBox<String> cbType;
    private JTextField tfAmount, tfDate, tfDesc;
    private int selectedId = -1;

    public FinancePanel() {
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
        JLabel lbl = new JLabel("💰 Finance Management");
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lbl.setForeground(new Color(30, 30, 60));
        p.add(lbl, BorderLayout.WEST);
        return p;
    }

    private JScrollPane buildTable() {
        String[] cols = {"ID", "Type", "Amount (₹)", "Date", "Description"};
        tableModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        table = UIHelper.styledTable(tableModel);

        // Color rows by type
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object val, boolean sel, boolean foc, int row, int col) {
                Component c = super.getTableCellRendererComponent(t, val, sel, foc, row, col);
                if (!sel) {
                    String type = (String) tableModel.getValueAt(row, 1);
                    c.setBackground("Income".equals(type) ? new Color(240, 253, 244) : new Color(255, 242, 242));
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

        JLabel title = new JLabel("Transaction Details");
        title.setFont(new Font("Segoe UI", Font.BOLD, 15));
        title.setForeground(new Color(30, 30, 60));
        title.setAlignmentX(LEFT_ALIGNMENT);
        card.add(title);
        card.add(Box.createVerticalStrut(15));

        cbType = UIHelper.styledCombo(new String[]{"Income", "Expense"});
        tfAmount = UIHelper.styledField("Amount");
        tfDate = UIHelper.styledField("YYYY-MM-DD");
        tfDesc = UIHelper.styledField("Description");

        addRow(card, "Type *", cbType);
        addRow(card, "Amount *", tfAmount);
        addRow(card, "Date *", tfDate);
        addRow(card, "Description", tfDesc);

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
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT * FROM finance ORDER BY date DESC")) {
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                tableModel.addRow(new Object[]{
                    rs.getInt("finance_id"), rs.getString("type"),
                    String.format("%.2f", rs.getDouble("amount")),
                    rs.getString("date"), rs.getString("description")
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
        cbType.setSelectedItem(tableModel.getValueAt(row, 1));
        tfAmount.setText(tableModel.getValueAt(row, 2).toString());
        tfDate.setText(tableModel.getValueAt(row, 3).toString());
        tfDesc.setText((String) tableModel.getValueAt(row, 4));
    }

    private void save() {
        String type = (String) cbType.getSelectedItem();
        String amtStr = tfAmount.getText().trim();
        String date = tfDate.getText().trim();
        if (amtStr.isEmpty() || date.isEmpty()) { UIHelper.showError(this, "Amount and Date are required."); return; }
        double amount;
        try { amount = Double.parseDouble(amtStr); } catch (NumberFormatException e) { UIHelper.showError(this, "Invalid amount."); return; }

        try (Connection conn = DBConnection.getConnection()) {
            if (selectedId == -1) {
                try (PreparedStatement ps = conn.prepareStatement("INSERT INTO finance (type, amount, date, description) VALUES (?,?,?,?)")) {
                    ps.setString(1, type); ps.setDouble(2, amount);
                    ps.setString(3, date); ps.setString(4, tfDesc.getText().trim());
                    ps.executeUpdate();
                }
                UIHelper.showSuccess(this, "Transaction added!");
            } else {
                try (PreparedStatement ps = conn.prepareStatement("UPDATE finance SET type=?, amount=?, date=?, description=? WHERE finance_id=?")) {
                    ps.setString(1, type); ps.setDouble(2, amount);
                    ps.setString(3, date); ps.setString(4, tfDesc.getText().trim());
                    ps.setInt(5, selectedId);
                    ps.executeUpdate();
                }
                UIHelper.showSuccess(this, "Transaction updated!");
            }
            clearForm(); loadData();
        } catch (SQLException e) {
            UIHelper.showError(this, "Error: " + e.getMessage());
        }
    }

    private void delete() {
        if (selectedId == -1) { UIHelper.showError(this, "Select a record to delete."); return; }
        if (!UIHelper.confirmDelete(this, "transaction")) return;
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement("DELETE FROM finance WHERE finance_id=?")) {
            ps.setInt(1, selectedId);
            ps.executeUpdate();
            UIHelper.showSuccess(this, "Deleted successfully!");
            clearForm(); loadData();
        } catch (SQLException e) {
            UIHelper.showError(this, "Error: " + e.getMessage());
        }
    }

    private void clearForm() {
        selectedId = -1;
        cbType.setSelectedIndex(0);
        tfAmount.setText(""); tfDate.setText(""); tfDesc.setText("");
        table.clearSelection();
    }
}
