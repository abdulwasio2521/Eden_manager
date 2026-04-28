package edenmanager;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.print.*;
import java.util.List;

/**
 * ReportFrame.java  —  v3
 *
 * ROOT layout: BorderLayout
 *   NORTH  — Gradient navbar with Print button
 *   CENTER — JTabbedPane (Plants tab | Users tab)
 *             Each tab is a JScrollPane containing a styled JTable.
 *   SOUTH  — Stats footer bar
 *
 * Key fixes
 * ─────────
 * • setExtendedState(MAXIMIZED_BOTH) — opens maximised.
 * • Tables use AUTO_RESIZE_ALL_COLUMNS so columns stretch to fill width.
 * • Alternating row renderer — same cream/parchment pattern.
 * • Navbar Print button is a plain opaque JButton.
 */
public class ReportFrame extends JFrame implements Printable {

    private final List<Plant> plants;
    private final List<User>  users;
    private JTable            plantTable;
    private JTable            userTable;

    public ReportFrame(List<Plant> plants, List<User> users) {
        this.plants = plants;
        this.users  = users;
        initUI();
    }

    private void initUI() {
        setTitle("Eden Manager — Reports");
        setMinimumSize(new Dimension(640, 440));
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());
        getContentPane().setBackground(UITheme.PARCHMENT);

