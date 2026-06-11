import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.geom.*;
import java.sql.*;

public class Dashboard extends JPanel {

    private static final Color TEXT_BRIGHT = new Color(240, 240, 255);
    private static final Color TEXT_MUTED  = new Color(150, 150, 190);

    private JLabel lblEmp, lblIncome, lblExpense, lblProjects, lblLeaves, lblAttend;

    public Dashboard() {
        setLayout(new BorderLayout(0, 24));
        setOpaque(false);
        setBorder(new EmptyBorder(30, 24, 30, 30));

        add(buildHeader(), BorderLayout.NORTH);
        add(buildCards(), BorderLayout.CENTER);

        refreshStats();
    }

    private JPanel buildHeader() {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);

        JLabel title = new JLabel("Dashboard Overview");
        title.setFont(new Font("Segoe UI", Font.BOLD, 30));
        title.setForeground(TEXT_BRIGHT);

        JLabel sub = new JLabel("Welcome back, Admin — here's what's happening today");
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        sub.setForeground(TEXT_MUTED);

        JPanel text = new JPanel(new GridLayout(2,1,0,6));
        text.setOpaque(false);
        text.add(title); text.add(sub);
        p.add(text, BorderLayout.WEST);

        // Date badge
        JLabel date = new JLabel(new java.text.SimpleDateFormat("EEE, dd MMM yyyy").format(new java.util.Date()));
        date.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        date.setForeground(TEXT_MUTED);
        date.setBorder(new EmptyBorder(0, 0, 20, 0));
        p.add(date, BorderLayout.EAST);
        return p;
    }

    private JPanel buildCards() {
        JPanel grid = new JPanel(new GridLayout(2, 3, 18, 18));
        grid.setOpaque(false);

        lblEmp     = bigLabel("0");
        lblIncome  = bigLabel("₹0");
        lblExpense = bigLabel("₹0");
        lblProjects= bigLabel("0");
        lblLeaves  = bigLabel("0");
        lblAttend  = bigLabel("0");

        grid.add(glassCard("Total Employees",   lblEmp,      "•", new Color(139, 92,  246), new Color(109, 40,  217)));
        grid.add(glassCard("Total Income",      lblIncome,   "•", new Color(16,  185, 129), new Color(5,   150, 105)));
        grid.add(glassCard("Total Expense",     lblExpense,  "•", new Color(239, 68,  68),  new Color(185, 28,  28)));
        grid.add(glassCard("Active Projects",   lblProjects, "•", new Color(245, 158, 11),  new Color(180, 108, 0)));
        grid.add(glassCard("Pending Leaves",    lblLeaves,   "•", new Color(99,  102, 241), new Color(67,  56,  202)));
        grid.add(glassCard("Today's Present",   lblAttend,   "•", new Color(20,  184, 166), new Color(15,  118, 110)));

        return grid;
    }

    private JLabel bigLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", Font.BOLD, 36));
        return l;
    }

    private JPanel glassCard(String title, JLabel valueLabel, String emoji, Color accent, Color accentDark) {
        JPanel card = new JPanel(new GridBagLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Frosted glass background
                g2.setColor(new Color(255, 255, 255, 18));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 24, 24);

                // Subtle gradient overlay
                GradientPaint gp = new GradientPaint(0, 0, new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 40),
                        0, getHeight(), new Color(0,0,0,0));
                g2.setPaint(gp);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 24, 24);

                // Glass border
                g2.setColor(new Color(255, 255, 255, 45));
                g2.setStroke(new BasicStroke(1f));
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 24, 24);

                // Top accent line
                g2.setPaint(new GradientPaint(0,0, accent, getWidth(), 0, accentDark));
                g2.setStroke(new BasicStroke(3f));
                g2.drawLine(24, 0, getWidth()-24, 0);

                // Glow orb
                g2.setColor(new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 30));
                g2.fillOval(getWidth()-80, -20, 100, 100);

                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setBorder(new EmptyBorder(22, 22, 22, 22));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx=0; gbc.gridy=0; gbc.anchor=GridBagConstraints.WEST; gbc.weightx=1; gbc.fill=GridBagConstraints.HORIZONTAL;

        // Emoji badge
        JLabel emojiLbl = new JLabel(emoji);
        emojiLbl.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 26));
        card.add(emojiLbl, gbc);

        gbc.gridy=1; gbc.insets=new Insets(10,0,4,0);
        valueLabel.setForeground(Color.WHITE);
        card.add(valueLabel, gbc);

        gbc.gridy=2; gbc.insets=new Insets(0,0,0,0);
        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        titleLbl.setForeground(new Color(200, 200, 230));
        card.add(titleLbl, gbc);

        return card;
    }

    public void refreshStats() {
        SwingWorker<Void, Void> w = new SwingWorker<>() {
            double income=0, expense=0;
            int emp=0, proj=0, leaves=0, attend=0;
            @Override protected Void doInBackground() {
                try (Connection conn = DBConnection.getConnection()) {
                    try (PreparedStatement ps = conn.prepareStatement("SELECT COUNT(*) FROM employees"); ResultSet rs = ps.executeQuery()) { if(rs.next()) emp=rs.getInt(1); }
                    try (PreparedStatement ps = conn.prepareStatement("SELECT SUM(amount) FROM finance WHERE type='Income'"); ResultSet rs = ps.executeQuery()) { if(rs.next()) income=rs.getDouble(1); }
                    try (PreparedStatement ps = conn.prepareStatement("SELECT SUM(amount) FROM finance WHERE type='Expense'"); ResultSet rs = ps.executeQuery()) { if(rs.next()) expense=rs.getDouble(1); }
                    try (PreparedStatement ps = conn.prepareStatement("SELECT COUNT(*) FROM projects"); ResultSet rs = ps.executeQuery()) { if(rs.next()) proj=rs.getInt(1); }
                    try (PreparedStatement ps = conn.prepareStatement("SELECT COUNT(*) FROM leaves WHERE status='Pending'"); ResultSet rs = ps.executeQuery()) { if(rs.next()) leaves=rs.getInt(1); }
                    try (PreparedStatement ps = conn.prepareStatement("SELECT COUNT(*) FROM attendance WHERE date=CURDATE() AND status='Present'"); ResultSet rs = ps.executeQuery()) { if(rs.next()) attend=rs.getInt(1); }
                } catch (SQLException e) { e.printStackTrace(); }
                return null;
            }
            @Override protected void done() {
                lblEmp.setText(String.valueOf(emp));
                lblIncome.setText(String.format("₹%,.0f", income));
                lblExpense.setText(String.format("₹%,.0f", expense));
                lblProjects.setText(String.valueOf(proj));
                lblLeaves.setText(String.valueOf(leaves));
                lblAttend.setText(String.valueOf(attend));
            }
        };
        w.execute();
    }
}