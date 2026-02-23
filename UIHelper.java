import java.sql.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;

public class UIHelper {
    public static final Color PRIMARY = new Color(99, 102, 241);
    public static final Color BG = new Color(245, 247, 252);
    public static final Color CARD = Color.WHITE;
    public static final Color TEXT = new Color(30, 30, 60);
    public static final Color MUTED = new Color(120, 130, 160);

    public static JTextField styledField(String placeholder) {
        JTextField f = new JTextField() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth()-1, getHeight()-1, 10, 10);
                g2.dispose();
                super.paintComponent(g);
            }
            @Override
            protected void paintBorder(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(hasFocus() ? PRIMARY : new Color(200, 210, 230));
                g2.setStroke(new BasicStroke(hasFocus() ? 2 : 1));
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 10, 10);
                g2.dispose();
            }
        };
        f.setOpaque(false);
        f.setBorder(new EmptyBorder(8, 12, 8, 12));
        f.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        f.setForeground(TEXT);
        f.setBackground(new Color(250, 251, 255));
        return f;
    }

    public static JComboBox<String> styledCombo(String[] items) {
        JComboBox<String> cb = new JComboBox<>(items);
        cb.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        cb.setBackground(new Color(250, 251, 255));
        cb.setForeground(TEXT);
        cb.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 210, 230), 1, true),
            new EmptyBorder(4, 8, 4, 8)
        ));
        cb.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        return cb;
    }

    public static JButton primaryButton(String text, Color color) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (getModel().isPressed()) {
                    g2.setColor(color.darker());
                } else if (getModel().isRollover()) {
                    g2.setColor(color.brighter());
                } else {
                    g2.setColor(color);
                }
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setOpaque(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setBorder(new EmptyBorder(8, 16, 8, 16));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    public static JTable styledTable(DefaultTableModel model) {
        JTable table = new JTable(model);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.setRowHeight(38);
        table.setShowVerticalLines(false);
        table.setShowHorizontalLines(true);
        table.setGridColor(new Color(235, 238, 250));
        table.setSelectionBackground(new Color(238, 242, 255));
        table.setSelectionForeground(new Color(30, 30, 60));
        table.setBackground(Color.WHITE);
        table.setIntercellSpacing(new Dimension(0, 1));
        table.setFillsViewportHeight(true);

        JTableHeader header = table.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 12));
        header.setBackground(new Color(245, 247, 252));
        header.setForeground(new Color(80, 90, 130));
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, new Color(220, 225, 245)));
        header.setPreferredSize(new Dimension(0, 40));

        // Stripe renderer
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object val, boolean sel, boolean foc, int row, int col) {
                Component c = super.getTableCellRendererComponent(t, val, sel, foc, row, col);
                if (!sel) {
                    c.setBackground(row % 2 == 0 ? Color.WHITE : new Color(250, 251, 255));
                }
                setBorder(new EmptyBorder(0, 12, 0, 12));
                return c;
            }
        });

        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        return table;
    }

    public static JPanel cardPanel() {
        JPanel p = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // Shadow
                g2.setColor(new Color(0,0,0,12));
                g2.fillRoundRect(3, 4, getWidth()-4, getHeight()-4, 16, 16);
                // Card
                g2.setColor(CARD);
                g2.fillRoundRect(0, 0, getWidth()-4, getHeight()-4, 16, 16);
                g2.dispose();
            }
        };
        p.setOpaque(false);
        p.setBorder(new EmptyBorder(20, 18, 20, 18));
        return p;
    }

    public static Border cardBorder() {
        return BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(225, 230, 245), 1, true),
            new EmptyBorder(0,0,0,0)
        );
    }

    public static void showSuccess(Component parent, String msg) {
        JOptionPane.showMessageDialog(SwingUtilities.getWindowAncestor(parent),
            msg, "Success", JOptionPane.INFORMATION_MESSAGE);
    }

    public static void showError(Component parent, String msg) {
        JOptionPane.showMessageDialog(SwingUtilities.getWindowAncestor(parent),
            msg, "Error", JOptionPane.ERROR_MESSAGE);
    }

    public static boolean confirmDelete(Component parent, String item) {
        return JOptionPane.showConfirmDialog(
            SwingUtilities.getWindowAncestor(parent),
            "Are you sure you want to delete this " + item + "?\nThis action cannot be undone.",
            "Confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE
        ) == JOptionPane.YES_OPTION;
    }
}
