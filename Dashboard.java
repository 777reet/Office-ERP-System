import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.sql.*;

public class Dashboard extends JPanel {
    private static final Color CARD_BG = new Color(255, 255, 255);
    private static final Color TEXT_DARK = new Color(30, 30, 60);
    private static final Color TEXT_MUTED = new Color(120, 130, 160);

    private JLabel lblEmployees, lblIncome, lblExpense, lblProjects, lblLeaves, lblAttendance;

    public Dashboard() {
        setLayout(new BorderLayout(20, 20));
        setBackground(new Color(245, 247, 252));
        setBorder(new EmptyBorder(30, 30, 30, 30));

        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        JLabel title = new JLabel("Dashboard Overview");
        title.setFont(new Font("Segoe UI", Font.BOLD, 28));
        title.setForeground(TEXT_DARK);
        JLabel subtitle = new JLabel("Welcome to Office ERP System");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subtitle.setForeground(TEXT_MUTED);
        JPanel titlePanel = new JPanel(new GridLayout(2,1,0,4));
        titlePanel.setOpaque(false);
        titlePanel.add(title);
        titlePanel.add(subtitle);
        header.add(titlePanel, BorderLayout.WEST);
        add(header, BorderLayout.NORTH);

        // Cards grid
        JPanel cardsPanel = new JPanel(new GridLayout(2, 3, 20, 20));
        cardsPanel.setOpaque(false);

        lblEmployees = new JLabel("0");
        lblIncome = new JLabel("₹0");
        lblExpense = new JLabel("₹0");
        lblProjects = new JLabel("0");
        lblLeaves = new JLabel("0");
        lblAttendance = new JLabel("0");

        cardsPanel.add(createStatCard("👥 Total Employees", lblEmployees, new Color(99, 102, 241), new Color(238, 242, 255)));
        cardsPanel.add(createStatCard("💰 Total Income", lblIncome, new Color(16, 185, 129), new Color(236, 253, 245)));
        cardsPanel.add(createStatCard("💸 Total Expense", lblExpense, new Color(239, 68, 68), new Color(254, 242, 242)));
        cardsPanel.add(createStatCard("📁 Active Projects", lblProjects, new Color(245, 158, 11), new Color(255, 251, 235)));
        cardsPanel.add(createStatCard("🏖 Pending Leaves", lblLeaves, new Color(139, 92, 246), new Color(245, 243, 255)));
        cardsPanel.add(createStatCard("📅 Today's Present", lblAttendance, new Color(20, 184, 166), new Color(240, 253, 250)));

        add(cardsPanel, BorderLayout.CENTER);

        // Bottom info
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setOpaque(false);
        JLabel info = new JLabel("Data updates on each visit to this dashboard.");
        info.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        info.setForeground(TEXT_MUTED);
        bottomPanel.add(info, BorderLayout.WEST);
        add(bottomPanel, BorderLayout.SOUTH);

        refreshStats();
    }

    private JPanel createStatCard(String title, JLabel valueLabel, Color accent, Color bg) {
        JPanel card = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(CARD_BG);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                // Left accent bar
                g2.setColor(accent);
                g2.fillRoundRect(0, 0, 6, getHeight(), 6, 6);
                g2.dispose();
            }
        };
        card.setBorder(new EmptyBorder(20, 24, 20, 24));
        card.setOpaque(false);
        // Shadow effect via compound border
        card.setBorder(BorderFactory.createCompoundBorder(
            new ShadowBorder(),
            new EmptyBorder(20, 24, 20, 24)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0; gbc.gridy = 0; gbc.anchor = GridBagConstraints.WEST; gbc.weightx = 1;

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        titleLabel.setForeground(TEXT_MUTED);
        card.add(titleLabel, gbc);

        gbc.gridy = 1; gbc.insets = new Insets(10, 0, 0, 0);
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 32));
        valueLabel.setForeground(accent);
        card.add(valueLabel, gbc);

        return card;
    }

    public void refreshStats() {
        SwingWorker<int[], Void> worker = new SwingWorker<>() {
            int[] stats = new int[4];
            double income = 0, expense = 0;

            @Override
            protected int[] doInBackground() {
                try (Connection conn = DBConnection.getConnection()) {
                    try (PreparedStatement ps = conn.prepareStatement("SELECT COUNT(*) FROM employees");
                         ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) stats[0] = rs.getInt(1);
                    }
                    try (PreparedStatement ps = conn.prepareStatement("SELECT SUM(amount) FROM finance WHERE type='Income'");
                         ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) income = rs.getDouble(1);
                    }
                    try (PreparedStatement ps = conn.prepareStatement("SELECT SUM(amount) FROM finance WHERE type='Expense'");
                         ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) expense = rs.getDouble(1);
                    }
                    try (PreparedStatement ps = conn.prepareStatement("SELECT COUNT(*) FROM projects");
                         ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) stats[1] = rs.getInt(1);
                    }
                    try (PreparedStatement ps = conn.prepareStatement("SELECT COUNT(*) FROM leaves WHERE status='Pending'");
                         ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) stats[2] = rs.getInt(1);
                    }
                    try (PreparedStatement ps = conn.prepareStatement("SELECT COUNT(*) FROM attendance WHERE date=CURDATE() AND status='Present'");
                         ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) stats[3] = rs.getInt(1);
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                return stats;
            }

            @Override
            protected void done() {
                lblEmployees.setText(String.valueOf(stats[0]));
                lblIncome.setText(String.format("₹%,.0f", income));
                lblExpense.setText(String.format("₹%,.0f", expense));
                lblProjects.setText(String.valueOf(stats[1]));
                lblLeaves.setText(String.valueOf(stats[2]));
                lblAttendance.setText(String.valueOf(stats[3]));
            }
        };
        worker.execute();
    }

    // Simple shadow border class
    static class ShadowBorder extends AbstractBorder {
        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(new Color(0, 0, 0, 15));
            g2.fillRoundRect(x + 2, y + 3, width - 3, height - 3, 20, 20);
            g2.setColor(new Color(0, 0, 0, 8));
            g2.fillRoundRect(x + 4, y + 5, width - 5, height - 5, 20, 20);
            g2.dispose();
        }
        @Override
        public Insets getBorderInsets(Component c) { return new Insets(5, 5, 8, 8); }
    }
}
