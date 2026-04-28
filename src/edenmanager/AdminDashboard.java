package edenmanager;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.nio.file.Files;
import java.util.List;

/**
 * AdminDashboard.java  —  v3
 *
 * ROOT layout: BorderLayout
 *   NORTH  — Gradient navbar  (fixed 58 px, always visible)
 *   CENTER — JSplitPane (HORIZONTAL)
 *     LEFT  — Table panel   (BorderLayout: search bar / table / action row)
 *     RIGHT — JScrollPane  ›  form card (GridBagLayout)
 *
 * Key fixes vs v2
 * ───────────────
 * • setExtendedState(MAXIMIZED_BOTH) — opens maximised by default.
 * • JSplitPane proportional resize weight = 0.55 so both sides grow.
 * • Form is inside a JScrollPane so it is always reachable at any height.
 * • Every form field row: gc.fill = HORIZONTAL + gc.weightx = 1.0 → stretches.
 * • Table JScrollPane uses a proper CREAM viewport background.
 * • Navbar buttons use opaque JButton with background colour (no custom paint
 *   needed) so they render correctly on all L&Fs.
 */
public class AdminDashboard extends JFrame {

    private final User     currentUser;
    private final PlantDAO plantDAO = new PlantDAO();
    private final UserDAO  userDAO  = new UserDAO();

    // ── Table state ───────────────────────────────────────────────────────────
    private JTable            plantTable;
    private DefaultTableModel tableModel;
    private JTextField        tfSearch;
    private JComboBox<String> cbCategory;

    // ── Form state ────────────────────────────────────────────────────────────
    private JLabel     lblFormTitle;
    private JTextField tfName, tfSpecies, tfCategory;
    private JTextArea  taDescription;
    private JLabel     lblImagePreview;
    private byte[]     selectedImageBytes = null;
    private int        editingPlantId     = -1;

    // =========================================================================
    // CONSTRUCTOR
    // =========================================================================

    public AdminDashboard(User user) {
        this.currentUser = user;
        initUI();
        loadPlantTable(plantDAO.getAllPlants());
    }

    // =========================================================================
    // UI CONSTRUCTION
    // =========================================================================

    private void initUI() {
        setTitle("Eden Manager — Admin Dashboard");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(860, 540));
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setLayout(new BorderLayout());
        getContentPane().setBackground(UITheme.PARCHMENT);

