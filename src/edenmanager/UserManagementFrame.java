package edenmanager;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.util.List;

/**
 * UserManagementFrame.java  —  v3
 *
 * ROOT layout: BorderLayout
 *   NORTH  — Gradient navbar
 *   CENTER — JScrollPane with full-width styled JTable
 *   SOUTH  — Action buttons (Refresh | Delete)
 *
 * Key fixes
 * ─────────
 * • setExtendedState(MAXIMIZED_BOTH) — table fills the screen.
 * • AUTO_RESIZE_ALL_COLUMNS — columns stretch to fill window width.
 * • Role column uses colour-coded renderer (green for Admin, muted for User).
 * • Action buttons row uses consistent UITheme factory methods.
 */
public class UserManagementFrame extends JFrame {

    private final UserDAO     userDAO;
    private DefaultTableModel tableModel;
    private JTable            userTable;

    public UserManagementFrame(UserDAO userDAO) {
        this.userDAO = userDAO;
        initUI();
        loadUsers();
    }

    // =========================================================================
    // UI CONSTRUCTION
    // =========================================================================

    private void initUI() {
        setTitle("Eden Manager — User Management");
        setMinimumSize(new Dimension(560, 380));
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());
        getContentPane().setBackground(UITheme.PARCHMENT);

        add(buildNavBar(),  BorderLayout.NORTH);
        add(buildTable(),   BorderLayout.CENTER);
        add(buildFooter(),  BorderLayout.SOUTH);
    }

    // ── Navbar ────────────────────────────────────────────────────────────────
    private JPanel buildNavBar() {
        return UITheme.buildNavBar("👥  User Management");
    }

    // ── Table ─────────────────────────────────────────────────────────────────
    private JScrollPane buildTable() {
        String[] cols = {"ID", "Full Name", "Username", "Role"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        userTable = new JTable(tableModel);
        userTable.setFont(UITheme.FONT_BODY);
        userTable.setRowHeight(34);
        userTable.setShowGrid(false);
        userTable.setIntercellSpacing(new Dimension(0, 0));
        userTable.setBackground(UITheme.CREAM);
        userTable.setSelectionBackground(new Color(0xFFE0D6));
        userTable.setSelectionForeground(UITheme.INK);
        userTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        userTable.getTableHeader().setFont(UITheme.FONT_LABEL);
        userTable.getTableHeader().setBackground(UITheme.PARCHMENT);
        userTable.getTableHeader().setForeground(UITheme.INK_LIGHT);
        userTable.getTableHeader().setBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, UITheme.CREAM_BORDER));

        // Base alternating-row renderer
        userTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v,
                    boolean sel, boolean foc, int row, int col) {
                Component c = super.getTableCellRendererComponent(
                        t, v, sel, foc, row, col);
                if (!sel) {
                    c.setBackground(row % 2 == 0 ? UITheme.CREAM : UITheme.PARCHMENT);
                    c.setForeground(UITheme.INK);
                }
                setFont(UITheme.FONT_BODY);
                setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
                return c;
            }
        });

        // Role column — colour-coded
        userTable.getColumnModel().getColumn(3).setCellRenderer(
                new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v,
                    boolean sel, boolean foc, int row, int col) {
                Component c = super.getTableCellRendererComponent(
                        t, v, sel, foc, row, col);
                if (!sel) {
                    c.setBackground(row % 2 == 0 ? UITheme.CREAM : UITheme.PARCHMENT);
                    boolean isAdmin = "Admin".equals(v);
                    c.setForeground(isAdmin ? UITheme.FERN : UITheme.INK_LIGHT);
                    setFont(isAdmin ? UITheme.FONT_LABEL : UITheme.FONT_BODY);
                }
                setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
                return c;
            }
        });

        JScrollPane sp = new JScrollPane(userTable);
        sp.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(
                        UITheme.PAD_MD, UITheme.PAD_MD, 0, UITheme.PAD_MD),
                new UITheme.RoundedBorder(UITheme.CREAM_BORDER, UITheme.RADIUS, 1f)));
        sp.setBackground(UITheme.CREAM);
        sp.getViewport().setBackground(UITheme.CREAM);
        sp.getVerticalScrollBar().setUnitIncrement(16);
        return sp;
    }

    // ── Footer buttons ────────────────────────────────────────────────────────
    private JPanel buildFooter() {
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, UITheme.PAD_SM));
        footer.setBackground(UITheme.PARCHMENT);
        footer.setBorder(BorderFactory.createEmptyBorder(
                UITheme.PAD_SM, UITheme.PAD_MD, UITheme.PAD_MD, UITheme.PAD_MD));

        JButton btnRefresh = UITheme.ghostButton("🔄  Refresh");
        JButton btnDelete  = UITheme.dangerButton("🗑  Delete Selected User");

        footer.add(btnRefresh);
        footer.add(btnDelete);

        btnRefresh.addActionListener(e -> loadUsers());
        btnDelete.addActionListener(e -> deleteSelectedUser());
        return footer;
    }

    // =========================================================================
    // LOGIC
    // =========================================================================

    private void loadUsers() {
        tableModel.setRowCount(0);
        for (User u : userDAO.getAllUsers())
            tableModel.addRow(new Object[]{
                u.getId(), u.getFullname(), u.getUsername(), u.getRole()});
    }

    private void deleteSelectedUser() {
        int row = userTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this,
                    "Please select a user to delete.",
                    "No Selection", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        int    id   = (int)    tableModel.getValueAt(row, 0);
        String un   = (String) tableModel.getValueAt(row, 2);
        String role = (String) tableModel.getValueAt(row, 3);

        if ("Admin".equals(role)) {
            JOptionPane.showMessageDialog(this,
                    "Admin accounts cannot be deleted.",
                    "Action Denied", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (JOptionPane.showConfirmDialog(this,
                "Delete user '" + un + "'? This cannot be undone.",
                "Confirm Delete", JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE) == JOptionPane.YES_OPTION) {
            if (userDAO.deleteUser(id)) {
                JOptionPane.showMessageDialog(this,
                        "User '" + un + "' deleted.",
                        "Done", JOptionPane.INFORMATION_MESSAGE);
                loadUsers();
            } else {
                JOptionPane.showMessageDialog(this,
                        "Could not delete user. Please try again.",
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}