        add(buildNavBar(),  BorderLayout.NORTH);
        add(buildTabs(),    BorderLayout.CENTER);
        add(buildFooter(),  BorderLayout.SOUTH);
    }

    // ── Navbar ────────────────────────────────────────────────────────────────
    private JPanel buildNavBar() {
        JPanel bar = UITheme.buildNavBar("📋  System Report");

        JButton btnPrint = new JButton("🖨  Print Report");
        btnPrint.setFont(UITheme.FONT_LABEL);
        btnPrint.setForeground(UITheme.INK);
        btnPrint.setBackground(UITheme.SAGE_LIGHT);
        btnPrint.setOpaque(true);
        btnPrint.setContentAreaFilled(true);
        btnPrint.setBorderPainted(false);
        btnPrint.setFocusPainted(false);
        btnPrint.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnPrint.setBorder(BorderFactory.createEmptyBorder(6, 16, 6, 16));
        btnPrint.addActionListener(e -> printReport());

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        right.setOpaque(false);
        right.add(btnPrint);
        bar.add(right, BorderLayout.EAST);
        return bar;
    }

    // ── Tabs ──────────────────────────────────────────────────────────────────
    private JTabbedPane buildTabs() {
        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(UITheme.FONT_SUBHEAD);
        tabs.setBackground(UITheme.PARCHMENT);
        tabs.setBorder(BorderFactory.createEmptyBorder(
                UITheme.PAD_MD, UITheme.PAD_MD, 0, UITheme.PAD_MD));

        tabs.addTab("🌿  Plants (" + plants.size() + ")", buildPlantTab());
        tabs.addTab("👥  Users (" + users.size() + ")",   buildUserTab());
        return tabs;
    }

    private JScrollPane buildPlantTab() {
        String[] cols = {"ID", "Name", "Species", "Category", "Description"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        for (Plant p : plants)
            model.addRow(new Object[]{
                p.getId(), p.getName(), p.getSpecies(),
                p.getCategory(), trunc(p.getDescription(), 60)});
        plantTable = makeTable(model);
        return tableScroll(plantTable);
    }

    private JScrollPane buildUserTab() {
        String[] cols = {"ID", "Full Name", "Username", "Role"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        for (User u : users)
            model.addRow(new Object[]{
                u.getId(), u.getFullname(), u.getUsername(), u.getRole()});
        userTable = makeTable(model);

        // Role column colour
        userTable.getColumnModel().getColumn(3).setCellRenderer(
                new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v,
                    boolean sel, boolean foc, int row, int col) {
                Component c = super.getTableCellRendererComponent(
                        t, v, sel, foc, row, col);
                if (!sel) {
                    c.setBackground(row % 2 == 0 ? UITheme.CREAM : UITheme.PARCHMENT);
                    boolean admin = "Admin".equals(v);
                    c.setForeground(admin ? UITheme.FERN : UITheme.INK_LIGHT);
                    setFont(admin ? UITheme.FONT_LABEL : UITheme.FONT_BODY);
                }
                setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
                return c;
            }
        });
        return tableScroll(userTable);
    }

    // ── Footer ────────────────────────────────────────────────────────────────
    private JPanel buildFooter() {
        JPanel f = new JPanel(new FlowLayout(FlowLayout.LEFT, UITheme.PAD_LG, 8));
        f.setBackground(UITheme.CREAM);
        f.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, UITheme.CREAM_BORDER));
        long admins = users.stream().filter(User::isAdmin).count();
        f.add(stat("Total Plants", plants.size() + ""));
        f.add(div());
        f.add(stat("Total Users", users.size() + ""));
        f.add(div());
        f.add(stat("Admins", admins + ""));
        f.add(div());
        f.add(stat("Regular Users", (users.size() - admins) + ""));
        return f;
    }

    // ── Print ─────────────────────────────────────────────────────────────────
    private void printReport() {
        PrinterJob job = PrinterJob.getPrinterJob();
        job.setPrintable(this);
        if (job.printDialog()) {
            try {
                job.print();
                JOptionPane.showMessageDialog(this,
                        "Report sent to printer successfully.",
                        "Printed", JOptionPane.INFORMATION_MESSAGE);
            } catch (PrinterException e) {
                JOptionPane.showMessageDialog(this,
                        "Print failed: " + e.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    @Override
    public int print(Graphics g, PageFormat pf, int pageIndex) throws PrinterException {
        if (pageIndex > 0) return NO_SUCH_PAGE;
        Graphics2D g2 = (Graphics2D) g;
        g2.translate(pf.getImageableX(), pf.getImageableY());
        int y = 0;
        g2.setFont(UITheme.FONT_TITLE);
        g2.setColor(UITheme.FERN);
        g2.drawString("Eden Manager — System Report", 0, y += 26);
        g2.setFont(UITheme.FONT_BODY);
        g2.setColor(UITheme.INK_LIGHT);
        g2.drawString("Plants: " + plants.size() + "   |   Users: " + users.size(), 0, y += 20);
        g2.setFont(UITheme.FONT_SUBHEAD);
        g2.setColor(UITheme.INK);
        g2.drawString("Plant Inventory", 0, y += 26);
        try {
            plantTable.print(JTable.PrintMode.FIT_WIDTH,
                    null, null, false, null, false, null);
        } catch (Exception ignored) {}
        return PAGE_EXISTS;
    }

    // =========================================================================
    // HELPERS
    // =========================================================================

    private static JTable makeTable(DefaultTableModel model) {
        JTable t = new JTable(model);
        t.setFont(UITheme.FONT_BODY);
        t.setRowHeight(30);
        t.setShowGrid(false);
        t.setIntercellSpacing(new Dimension(0, 0));
        t.setBackground(UITheme.CREAM);
        t.setSelectionBackground(UITheme.SAGE_LIGHT);
        t.setSelectionForeground(UITheme.INK);
        t.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        t.getTableHeader().setFont(UITheme.FONT_LABEL);
        t.getTableHeader().setBackground(UITheme.PARCHMENT);
        t.getTableHeader().setForeground(UITheme.INK_LIGHT);
        t.getTableHeader().setBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, UITheme.CREAM_BORDER));
        t.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable tbl, Object v,
                    boolean sel, boolean foc, int row, int col) {
                Component c = super.getTableCellRendererComponent(
                        tbl, v, sel, foc, row, col);
                if (!sel) c.setBackground(row % 2 == 0 ? UITheme.CREAM : UITheme.PARCHMENT);
                setFont(UITheme.FONT_BODY);
                setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
                return c;
            }
        });
        return t;
    }

    private static JScrollPane tableScroll(JTable t) {
        JScrollPane sp = new JScrollPane(t);
        sp.setBorder(new UITheme.RoundedBorder(UITheme.CREAM_BORDER, UITheme.RADIUS, 1f));
        sp.setBackground(UITheme.CREAM);
        sp.getViewport().setBackground(UITheme.CREAM);
        sp.getVerticalScrollBar().setUnitIncrement(16);
        return sp;
    }

    private static JPanel stat(String label, String value) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        p.setOpaque(false);
        p.add(UITheme.label(label + ":", UITheme.FONT_SMALL, UITheme.INK_LIGHT));
        p.add(UITheme.label(value, UITheme.FONT_LABEL, UITheme.FERN));
        return p;
    }

    private static JSeparator div() {
        JSeparator s = new JSeparator(SwingConstants.VERTICAL);
        s.setForeground(UITheme.CREAM_BORDER);
        s.setPreferredSize(new Dimension(1, 18));
        return s;
    }

    private static String trunc(String s, int max) {
        if (s == null) return "";
        return s.length() <= max ? s : s.substring(0, max) + "…";
    }
}