        add(buildNavBar(),    BorderLayout.NORTH);
        add(buildSplitPane(), BorderLayout.CENTER);
    }

    // ── Navbar ────────────────────────────────────────────────────────────────
    private JPanel buildNavBar() {
        JPanel bar = UITheme.buildNavBar("🌿  Eden Manager — Admin");

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        right.setOpaque(false);
        right.add(UITheme.label("👤 " + currentUser.getFullname(),
                UITheme.FONT_BODY, Color.WHITE));
        right.add(navBtn("📋 Report", e -> openReportWindow()));
        right.add(navBtn("👥 Users",  e -> openUserManagement()));
        right.add(navBtn("Logout",    e -> logout()));
        bar.add(right, BorderLayout.EAST);
        return bar;
    }

    /** Small pill-shaped navbar button (fully opaque, no custom paint needed). */
    private JButton navBtn(String text, ActionListener al) {
        JButton b = new JButton(text);
        b.setFont(UITheme.FONT_LABEL);
        b.setForeground(UITheme.INK);
        b.setBackground(UITheme.SAGE_LIGHT);
        b.setOpaque(true);
        b.setContentAreaFilled(true);
        b.setBorderPainted(false);
        b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setBorder(BorderFactory.createEmptyBorder(6, 14, 6, 14));
        b.addActionListener(al);
        return b;
    }

    // ── Split pane ────────────────────────────────────────────────────────────
    private JSplitPane buildSplitPane() {
        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                buildTableSide(), buildFormSide());
        split.setDividerLocation(0.55);   // proportional — set after pack/show
        split.setResizeWeight(0.55);      // both sides grow on resize
        split.setDividerSize(6);
        split.setBorder(null);
        split.setBackground(UITheme.PARCHMENT);
        // Proportional divider works properly after the frame is visible:
        split.addComponentListener(new ComponentAdapter() {
            private boolean done = false;
            @Override public void componentResized(ComponentEvent e) {
                if (!done) { split.setDividerLocation(0.55); done = true; }
            }
        });
        return split;
    }

    // ── LEFT: table panel ─────────────────────────────────────────────────────
    private JPanel buildTableSide() {
        JPanel panel = new JPanel(new BorderLayout(0, 8));
        panel.setBackground(UITheme.PARCHMENT);
        panel.setBorder(BorderFactory.createEmptyBorder(
                UITheme.PAD_MD, UITheme.PAD_MD, UITheme.PAD_MD, UITheme.PAD_SM));

        panel.add(buildSearchBar(),    BorderLayout.NORTH);
        panel.add(buildTableScroll(),  BorderLayout.CENTER);
        panel.add(buildTableActions(), BorderLayout.SOUTH);
        return panel;
    }

    private JPanel buildSearchBar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 6));
        bar.setBackground(UITheme.CREAM);
        bar.setBorder(BorderFactory.createCompoundBorder(
                new UITheme.RoundedBorder(UITheme.CREAM_BORDER, UITheme.RADIUS, 1f),
                BorderFactory.createEmptyBorder(4, 8, 4, 8)));

        tfSearch = UITheme.styledTextField(14);
        tfSearch.setPreferredSize(new Dimension(160, 34));

        JButton btnSearch = UITheme.primaryButton("Search");
        btnSearch.setPreferredSize(new Dimension(88, 34));

        cbCategory = UITheme.styledComboBox();
        cbCategory.setPreferredSize(new Dimension(130, 34));
        refreshCategoryFilter();

        JButton btnFilter = UITheme.ghostButton("Filter");
        btnFilter.setPreferredSize(new Dimension(74, 34));

        JButton btnClear = UITheme.linkButton("Clear");

        bar.add(UITheme.label("Search:", UITheme.FONT_LABEL, UITheme.INK_LIGHT));
        bar.add(tfSearch);
        bar.add(btnSearch);
        bar.add(UITheme.label("  Category:", UITheme.FONT_LABEL, UITheme.INK_LIGHT));
        bar.add(cbCategory);
        bar.add(btnFilter);
        bar.add(btnClear);

        btnSearch.addActionListener(e -> performSearch());
        tfSearch.addActionListener(e -> performSearch());
        btnFilter.addActionListener(e -> performFilter());
        btnClear.addActionListener(e -> {
            tfSearch.setText("");
            cbCategory.setSelectedIndex(0);
            loadPlantTable(plantDAO.getAllPlants());
        });
        return bar;
    }

    private JScrollPane buildTableScroll() {
        String[] cols = {"ID", "Name", "Species", "Category"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        plantTable = new JTable(tableModel);
        plantTable.setFont(UITheme.FONT_BODY);
        plantTable.setRowHeight(32);
        plantTable.setShowGrid(false);
        plantTable.setIntercellSpacing(new Dimension(0, 0));
        plantTable.setBackground(UITheme.CREAM);
        plantTable.setSelectionBackground(UITheme.SAGE_LIGHT);
        plantTable.setSelectionForeground(UITheme.INK);
        plantTable.getTableHeader().setFont(UITheme.FONT_LABEL);
        plantTable.getTableHeader().setBackground(UITheme.PARCHMENT);
        plantTable.getTableHeader().setForeground(UITheme.INK_LIGHT);
        plantTable.getTableHeader().setBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, UITheme.CREAM_BORDER));

        // Alternating rows
        plantTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v,
                    boolean sel, boolean foc, int row, int col) {
                Component c = super.getTableCellRendererComponent(t,v,sel,foc,row,col);
                if (!sel) c.setBackground(row % 2 == 0 ? UITheme.CREAM : UITheme.PARCHMENT);
                setFont(UITheme.FONT_BODY);
                setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
                return c;
            }
        });

        // Hide ID column (used internally only)
        plantTable.getColumnModel().getColumn(0).setMinWidth(0);
        plantTable.getColumnModel().getColumn(0).setMaxWidth(0);
        plantTable.getColumnModel().getColumn(0).setWidth(0);

        JScrollPane sp = new JScrollPane(plantTable);
        sp.setBorder(new UITheme.RoundedBorder(UITheme.CREAM_BORDER, UITheme.RADIUS, 1f));
        sp.getViewport().setBackground(UITheme.CREAM);
        sp.getVerticalScrollBar().setUnitIncrement(16);
        return sp;
    }

    private JPanel buildTableActions() {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        row.setBackground(UITheme.PARCHMENT);
        JButton btnEdit   = UITheme.primaryButton("✏  Edit Selected");
        JButton btnDelete = UITheme.dangerButton("🗑  Delete");
        row.add(btnEdit);
        row.add(btnDelete);
        btnEdit.addActionListener(e -> loadSelectedForEdit());
        btnDelete.addActionListener(e -> deleteSelected());
        return row;
    }

    // ── RIGHT: form panel ─────────────────────────────────────────────────────
    private JScrollPane buildFormSide() {
        // A parchment wrapper centres the card and fills the scroll viewport.
        JPanel wrapper = new JPanel(new GridBagLayout());
        wrapper.setBackground(UITheme.PARCHMENT);
        wrapper.setBorder(BorderFactory.createEmptyBorder(
                UITheme.PAD_MD, UITheme.PAD_SM, UITheme.PAD_MD, UITheme.PAD_MD));

        GridBagConstraints wc = new GridBagConstraints();
        wc.fill    = GridBagConstraints.BOTH;
        wc.weightx = 1.0;
        wc.weighty = 1.0;
        wrapper.add(buildFormCard(), wc);

        JScrollPane sp = new JScrollPane(wrapper);
        sp.setBorder(null);
        sp.setBackground(UITheme.PARCHMENT);
        sp.getViewport().setBackground(UITheme.PARCHMENT);
        sp.getVerticalScrollBar().setUnitIncrement(16);
        sp.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        return sp;
    }

    private JPanel buildFormCard() {
        JPanel card = new JPanel(new GridBagLayout());
        card.setBackground(UITheme.CREAM);
        card.setBorder(BorderFactory.createCompoundBorder(
                new UITheme.RoundedBorder(UITheme.CREAM_BORDER, UITheme.RADIUS_LARGE, 1f),
                BorderFactory.createEmptyBorder(
                        UITheme.PAD_LG, UITheme.PAD_LG, UITheme.PAD_LG, UITheme.PAD_LG)));

        GridBagConstraints gc = new GridBagConstraints();
        gc.gridx   = 0;
        gc.fill    = GridBagConstraints.HORIZONTAL;
        gc.weightx = 1.0;
        int row    = 0;

        // Form title
        gc.gridy  = row++;
        gc.insets = new Insets(0, 0, UITheme.PAD_MD, 0);
        lblFormTitle = UITheme.label("Add New Plant", UITheme.FONT_HEADING, UITheme.FERN);
        card.add(lblFormTitle, gc);

        // Text fields
        row = formField(card, gc, row, "Plant Name *",
                tfName        = styledField());
        row = formField(card, gc, row, "Species *",
                tfSpecies     = styledField());
        row = formField(card, gc, row, "Category *",
                tfCategory    = styledField());

        // Description text area
        gc.gridy  = row++;
        gc.insets = new Insets(UITheme.PAD_SM, 0, 4, 0);
        card.add(formLabel("Description"), gc);

        gc.gridy  = row++;
        gc.insets = new Insets(0, 0, UITheme.PAD_SM, 0);
        taDescription = UITheme.styledTextArea(5, 0);
        JScrollPane descSp = new JScrollPane(taDescription);
        descSp.setBorder(new UITheme.RoundedBorder(UITheme.CREAM_BORDER, UITheme.RADIUS - 2, 1f));
        descSp.setPreferredSize(new Dimension(0, 110));
        card.add(descSp, gc);

        // Image preview
        gc.gridy  = row++;
        gc.insets = new Insets(UITheme.PAD_SM, 0, 4, 0);
        card.add(formLabel("Plant Image"), gc);

        gc.gridy  = row++;
        gc.insets = new Insets(0, 0, 6, 0);
        lblImagePreview = new JLabel(UITheme.bytesToIcon(null,
                UITheme.THUMBNAIL_SIZE, UITheme.THUMBNAIL_SIZE));
        lblImagePreview.setHorizontalAlignment(SwingConstants.CENTER);
        lblImagePreview.setBorder(new UITheme.RoundedBorder(
                UITheme.CREAM_BORDER, UITheme.RADIUS, 1f));
        lblImagePreview.setPreferredSize(
                new Dimension(UITheme.THUMBNAIL_SIZE, UITheme.THUMBNAIL_SIZE));
        card.add(lblImagePreview, gc);

        gc.gridy  = row++;
        gc.insets = new Insets(0, 0, UITheme.PAD_LG, 0);
        JButton btnImg = UITheme.ghostButton("📷  Choose Image…");
        card.add(btnImg, gc);

        // Action row
        gc.gridy  = row++;
        gc.insets = new Insets(0, 0, 0, 0);
        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        btnRow.setOpaque(false);
        JButton btnSave   = UITheme.primaryButton("💾  Save Plant");
        JButton btnCancel = UITheme.ghostButton("✕  Clear");
        btnRow.add(btnSave);
        btnRow.add(btnCancel);
        card.add(btnRow, gc);

        // Push remaining space down
        gc.gridy   = row++;
        gc.weighty = 1.0;
        gc.fill    = GridBagConstraints.BOTH;
        card.add(new JPanel() {{ setOpaque(false); }}, gc);

        btnImg.addActionListener(e    -> chooseImage());
        btnSave.addActionListener(e   -> savePlant());
        btnCancel.addActionListener(e -> clearForm());

        return card;
    }

    // =========================================================================
    // LOGIC
    // =========================================================================

    private void loadPlantTable(List<Plant> plants) {
        tableModel.setRowCount(0);
        for (Plant p : plants)
            tableModel.addRow(new Object[]{
                p.getId(), p.getName(), p.getSpecies(), p.getCategory()});
    }

    private void refreshCategoryFilter() {
        cbCategory.removeAllItems();
        for (String c : plantDAO.getAllCategories()) cbCategory.addItem(c);
    }

    private void performSearch() {
        String kw = tfSearch.getText().trim();
        loadPlantTable(kw.isEmpty()
                ? plantDAO.getAllPlants()
                : plantDAO.searchPlants(kw));
    }

    private void performFilter() {
        String cat = (String) cbCategory.getSelectedItem();
        loadPlantTable((cat == null || "All".equals(cat))
                ? plantDAO.getAllPlants()
                : plantDAO.filterByCategory(cat));
    }

    private void chooseImage() {
        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Select Plant Image");
        fc.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
                "Images (*.jpg, *.jpeg, *.png)", "jpg", "jpeg", "png"));
        if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                selectedImageBytes = Files.readAllBytes(fc.getSelectedFile().toPath());
                lblImagePreview.setIcon(UITheme.scaleIcon(
                        new ImageIcon(selectedImageBytes),
                        UITheme.THUMBNAIL_SIZE, UITheme.THUMBNAIL_SIZE));
                lblImagePreview.setText("");
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this,
                        "Could not read image: " + ex.getMessage(),
                        "Image Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void savePlant() {
        String name = tfName.getText().trim();
        String sp   = tfSpecies.getText().trim();
        String cat  = tfCategory.getText().trim();
        String desc = taDescription.getText().trim();

        if (name.isEmpty() || sp.isEmpty() || cat.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Name, Species, and Category are required.",
                    "Validation", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (editingPlantId == -1 && selectedImageBytes == null) {
            JOptionPane.showMessageDialog(this,
                    "Please choose a plant image.",
                    "Validation", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Plant p = new Plant();
        p.setName(name); p.setSpecies(sp);
        p.setCategory(cat); p.setDescription(desc);
        p.setImage(selectedImageBytes);

        boolean ok;
        if (editingPlantId == -1) {
            ok = plantDAO.addPlant(p);
        } else {
            p.setId(editingPlantId);
            ok = plantDAO.updatePlant(p);
        }

        if (ok) {
            JOptionPane.showMessageDialog(this,
                    "Plant '" + name + "' saved successfully! 🌿",
                    "Saved", JOptionPane.INFORMATION_MESSAGE);
            clearForm();
            loadPlantTable(plantDAO.getAllPlants());
            refreshCategoryFilter();
        } else {
            JOptionPane.showMessageDialog(this,
                    "Failed to save. Please try again.",
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadSelectedForEdit() {
        int row = plantTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Select a plant to edit.",
                    "No Selection", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        int id = (int) tableModel.getValueAt(row, 0);
        Plant p = plantDAO.getPlantById(id);
        if (p == null) return;

        editingPlantId = p.getId();
        lblFormTitle.setText("Editing: " + p.getName());
        tfName.setText(p.getName());
        tfSpecies.setText(p.getSpecies());
        tfCategory.setText(p.getCategory());
        taDescription.setText(p.getDescription());
        selectedImageBytes = null;
        if (p.getImage() != null)
            lblImagePreview.setIcon(UITheme.bytesToIcon(
                    p.getImage(), UITheme.THUMBNAIL_SIZE, UITheme.THUMBNAIL_SIZE));
    }

    private void deleteSelected() {
        int row = plantTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Select a plant to delete.",
                    "No Selection", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        String name = (String) tableModel.getValueAt(row, 1);
        int    id   = (int)    tableModel.getValueAt(row, 0);
        if (JOptionPane.showConfirmDialog(this,
                "Delete '" + name + "'? This cannot be undone.",
                "Confirm Delete", JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE) == JOptionPane.YES_OPTION) {
            if (plantDAO.deletePlant(id)) {
                JOptionPane.showMessageDialog(this,
                        "'" + name + "' deleted.", "Done",
                        JOptionPane.INFORMATION_MESSAGE);
                loadPlantTable(plantDAO.getAllPlants());
                refreshCategoryFilter();
                clearForm();
            }
        }
    }

    private void clearForm() {
        editingPlantId     = -1;
        selectedImageBytes = null;
        lblFormTitle.setText("Add New Plant");
        tfName.setText(""); tfSpecies.setText("");
        tfCategory.setText(""); taDescription.setText("");
        lblImagePreview.setIcon(UITheme.bytesToIcon(
                null, UITheme.THUMBNAIL_SIZE, UITheme.THUMBNAIL_SIZE));
        plantTable.clearSelection();
    }

    private void openReportWindow() {
        new ReportFrame(plantDAO.getAllPlantsNoImage(), userDAO.getAllUsers())
                .setVisible(true);
    }

    private void openUserManagement() {
        new UserManagementFrame(userDAO).setVisible(true);
    }

    private void logout() {
        if (JOptionPane.showConfirmDialog(this,
                "Are you sure you want to logout?", "Logout",
                JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            dispose();
            SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
        }
    }

    // =========================================================================
    // HELPERS
    // =========================================================================

    private static JTextField styledField() {
        JTextField tf = UITheme.styledTextField(0);
        tf.setPreferredSize(new Dimension(0, 40));
        return tf;
    }

    private static int formField(JPanel card, GridBagConstraints gc,
                                 int row, String label, JTextField field) {
        gc.gridy  = row++;
        gc.insets = new Insets(UITheme.PAD_SM, 0, 4, 0);
        gc.fill   = GridBagConstraints.HORIZONTAL;
        card.add(formLabel(label), gc);
        gc.gridy  = row++;
        gc.insets = new Insets(0, 0, 2, 0);
        card.add(field, gc);
        return row;
    }

    private static JLabel formLabel(String text) {
        JLabel l = UITheme.label(text, UITheme.FONT_LABEL, UITheme.INK_LIGHT);
        l.setBorder(BorderFactory.createEmptyBorder(0, 2, 0, 0));
        return l;
    }
}