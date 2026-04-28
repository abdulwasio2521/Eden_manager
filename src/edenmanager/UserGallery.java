package edenmanager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;

/**
 * UserGallery.java  —  v3
 *
 * ROOT layout: BorderLayout
 *   NORTH  — Gradient navbar (58 px fixed)
 *   CENTER — BorderLayout wrapper
 *     NORTH  — Filter / search bar  (FlowLayout, always visible)
 *     CENTER — JScrollPane containing the WrapLayout gallery panel
 *
 * Key fixes vs v2
 * ───────────────
 * • setExtendedState(MAXIMIZED_BOTH) — opens maximised.
 * • Gallery JScrollPane viewport background = PARCHMENT (no white flash).
 * • WrapLayout is the custom inner class — cards reflow on every resize.
 * • Plant cards use opaque rounded JPanel with paintComponent override
 *   so rounded corners are visible on any L&F.
 * • Navbar logout button is a plain opaque JButton (always visible).
 */
public class UserGallery extends JFrame {

    private final User     currentUser;
    private final PlantDAO plantDAO = new PlantDAO();

    private JPanel            galleryPanel;
    private JTextField        tfSearch;
    private JComboBox<String> cbCategory;
    private JLabel            lblCount;

    // =========================================================================
    // CONSTRUCTOR
    // =========================================================================

    public UserGallery(User user) {
        this.currentUser = user;
        initUI();
        loadGallery(plantDAO.getAllPlants());
    }

    // =========================================================================
    // UI CONSTRUCTION
    // =========================================================================

    private void initUI() {
        setTitle("Eden Manager — Plant Gallery");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(640, 440));
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setLayout(new BorderLayout());
        getContentPane().setBackground(UITheme.PARCHMENT);

