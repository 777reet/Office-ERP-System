import java.sql.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.geom.*;

public class UIHelper {
    // Glassmorphism palette
    public static final Color BG_DARK    = new Color(10,  10,  25);
    public static final Color GLASS      = new Color(255, 255, 255, 18);
    public static final Color GLASS_BDR  = new Color(255, 255, 255, 45);
    public static final Color ACCENT     = new Color(139, 92,  246);
    public static final Color ACCENT2    = new Color(59,  130, 246);
    public static final Color SUCCESS    = new Color(16,  185, 129);
    public static final Color DANGER     = new Color(239, 68,  68);
    public static final Color TEXT       = new Color(240, 240, 255);
    public static final Color TEXT_MUTED = new Color(160, 160, 200);
    public static final Color TABLE_ROW1 = new Color(255, 255, 255, 12);
    public static final Color TABLE_ROW2 = new Color(255, 255, 255, 5);
    public static final Color TABLE_SEL  = new Color(139, 92,  246, 80);

    // ── Glass card panel ────────────────────────────────────────────────────
    public static JPanel glassPanel() {
        JPanel p = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(GLASS);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 24, 24);
                g2.setColor(GLASS_BDR);
                g2.setStroke(new BasicStroke(1f));
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 24, 24);
                g2.dispose();
            }
        };
        p.setOpaque(false);
        p.setBorder(new EmptyBorder(20, 18, 20, 18));
        return p;
    }

    // Keep old name for compatibility
    public static JPanel cardPanel() { return glassPanel(); }
    public static Border cardBorder() {
        return BorderFactory.createLineBorder(new Color(255,255,255,30), 1, true);
    }

    // ── Styled text field ───────────────────────────────────────────────────
    public static JTextField styledField(String placeholder) {
        JTextField f = new JTextField() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(255,255,255,15));
                g2.fillRoundRect(0, 0, getWidth()-1, getHeight()-1, 12, 12);
                g2.dispose();
                super.paintComponent(g);
            }
            @Override protected void paintBorder(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(hasFocus() ? ACCENT : new Color(255,255,255,40));
                g2.setStroke(new BasicStroke(hasFocus() ? 2f : 1f));
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 12, 12);
                g2.dispose();
            }
        };
        f.setOpaque(false);
        f.setBorder(new EmptyBorder(9, 13, 9, 13));
        f.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        f.setForeground(TEXT);
        f.setCaretColor(ACCENT);
        f.setBackground(new Color(0,0,0,0));
        return f;
    }

    // ── Styled combo box ────────────────────────────────────────────────────
    public static JComboBox<String> styledCombo(String[] items) {
        JComboBox<String> cb = new JComboBox<>(items);
        cb.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        cb.setBackground(new Color(30, 20, 60));
        cb.setForeground(TEXT);
        cb.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(255,255,255,40), 1, true),
            new EmptyBorder(4, 8, 4, 8)
        ));
        cb.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        cb.setRenderer(new DefaultListCellRenderer() {
            @Override public Component getListCellRendererComponent(JList<?> l, Object val, int idx, boolean sel, boolean foc) {
                super.getListCellRendererComponent(l, val, idx, sel, foc);
                setBackground(sel ? new Color(139,92,246,180) : new Color(30,20,60));
                setForeground(TEXT);
                setBorder(new EmptyBorder(6, 10, 6, 10));
                return this;
            }
        });
        return cb;
    }

    // ── Gradient button ─────────────────────────────────────────────────────
    public static JButton primaryButton(String text, Color color) {
        JButton btn = new JButton(text) {
            boolean hover = false;
            { addMouseListener(new java.awt.event.MouseAdapter() {
                public void mouseEntered(java.awt.event.MouseEvent e) { hover=true; repaint(); }
                public void mouseExited(java.awt.event.MouseEvent e)  { hover=false; repaint(); }
            }); }
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color c1 = hover ? color.brighter() : color;
                Color c2 = color.darker();
                GradientPaint gp = new GradientPaint(0, 0, c1, 0, getHeight(), c2);
                g2.setPaint(gp);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 14, 14);
                if (hover) {
                    g2.setColor(new Color(255,255,255,40));
                    g2.setStroke(new BasicStroke(1.5f));
                    g2.drawRoundRect(1,1,getWidth()-2,getHeight()-2,14,14);
                }
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setOpaque(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setBorder(new EmptyBorder(9, 18, 9, 18));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setFocusPainted(false);
        return btn;
    }

    // ── Dark glass table ────────────────────────────────────────────────────
    public static JTable styledTable(DefaultTableModel model) {
        JTable table = new JTable(model) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.setRowHeight(40);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 2));
        table.setOpaque(false);
        table.setBackground(new Color(0,0,0,0));
        table.setSelectionBackground(TABLE_SEL);
        table.setSelectionForeground(Color.WHITE);
        table.setForeground(TEXT);
        table.setFillsViewportHeight(true);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Header
        JTableHeader header = table.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 12));
        header.setForeground(TEXT_MUTED);
        header.setOpaque(false);
        header.setBackground(new Color(255,255,255,10));
        header.setBorder(BorderFactory.createMatteBorder(0,0,1,0, new Color(255,255,255,30)));
        header.setPreferredSize(new Dimension(0, 42));
        header.setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int r, int c) {
                super.getTableCellRendererComponent(t,v,s,f,r,c);
                setBackground(new Color(255,255,255,10));
                setForeground(TEXT_MUTED);
                setFont(new Font("Segoe UI", Font.BOLD, 12));
                setBorder(new EmptyBorder(0,14,0,14));
                setOpaque(true);
                return this;
            }
        });

        // Row renderer with glass rows
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foc, int row, int col) {
                super.getTableCellRendererComponent(t,v,sel,foc,row,col);
                if (sel) {
                    setBackground(new Color(139,92,246,90));
                    setForeground(Color.WHITE);
                } else {
                    setBackground(row%2==0 ? new Color(255,255,255,10) : new Color(255,255,255,5));
                    setForeground(TEXT);
                }
                setBorder(new EmptyBorder(0,14,0,14));
                setOpaque(true);
                return this;
            }
        });

        return table;
    }

    // ── Scroll pane wrapper ─────────────────────────────────────────────────
    public static JScrollPane glassScroll(JTable table) {
        JScrollPane sp = new JScrollPane(table) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(255,255,255,12));
                g2.fillRoundRect(0,0,getWidth(),getHeight(),20,20);
                g2.setColor(new Color(255,255,255,35));
                g2.setStroke(new BasicStroke(1f));
                g2.drawRoundRect(0,0,getWidth()-1,getHeight()-1,20,20);
                g2.dispose();
            }
        };
        sp.setOpaque(false);
        sp.getViewport().setOpaque(false);
        sp.getViewport().setBackground(new Color(0,0,0,0));
        sp.setBorder(BorderFactory.createEmptyBorder());
        sp.getVerticalScrollBar().setOpaque(false);
        sp.getVerticalScrollBar().setBackground(new Color(0,0,0,0));
        return sp;
    }

    // ── Dialogs ─────────────────────────────────────────────────────────────
    public static void showSuccess(Component parent, String msg) {
        JOptionPane.showMessageDialog(SwingUtilities.getWindowAncestor(parent), msg, "Success ✓", JOptionPane.INFORMATION_MESSAGE);
    }
    public static void showError(Component parent, String msg) {
        JOptionPane.showMessageDialog(SwingUtilities.getWindowAncestor(parent), msg, "Error", JOptionPane.ERROR_MESSAGE);
    }
    public static boolean confirmDelete(Component parent, String item) {
        return JOptionPane.showConfirmDialog(SwingUtilities.getWindowAncestor(parent),
            "Delete this " + item + "? This cannot be undone.",
            "Confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) == JOptionPane.YES_OPTION;
    }
}