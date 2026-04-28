package edenmanager;

import javax.swing.*;
import java.awt.*;

/**
 * DetailFrame.java  —  v3
 *
 * ROOT layout: BorderLayout
 *   WEST   — Fixed-width (DETAIL_IMG_SIZE + 48px) GradientPanel with large image
 *   CENTER — JScrollPane  ›  GridBagLayout detail panel
 *
 * Key fixes vs v2
 * ───────────────
 * • setExtendedState(MAXIMIZED_BOTH) — no buttons get cut off.
 * • WEST panel has preferred width; CENTER gets all remaining space.
 * • Detail panel: gc.weighty = 1.0 on the spacer row so the close
 *   button is always pushed to the bottom and remains visible.
 * • JScrollPane on the detail panel means nothing clips even on small screens.
 */
public class DetailFrame extends JFrame {

    private final int      plantId;
    private final PlantDAO plantDAO = new PlantDAO();

    public DetailFrame(int plantId) {
        this.plantId = plantId;
        initUI();
    }

    private void initUI() {
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setMinimumSize(new Dimension(600, 400));
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        getContentPane().setBackground(UITheme.PARCHMENT);
        setLayout(new BorderLayout());

        Plant plant = plantDAO.getPlantById(plantId);
        if (plant == null) {
            setTitle("Plant Not Found");
            JLabel msg = UITheme.label("Plant not found.",
                    UITheme.FONT_HEADING, UITheme.INK_LIGHT);
            msg.setHorizontalAlignment(SwingConstants.CENTER);
            add(msg, BorderLayout.CENTER);
            return;
        }

        setTitle("🌿  " + plant.getName() + "  —  Eden Manager");
        add(buildImagePanel(plant), BorderLayout.WEST);
        add(buildDetailScroll(plant), BorderLayout.CENTER);
    }

    // ── Left: large image on a gradient background ────────────────────────────
    private JPanel buildImagePanel(Plant plant) {
        int imgW = UITheme.DETAIL_IMG_SIZE;
        int panelW = imgW + 60;

        UITheme.GradientPanel panel = new UITheme.GradientPanel(
                new GridBagLayout(), UITheme.CANOPY, UITheme.FERN);
        panel.setPreferredSize(new Dimension(panelW, 0));
        panel.setBorder(BorderFactory.createEmptyBorder(
                UITheme.PAD_LG, UITheme.PAD_LG, UITheme.PAD_LG, UITheme.PAD_LG));

        JLabel imgLbl = new JLabel(UITheme.bytesToIcon(
                plant.getImage(), imgW, imgW));
        imgLbl.setHorizontalAlignment(SwingConstants.CENTER);
        imgLbl.setBorder(new UITheme.RoundedBorder(
                new Color(255, 255, 255, 55), UITheme.RADIUS_LARGE, 2f));

        panel.add(imgLbl);
        return panel;
    }

    // ── Right: scrollable detail panel ───────────────────────────────────────
    private JScrollPane buildDetailScroll(Plant plant) {
        JPanel detail = new JPanel(new GridBagLayout());
        detail.setBackground(UITheme.PARCHMENT);
        detail.setBorder(BorderFactory.createEmptyBorder(
                UITheme.PAD_XL, UITheme.PAD_XL, UITheme.PAD_XL, UITheme.PAD_XL));

        GridBagConstraints gc = new GridBagConstraints();
        gc.gridx   = 0;
        gc.fill    = GridBagConstraints.HORIZONTAL;
        gc.weightx = 1.0;
        gc.anchor  = GridBagConstraints.NORTHWEST;
        int row    = 0;

        // Category pill
        gc.gridy  = row++;
        gc.insets = new Insets(0, 0, UITheme.PAD_SM, 0);
        JLabel chip = new JLabel("  " + plant.getCategory() + "  ");
        chip.setFont(UITheme.FONT_SMALL);
        chip.setForeground(UITheme.FERN);
        chip.setBackground(UITheme.SAGE_LIGHT);
        chip.setOpaque(true);
        chip.setBorder(BorderFactory.createCompoundBorder(
                new UITheme.RoundedBorder(UITheme.SAGE, UITheme.RADIUS, 1f),
                BorderFactory.createEmptyBorder(3, 8, 3, 8)));
        detail.add(chip, gc);

        // Plant name
        gc.gridy  = row++;
        gc.insets = new Insets(0, 0, 4, 0);
        detail.add(UITheme.label(plant.getName(),
                UITheme.FONT_TITLE, UITheme.CANOPY), gc);

        // Species
        gc.gridy  = row++;
        gc.insets = new Insets(0, 2, UITheme.PAD_MD, 0);
        JLabel specLbl = new JLabel(plant.getSpecies());
        specLbl.setFont(new Font("Georgia", Font.ITALIC, 13));
        specLbl.setForeground(UITheme.INK_LIGHT);
        detail.add(specLbl, gc);

        // Divider
        gc.gridy  = row++;
        gc.insets = new Insets(0, 0, UITheme.PAD_MD, 0);
        JSeparator sep = new JSeparator();
        sep.setForeground(UITheme.CREAM_BORDER);
        detail.add(sep, gc);

        // About heading
        gc.gridy  = row++;
        gc.insets = new Insets(0, 0, UITheme.PAD_SM, 0);
        detail.add(UITheme.label("About this plant",
                UITheme.FONT_SUBHEAD, UITheme.INK), gc);

        // Description
        String descText = (plant.getDescription() != null
                && !plant.getDescription().isEmpty())
                ? plant.getDescription()
                : "No description available for this plant.";
        gc.gridy  = row++;
        gc.insets = new Insets(0, 0, UITheme.PAD_LG, 0);
        JTextArea ta = new JTextArea(descText);
        ta.setFont(UITheme.FONT_BODY);
        ta.setForeground(UITheme.INK_LIGHT);
        ta.setBackground(UITheme.PARCHMENT);
        ta.setEditable(false);
        ta.setLineWrap(true);
        ta.setWrapStyleWord(true);
        ta.setBorder(null);
        detail.add(ta, gc);

        // Vertical spacer — pushes footer to bottom
        gc.gridy   = row++;
        gc.weighty = 1.0;
        gc.fill    = GridBagConstraints.BOTH;
        gc.insets  = new Insets(0, 0, 0, 0);
        detail.add(new JPanel() {{ setOpaque(false); }}, gc);

        // Footer row: ID label + Close button
        gc.gridy   = row++;
        gc.weighty = 0;
        gc.fill    = GridBagConstraints.HORIZONTAL;
        gc.insets  = new Insets(UITheme.PAD_SM, 0, 0, 0);
        JPanel footer = new JPanel(new BorderLayout());
        footer.setOpaque(false);
        footer.add(UITheme.label("Plant ID  #" + plant.getId(),
                UITheme.FONT_SMALL, UITheme.INK_LIGHT), BorderLayout.WEST);
        JButton btnClose = UITheme.primaryButton("Close");
        btnClose.addActionListener(e -> dispose());
        footer.add(btnClose, BorderLayout.EAST);
        detail.add(footer, gc);

        JScrollPane sp = new JScrollPane(detail);
        sp.setBorder(null);
        sp.setBackground(UITheme.PARCHMENT);
        sp.getViewport().setBackground(UITheme.PARCHMENT);
        sp.getVerticalScrollBar().setUnitIncrement(16);
        sp.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        return sp;
    }
}