        add(buildNavBar(),        BorderLayout.NORTH);
        add(buildGalleryWrapper(), BorderLayout.CENTER);
    }

    // ── Navbar ────────────────────────────────────────────────────────────────
    private JPanel buildNavBar() {
        JPanel bar = UITheme.buildNavBar("🌿  Eden Plant Gallery");

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        right.setOpaque(false);
        right.add(UITheme.label("👤 " + currentUser.getFullname(),
                UITheme.FONT_BODY, Color.WHITE));

        JButton btnLogout = new JButton("Logout");
        btnLogout.setFont(UITheme.FONT_LABEL);
        btnLogout.setForeground(UITheme.INK);
        btnLogout.setBackground(UITheme.SAGE_LIGHT);
        btnLogout.setOpaque(true);
        btnLogout.setContentAreaFilled(true);
        btnLogout.setBorderPainted(false);
        btnLogout.setFocusPainted(false);
        btnLogout.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnLogout.setBorder(BorderFactory.createEmptyBorder(6, 14, 6, 14));
        btnLogout.addActionListener(e -> logout());
        right.add(btnLogout);

        bar.add(right, BorderLayout.EAST);
        return bar;
    }

    // ── Gallery wrapper ───────────────────────────────────────────────────────
    private JPanel buildGalleryWrapper() {
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(UITheme.PARCHMENT);
        wrapper.add(buildFilterBar(),    BorderLayout.NORTH);
        wrapper.add(buildGalleryScroll(), BorderLayout.CENTER);
        return wrapper;
    }

    // ── Filter / search bar ───────────────────────────────────────────────────
    private JPanel buildFilterBar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        bar.setBackground(UITheme.CREAM);
        bar.setBorder(BorderFactory.createMatteBorder(
                0, 0, 1, 0, UITheme.CREAM_BORDER));

        tfSearch = UITheme.styledTextField(16);
        tfSearch.setPreferredSize(new Dimension(200, 34));

        JButton btnSearch = UITheme.primaryButton("Search");
        btnSearch.setPreferredSize(new Dimension(86, 34));

        cbCategory = UITheme.styledComboBox();
        cbCategory.setPreferredSize(new Dimension(138, 34));
        refreshCategoryFilter();

        JButton btnFilter = UITheme.ghostButton("Filter");
        btnFilter.setPreferredSize(new Dimension(72, 34));

        JButton btnAll = UITheme.linkButton("Show All");

        lblCount = UITheme.label("", UITheme.FONT_SMALL, UITheme.INK_LIGHT);

        bar.add(UITheme.label("Search:", UITheme.FONT_LABEL, UITheme.INK_LIGHT));
        bar.add(tfSearch);
        bar.add(btnSearch);
        bar.add(UITheme.label("  Category:", UITheme.FONT_LABEL, UITheme.INK_LIGHT));
        bar.add(cbCategory);
        bar.add(btnFilter);
        bar.add(btnAll);
        bar.add(lblCount);

        btnSearch.addActionListener(e -> performSearch());
        tfSearch.addActionListener(e -> performSearch());
        btnFilter.addActionListener(e -> performFilter());
        btnAll.addActionListener(e -> {
            tfSearch.setText("");
            cbCategory.setSelectedIndex(0);
            loadGallery(plantDAO.getAllPlants());
        });
        return bar;
    }

    // ── Gallery scroll area ───────────────────────────────────────────────────
    private JScrollPane buildGalleryScroll() {
        galleryPanel = new JPanel(new WrapLayout(FlowLayout.LEFT, 18, 18));
        galleryPanel.setBackground(UITheme.PARCHMENT);
        galleryPanel.setBorder(BorderFactory.createEmptyBorder(
                UITheme.PAD_MD, UITheme.PAD_MD, UITheme.PAD_MD, UITheme.PAD_MD));

        JScrollPane sp = new JScrollPane(galleryPanel);
        sp.setBorder(null);
        sp.setBackground(UITheme.PARCHMENT);
        sp.getViewport().setBackground(UITheme.PARCHMENT);
        sp.getVerticalScrollBar().setUnitIncrement(18);
        sp.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        return sp;
    }

    // =========================================================================
    // GALLERY RENDERING
    // =========================================================================

    private void loadGallery(List<Plant> plants) {
        galleryPanel.removeAll();
        galleryPanel.setLayout(new WrapLayout(FlowLayout.LEFT, 18, 18));

        if (plants.isEmpty()) {
            JPanel empty = new JPanel(new GridBagLayout());
            empty.setBackground(UITheme.PARCHMENT);
            empty.add(UITheme.label("No plants found 🌾",
                    UITheme.FONT_HEADING, UITheme.INK_LIGHT));
            galleryPanel.setLayout(new BorderLayout());
            galleryPanel.add(empty, BorderLayout.CENTER);
        } else {
            for (Plant p : plants) galleryPanel.add(buildCard(p));
        }

        int n = plants.size();
        lblCount.setText(n + " plant" + (n != 1 ? "s" : ""));
        galleryPanel.revalidate();
        galleryPanel.repaint();
    }

    // ── Single plant card ─────────────────────────────────────────────────────
    private JPanel buildCard(Plant plant) {
        final int W = UITheme.THUMBNAIL_SIZE + 24;
        final int H = UITheme.THUMBNAIL_SIZE + 58;

        JPanel card = new JPanel(new BorderLayout(0, 6)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(),
                        UITheme.RADIUS_LARGE, UITheme.RADIUS_LARGE);
                g2.dispose();
                // Don't call super — we painted the bg ourselves
            }
        };
        card.setOpaque(false);
        card.setBackground(UITheme.CREAM);
        card.setPreferredSize(new Dimension(W, H));
        card.setBorder(BorderFactory.createCompoundBorder(
                new UITheme.RoundedBorder(UITheme.CREAM_BORDER, UITheme.RADIUS_LARGE, 1f),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)));
        card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        // Thumbnail button
        JButton imgBtn = new JButton(UITheme.bytesToIcon(
                plant.getImage(), UITheme.THUMBNAIL_SIZE, UITheme.THUMBNAIL_SIZE));
        imgBtn.setContentAreaFilled(false);
        imgBtn.setBorderPainted(false);
        imgBtn.setFocusPainted(false);
        imgBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        imgBtn.addActionListener(e -> new DetailFrame(plant.getId()).setVisible(true));

        // Labels
        JPanel labels = new JPanel(new GridLayout(2, 1, 0, 2));
        labels.setOpaque(false);
        JLabel nameLbl = UITheme.label(plant.getName(), UITheme.FONT_LABEL, UITheme.INK);
        nameLbl.setHorizontalAlignment(SwingConstants.CENTER);
        JLabel catLbl  = UITheme.label(plant.getCategory(), UITheme.FONT_SMALL, UITheme.INK_LIGHT);
        catLbl.setHorizontalAlignment(SwingConstants.CENTER);
        labels.add(nameLbl);
        labels.add(catLbl);

        card.add(imgBtn, BorderLayout.CENTER);
        card.add(labels, BorderLayout.SOUTH);

        // Hover highlight
        MouseAdapter ma = new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) {
                card.setBackground(new Color(0xE8F5EE));
                card.setBorder(BorderFactory.createCompoundBorder(
                        new UITheme.RoundedBorder(UITheme.SAGE, UITheme.RADIUS_LARGE, 2f),
                        BorderFactory.createEmptyBorder(10, 10, 10, 10)));
                card.repaint();
            }
            @Override public void mouseExited(MouseEvent e) {
                card.setBackground(UITheme.CREAM);
                card.setBorder(BorderFactory.createCompoundBorder(
                        new UITheme.RoundedBorder(UITheme.CREAM_BORDER, UITheme.RADIUS_LARGE, 1f),
                        BorderFactory.createEmptyBorder(10, 10, 10, 10)));
                card.repaint();
            }
            @Override public void mouseClicked(MouseEvent e) {
                new DetailFrame(plant.getId()).setVisible(true);
            }
        };
        card.addMouseListener(ma);
        imgBtn.addMouseListener(ma);

        return card;
    }

    // =========================================================================
    // LOGIC
    // =========================================================================

    private void performSearch() {
        String kw = tfSearch.getText().trim();
        loadGallery(kw.isEmpty()
                ? plantDAO.getAllPlants()
                : plantDAO.searchPlants(kw));
    }

    private void performFilter() {
        String cat = (String) cbCategory.getSelectedItem();
        loadGallery((cat == null || "All".equals(cat))
                ? plantDAO.getAllPlants()
                : plantDAO.filterByCategory(cat));
    }

    private void refreshCategoryFilter() {
        cbCategory.removeAllItems();
        for (String c : plantDAO.getAllCategories()) cbCategory.addItem(c);
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
    // INNER CLASS: WrapLayout  (responsive wrapping FlowLayout)
    // =========================================================================

    private static class WrapLayout extends FlowLayout {
        WrapLayout(int align, int hgap, int vgap) { super(align, hgap, vgap); }

        @Override
        public Dimension preferredLayoutSize(Container t) { return layout(t, true); }

        @Override
        public Dimension minimumLayoutSize(Container t) {
            Dimension d = layout(t, false);
            d.width -= (getHgap() + 1);
            return d;
        }

        private Dimension layout(Container target, boolean preferred) {
            synchronized (target.getTreeLock()) {
                int tw = target.getSize().width;
                if (tw == 0) tw = Integer.MAX_VALUE;
                Insets ins = target.getInsets();
                int maxW = tw - ins.left - ins.right - getHgap() * 2;
                int rW = 0, rH = 0;
                Dimension dim = new Dimension(0, 0);
                for (int i = 0; i < target.getComponentCount(); i++) {
                    Component m = target.getComponent(i);
                    if (!m.isVisible()) continue;
                    Dimension d = preferred ? m.getPreferredSize() : m.getMinimumSize();
                    if (rW + d.width > maxW) {
                        dim.height += rH + getVgap();
                        rW = 0; rH = 0;
                    }
                    rW += d.width + getHgap();
                    rH  = Math.max(rH, d.height);
                }
                dim.height += rH + getVgap();
                dim.width   = maxW;
                return dim;
            }
        }
    }
}