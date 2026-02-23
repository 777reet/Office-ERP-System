import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;

public class Main extends JFrame {

    private static final Color SIDEBAR_BG = new Color(22, 27, 58);
    private static final Color SIDEBAR_HOVER = new Color(40, 50, 100);
    private static final Color SIDEBAR_ACTIVE = new Color(99, 102, 241);
    private static final Color SIDEBAR_TEXT = new Color(200, 210, 240);
    private static final Color SIDEBAR_TEXT_ACTIVE = Color.WHITE;
    private static final Color CONTENT_BG = new Color(245, 247, 252);

    private CardLayout cardLayout;
    private JPanel contentPanel;
    private Dashboard dashboard;
    private JButton activeButton = null;

    public static void main(String[] args) {
        // Initialize DB
        try {
            DBConnection.initializeTables();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null,
                "Database connection failed!\n\nPlease ensure:\n" +
                "• MySQL is running on localhost:3306\n" +
                "• Database 'office_erp' exists or can be created\n" +
                "• Username: root, Password: root\n" +
                "• MySQL JDBC driver (mysql-connector-java.jar) is in classpath\n\n" +
                "Error: " + e.getMessage(),
                "Database Error", JOptionPane.ERROR_MESSAGE);
        }

        // Apply system look and feel with Swing styles
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
            UIManager.put("Panel.background", new Color(245, 247, 252));
            UIManager.put("OptionPane.background", Color.WHITE);
            UIManager.put("OptionPane.messageForeground", new Color(30, 30, 60));
        } catch (Exception ignored) {}

        SwingUtilities.invokeLater(() -> {
            Main frame = new Main();
            frame.setVisible(true);
        });
    }

    public Main() {
        setTitle("Office ERP System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1280, 780);
        setMinimumSize(new Dimension(1000, 600));
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // Set app icon (programmatic)
        setIconImage(createIcon());

        // Build sidebar
        JPanel sidebar = buildSidebar();
        add(sidebar, BorderLayout.WEST);

        // Build content area
        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);
        contentPanel.setBackground(CONTENT_BG);

        dashboard = new Dashboard();
        EmployeePanel employeePanel = new EmployeePanel();
        FinancePanel financePanel = new FinancePanel();
        AttendancePanel attendancePanel = new AttendancePanel();
        DepartmentPanel departmentPanel = new DepartmentPanel();
        ProjectPanel projectPanel = new ProjectPanel();
        LeavePanel leavePanel = new LeavePanel();

        contentPanel.add(dashboard, "DASHBOARD");
        contentPanel.add(employeePanel, "EMPLOYEES");
        contentPanel.add(financePanel, "FINANCE");
        contentPanel.add(attendancePanel, "ATTENDANCE");
        contentPanel.add(departmentPanel, "DEPARTMENTS");
        contentPanel.add(projectPanel, "PROJECTS");
        contentPanel.add(leavePanel, "LEAVES");

        add(contentPanel, BorderLayout.CENTER);

        // Status bar
        JPanel statusBar = buildStatusBar();
        add(statusBar, BorderLayout.SOUTH);

        // Start at dashboard
        cardLayout.show(contentPanel, "DASHBOARD");
    }

    private JPanel buildSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(SIDEBAR_BG);
        sidebar.setPreferredSize(new Dimension(220, 0));

        // Logo section
        JPanel logoPanel = new JPanel(new BorderLayout());
        logoPanel.setBackground(new Color(15, 20, 45));
        logoPanel.setBorder(new EmptyBorder(22, 20, 22, 20));
        logoPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));

        JLabel logoText = new JLabel("⚡ Office ERP");
        logoText.setFont(new Font("Segoe UI", Font.BOLD, 18));
        logoText.setForeground(Color.WHITE);
        JLabel logoSub = new JLabel("Management System");
        logoSub.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        logoSub.setForeground(new Color(150, 160, 200));

        JPanel logoTextPanel = new JPanel(new GridLayout(2, 1));
        logoTextPanel.setOpaque(false);
        logoTextPanel.add(logoText);
        logoTextPanel.add(logoSub);
        logoPanel.add(logoTextPanel, BorderLayout.CENTER);
        sidebar.add(logoPanel);

        // Nav section label
        sidebar.add(buildSectionLabel("NAVIGATION"));

        // Nav items
        String[][] navItems = {
            {"🏠", "Dashboard", "DASHBOARD"},
            {"👥", "Employees", "EMPLOYEES"},
            {"💰", "Finance", "FINANCE"},
            {"📅", "Attendance", "ATTENDANCE"},
            {"🏢", "Departments", "DEPARTMENTS"},
            {"📁", "Projects", "PROJECTS"},
            {"🏖", "Leave Mgmt", "LEAVES"}
        };

        for (String[] item : navItems) {
            JButton btn = buildNavButton(item[0], item[1], item[2]);
            sidebar.add(btn);
            if ("DASHBOARD".equals(item[2])) {
                setActiveButton(btn);
            }
        }

        sidebar.add(Box.createVerticalGlue());

        // Bottom user info
        JPanel userPanel = new JPanel(new BorderLayout(10, 0));
        userPanel.setBackground(new Color(15, 20, 45));
        userPanel.setBorder(new EmptyBorder(15, 20, 15, 20));
        userPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 70));

        JLabel avatar = new JLabel("👤") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(SIDEBAR_ACTIVE);
                g2.fillOval(0, 0, getWidth(), getHeight());
                g2.dispose();
                super.paintComponent(g);
            }
        };
        avatar.setHorizontalAlignment(JLabel.CENTER);
        avatar.setPreferredSize(new Dimension(38, 38));

        JLabel userName = new JLabel("Admin User");
        userName.setFont(new Font("Segoe UI", Font.BOLD, 13));
        userName.setForeground(Color.WHITE);
        JLabel userRole = new JLabel("Administrator");
        userRole.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        userRole.setForeground(new Color(150, 160, 200));

        JPanel userInfo = new JPanel(new GridLayout(2, 1));
        userInfo.setOpaque(false);
        userInfo.add(userName);
        userInfo.add(userRole);

        userPanel.add(avatar, BorderLayout.WEST);
        userPanel.add(userInfo, BorderLayout.CENTER);
        sidebar.add(userPanel);

        return sidebar;
    }

    private JLabel buildSectionLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 10));
        lbl.setForeground(new Color(100, 115, 160));
        lbl.setBorder(new EmptyBorder(16, 20, 6, 20));
        lbl.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        return lbl;
    }

    private JButton buildNavButton(String icon, String label, String cardName) {
        JButton btn = new JButton(icon + "  " + label) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                boolean isActive = this == activeButton;
                boolean isHover = getModel().isRollover();

                if (isActive) {
                    // Active highlight bar
                    g2.setColor(SIDEBAR_ACTIVE);
                    g2.fillRoundRect(8, 4, getWidth() - 16, getHeight() - 8, 12, 12);
                } else if (isHover) {
                    g2.setColor(SIDEBAR_HOVER);
                    g2.fillRoundRect(8, 4, getWidth() - 16, getHeight() - 8, 12, 12);
                }
                g2.dispose();
                super.paintComponent(g);
            }
        };

        btn.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        btn.setForeground(SIDEBAR_TEXT);
        btn.setOpaque(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setBorder(new EmptyBorder(10, 20, 10, 20));
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setFocusPainted(false);

        btn.addActionListener(e -> {
            setActiveButton(btn);
            cardLayout.show(contentPanel, cardName);
            if ("DASHBOARD".equals(cardName)) {
                dashboard.refreshStats();
            }
        });

        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) { btn.repaint(); }
            @Override
            public void mouseExited(MouseEvent e) { btn.repaint(); }
        });

        return btn;
    }

    private void setActiveButton(JButton btn) {
        if (activeButton != null) {
            activeButton.setForeground(SIDEBAR_TEXT);
            activeButton.repaint();
        }
        activeButton = btn;
        btn.setForeground(SIDEBAR_TEXT_ACTIVE);
        btn.repaint();
    }

    private JPanel buildStatusBar() {
        JPanel bar = new JPanel(new BorderLayout(10, 0));
        bar.setBackground(new Color(22, 27, 58));
        bar.setBorder(new EmptyBorder(5, 15, 5, 15));
        bar.setPreferredSize(new Dimension(0, 28));

        JLabel left = new JLabel("Office ERP System v1.0  |  MySQL Database");
        left.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        left.setForeground(new Color(150, 160, 200));

        JLabel right = new JLabel("© 2025 Office ERP  |  Java + Swing + JDBC");
        right.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        right.setForeground(new Color(120, 130, 180));

        bar.add(left, BorderLayout.WEST);
        bar.add(right, BorderLayout.EAST);
        return bar;
    }

    private Image createIcon() {
        // Create a simple programmatic icon
        BufferedImageHelper img = new BufferedImageHelper(32, 32);
        return img.getImage();
    }

    // Simple helper to generate app icon
    static class BufferedImageHelper {
        private java.awt.image.BufferedImage img;
        BufferedImageHelper(int w, int h) {
            img = new java.awt.image.BufferedImage(w, h, java.awt.image.BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2 = img.createGraphics();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(new Color(99, 102, 241));
            g2.fillRoundRect(0, 0, w, h, 8, 8);
            g2.setColor(Color.WHITE);
            g2.setFont(new Font("Segoe UI", Font.BOLD, 20));
            g2.drawString("E", 8, 24);
            g2.dispose();
        }
        Image getImage() { return img; }
    }
}
