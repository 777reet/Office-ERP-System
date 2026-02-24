import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;

public class Main extends JFrame {

    private static final Color BG_TOP        = new Color(10, 10, 25);
    private static final Color BG_BOT        = new Color(20, 15, 45);
    private static final Color PILL_BG       = new Color(255, 255, 255, 18);
    private static final Color PILL_ACTIVE   = new Color(139, 92, 246);
    private static final Color PILL_HOVER    = new Color(255, 255, 255, 30);
    private static final Color TEXT_BRIGHT   = new Color(240, 240, 255);
    private static final Color TEXT_MUTED    = new Color(150, 150, 190);
    private static final Color ACCENT        = new Color(139, 92, 246);

    private CardLayout cardLayout;
    private JPanel contentPanel;
    private Dashboard dashboard;
    private JButton activeButton = null;

    public static void main(String[] args) {
        try { DBConnection.initializeTables(); } catch (Exception e) {
            JOptionPane.showMessageDialog(null,
                "Database connection failed!\n\nEnsure MySQL is running on localhost:3306\n" +
                "and credentials are correct.\n\nError: " + e.getMessage(),
                "Database Error", JOptionPane.ERROR_MESSAGE);
        }
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
            UIManager.put("OptionPane.background", new Color(20, 15, 45));
            UIManager.put("Panel.background", new Color(20, 15, 45));
        } catch (Exception ignored) {}
        SwingUtilities.invokeLater(() -> new Main().setVisible(true));
    }

    public Main() {
        setTitle("Office ERP System");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1360, 820);
        setMinimumSize(new Dimension(1100, 650));
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        setIconImage(makeIcon());

        // Gradient background base
        JPanel base = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0, BG_TOP, getWidth(), getHeight(), BG_BOT);
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());
                // Subtle orbs
                g2.setColor(new Color(139, 92, 246, 25));
                g2.fillOval(-100, -100, 500, 500);
                g2.setColor(new Color(59, 130, 246, 20));
                g2.fillOval(getWidth() - 300, getHeight() - 300, 500, 500);
            }
        };
        base.setOpaque(false);
        setContentPane(base);

        base.add(buildSidebar(), BorderLayout.WEST);

        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);
        contentPanel.setOpaque(false);

        dashboard = new Dashboard();
        contentPanel.add(dashboard,              "DASHBOARD");
        contentPanel.add(new EmployeePanel(),    "EMPLOYEES");
        contentPanel.add(new FinancePanel(),     "FINANCE");
        contentPanel.add(new AttendancePanel(),  "ATTENDANCE");
        contentPanel.add(new DepartmentPanel(),  "DEPARTMENTS");
        contentPanel.add(new ProjectPanel(),     "PROJECTS");
        contentPanel.add(new LeavePanel(),       "LEAVES");

        base.add(contentPanel, BorderLayout.CENTER);
        base.add(buildStatusBar(), BorderLayout.SOUTH);

        cardLayout.show(contentPanel, "DASHBOARD");
    }

    private JPanel buildSidebar() {
        // Outer wrapper to give padding around pill
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.setBorder(new EmptyBorder(20, 16, 20, 8));
        wrapper.setPreferredSize(new Dimension(210, 0));

        // The pill panel
        JPanel pill = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // Frosted glass pill background
                g2.setColor(new Color(255, 255, 255, 15));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 32, 32);
                // Border glow
                g2.setColor(new Color(255, 255, 255, 40));
                g2.setStroke(new BasicStroke(1f));
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 32, 32);
                g2.dispose();
            }
        };
        pill.setLayout(new BoxLayout(pill, BoxLayout.Y_AXIS));
        pill.setOpaque(false);
        pill.setBorder(new EmptyBorder(20, 10, 20, 10));

        // Logo
        JPanel logo = new JPanel(new GridLayout(2,1,0,2));
        logo.setOpaque(false);
        logo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));
        logo.setBorder(new EmptyBorder(4, 8, 12, 8));
        JLabel logoTitle = new JLabel("⚡ OfficeERP");
        logoTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        logoTitle.setForeground(TEXT_BRIGHT);
        JLabel logoSub = new JLabel("Management Suite");
        logoSub.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        logoSub.setForeground(TEXT_MUTED);
        logo.add(logoTitle); logo.add(logoSub);
        pill.add(logo);

        // Divider
        pill.add(glassDiv());
        pill.add(Box.createVerticalStrut(10));

        // Nav items
        String[][] nav = {
            {"🏠","Dashboard","DASHBOARD"},
            {"👥","Employees","EMPLOYEES"},
            {"💰","Finance","FINANCE"},
            {"📅","Attendance","ATTENDANCE"},
            {"🏢","Departments","DEPARTMENTS"},
            {"📁","Projects","PROJECTS"},
            {"🏖","Leave Mgmt","LEAVES"},
        };
        for (String[] item : nav) {
            JButton btn = navBtn(item[0], item[1], item[2]);
            pill.add(btn);
            pill.add(Box.createVerticalStrut(4));
            if ("DASHBOARD".equals(item[2])) setActive(btn);
        }

        pill.add(Box.createVerticalGlue());
        pill.add(glassDiv());

        // User card
        JPanel user = new JPanel(new BorderLayout(8, 0));
        user.setOpaque(false);
        user.setMaximumSize(new Dimension(Integer.MAX_VALUE, 52));
        user.setBorder(new EmptyBorder(10, 8, 4, 8));
        JLabel avatar = new JLabel("A") {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(ACCENT);
                g2.fillOval(0, 0, getWidth(), getHeight());
                g2.dispose();
                super.paintComponent(g);
            }
        };
        avatar.setHorizontalAlignment(JLabel.CENTER);
        avatar.setForeground(Color.WHITE);
        avatar.setFont(new Font("Segoe UI", Font.BOLD, 14));
        avatar.setPreferredSize(new Dimension(34, 34));
        JPanel info = new JPanel(new GridLayout(2,1));
        info.setOpaque(false);
        JLabel uname = new JLabel("Admin User");
        uname.setFont(new Font("Segoe UI", Font.BOLD, 12));
        uname.setForeground(TEXT_BRIGHT);
        JLabel urole = new JLabel("Administrator");
        urole.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        urole.setForeground(TEXT_MUTED);
        info.add(uname); info.add(urole);
        user.add(avatar, BorderLayout.WEST);
        user.add(info, BorderLayout.CENTER);
        pill.add(user);

        wrapper.add(pill, BorderLayout.CENTER);
        return wrapper;
    }

    private JComponent glassDiv() {
        JPanel d = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                g.setColor(new Color(255,255,255,25));
                g.fillRect(0, 3, getWidth(), 1);
            }
        };
        d.setOpaque(false);
        d.setMaximumSize(new Dimension(Integer.MAX_VALUE, 8));
        return d;
    }

    private JButton navBtn(String icon, String label, String card) {
        JButton btn = new JButton(icon + "  " + label) {
            boolean hover = false;
            {
                addMouseListener(new MouseAdapter() {
                    public void mouseEntered(MouseEvent e) { hover=true; repaint(); }
                    public void mouseExited(MouseEvent e)  { hover=false; repaint(); }
                });
            }
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                boolean active = this == activeButton;
                if (active) {
                    // Glowing active pill
                    g2.setColor(new Color(139, 92, 246, 200));
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
                    // Inner glow
                    g2.setColor(new Color(167, 139, 250, 80));
                    g2.setStroke(new BasicStroke(1.5f));
                    g2.drawRoundRect(1, 1, getWidth()-2, getHeight()-2, 16, 16);
                } else if (hover) {
                    g2.setColor(new Color(255, 255, 255, 25));
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
                }
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        btn.setForeground(this.activeButton == null ? TEXT_BRIGHT : TEXT_MUTED);
        btn.setOpaque(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setBorder(new EmptyBorder(10, 14, 10, 14));
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setFocusPainted(false);
        btn.addActionListener(e -> {
            setActive(btn);
            cardLayout.show(contentPanel, card);
            if ("DASHBOARD".equals(card)) dashboard.refreshStats();
        });
        return btn;
    }

    private void setActive(JButton btn) {
        if (activeButton != null) {
            activeButton.setForeground(TEXT_MUTED);
            activeButton.repaint();
        }
        activeButton = btn;
        btn.setForeground(TEXT_BRIGHT);
        btn.repaint();
    }

    private JPanel buildStatusBar() {
        JPanel bar = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(new Color(255,255,255,8));
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.setColor(new Color(255,255,255,20));
                g2.fillRect(0, 0, getWidth(), 1);
                g2.dispose();
            }
        };
        bar.setOpaque(false);
        bar.setBorder(new EmptyBorder(5, 20, 5, 20));
        bar.setPreferredSize(new Dimension(0, 28));
        JLabel l = new JLabel("⚡ Office ERP v1.0  |  Java + Swing + JDBC + MySQL");
        l.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        l.setForeground(TEXT_MUTED);
        JLabel r = new JLabel("© 2025 Office ERP System");
        r.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        r.setForeground(TEXT_MUTED);
        bar.add(l, BorderLayout.WEST);
        bar.add(r, BorderLayout.EAST);
        return bar;
    }

    private Image makeIcon() {
        java.awt.image.BufferedImage img = new java.awt.image.BufferedImage(32,32, java.awt.image.BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = img.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setPaint(new GradientPaint(0,0,new Color(139,92,246),32,32,new Color(59,130,246)));
        g2.fillRoundRect(0,0,32,32,10,10);
        g2.setColor(Color.WHITE); g2.setFont(new Font("Segoe UI",Font.BOLD,18));
        g2.drawString("E",9,24); g2.dispose();
        return img;
    }
}