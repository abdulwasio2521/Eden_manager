package edenmanager;

import javax.swing.*;
import javax.swing.border.AbstractBorder;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * UITheme.java
 * Central design system for Eden Manager v3.
 *
 * Palette  — "Botanical Manuscript":
 *   CANOPY      #1A3C2E   deep forest (navbar dark end)
 *   FERN        #2D6A4F   primary green
 *   MOSS        #40916C   hover / active green
 *   SAGE        #74C69D   light green accent
 *   SAGE_LIGHT  #B7E4C7   ghost-button hover fill
 *   PARCHMENT   #F8F4ED   warm off-white background
 *   CREAM       #FDFAF5   card surface
 *   CREAM_BORDER#E8E0D0   border / divider
 *   TERRACOTTA  #B5533C   danger / delete
 *   TERRA_LIGHT #D4735E   danger hover
 *   GOLD        #C8963E   warning / accent
 *   INK         #1C2B22   primary text
 *   INK_LIGHT   #4A5E52   muted / secondary text
 */
public class UITheme {

    // ── Raw palette ──────────────────────────────────────────────────────────
    public static final Color CANOPY        = new Color(0x1A3C2E);
    public static final Color FERN          = new Color(0x2D6A4F);
    public static final Color MOSS          = new Color(0x40916C);
    public static final Color SAGE          = new Color(0x74C69D);
    public static final Color SAGE_LIGHT    = new Color(0xB7E4C7);
    public static final Color PARCHMENT     = new Color(0xF8F4ED);
    public static final Color CREAM         = new Color(0xFDFAF5);
    public static final Color CREAM_BORDER  = new Color(0xE8E0D0);
    public static final Color TERRACOTTA    = new Color(0xB5533C);
    public static final Color TERRA_LIGHT   = new Color(0xD4735E);
    public static final Color GOLD          = new Color(0xC8963E);
    public static final Color INK           = new Color(0x1C2B22);
    public static final Color INK_LIGHT     = new Color(0x4A5E52);

    // ── Semantic / back-compat aliases ───────────────────────────────────────
    public static final Color BACKGROUND    = PARCHMENT;
    public static final Color SURFACE       = CREAM;
    public static final Color SURFACE_BORDER= CREAM_BORDER;
    public static final Color BORDER        = CREAM_BORDER;
    public static final Color TEXT_PRIMARY  = INK;
    public static final Color TEXT_SECONDARY= INK_LIGHT;
    public static final Color PRIMARY       = FERN;
    public static final Color PRIMARY_DARK  = CANOPY;
    public static final Color PRIMARY_LIGHT = MOSS;
    public static final Color DANGER        = TERRACOTTA;
    public static final Color DANGER_LIGHT  = TERRA_LIGHT;
    public static final Color SUCCESS       = new Color(0x2E7D32);
    public static final Color WARNING       = GOLD;
    public static final Color TEXT_ON_PRIMARY = Color.WHITE;

    // ── Typography ───────────────────────────────────────────────────────────
    public static final Font FONT_DISPLAY   = new Font("Georgia", Font.BOLD,  26);
    public static final Font FONT_TITLE     = new Font("Georgia", Font.BOLD,  20);
    public static final Font FONT_HEADING   = new Font("Georgia", Font.BOLD,  16);
    public static final Font FONT_SUBHEAD   = new Font("Tahoma",  Font.BOLD,  14);
    public static final Font FONT_BODY      = new Font("Tahoma",  Font.PLAIN, 13);
    public static final Font FONT_LABEL     = new Font("Tahoma",  Font.BOLD,  12);
    public static final Font FONT_SMALL     = new Font("Tahoma",  Font.PLAIN, 11);
    public static final Font FONT_BUTTON    = new Font("Tahoma",  Font.BOLD,  13);
    public static final Font FONT_CAPTION   = new Font("Georgia", Font.ITALIC,12);
    public static final Font FONT_MONOSPACE = new Font("Consolas",Font.PLAIN, 13);

    // ── Sizing ───────────────────────────────────────────────────────────────
    public static final int RADIUS          = 10;
    public static final int RADIUS_LARGE    = 16;
    public static final int PAD_XS          =  5;
    public static final int PAD_SM          =  9;
    public static final int PAD_MD          = 14;
    public static final int PAD_LG          = 22;
    public static final int PAD_XL          = 32;
    public static final int THUMBNAIL_SIZE  = 160;
    public static final int DETAIL_IMG_SIZE = 280;

    // =========================================================================
    // BUTTONS
    // =========================================================================

    /** Filled green primary button with rounded corners. */
    public static JButton primaryButton(String text) {
        return filledBtn(text, FERN, MOSS, Color.WHITE);
    }

    /** Filled terracotta danger button. */
    public static JButton dangerButton(String text) {
        return filledBtn(text, TERRACOTTA, TERRA_LIGHT, Color.WHITE);
    }

    /** Outlined ghost button — transparent fill, green border. */
    public static JButton ghostButton(String text) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                if (getModel().isRollover()) {
                    g2.setColor(new Color(183, 228, 199, 130));
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), RADIUS, RADIUS);
                }
                g2.setColor(FERN);
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawRoundRect(1, 1, getWidth() - 2, getHeight() - 2, RADIUS, RADIUS);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        applyBtnBase(btn, FERN, PARCHMENT);
        btn.setBorder(BorderFactory.createEmptyBorder(PAD_SM, PAD_MD, PAD_SM, PAD_MD));
        return btn;
    }

    /** Flat text-only link button. */
    public static JButton linkButton(String text) {
        JButton b = new JButton(text);
        b.setFont(FONT_SMALL);
        b.setForeground(FERN);
        b.setContentAreaFilled(false);
        b.setBorderPainted(false);
        b.setFocusPainted(false);
        b.setOpaque(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setBorder(BorderFactory.createEmptyBorder(2, 4, 2, 4));
        return b;
    }

    private static JButton filledBtn(String text, Color bg, Color hover, Color fg) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                Color fill = getModel().isPressed() ? bg.darker()
                           : getModel().isRollover() ? hover : bg;
                g2.setColor(fill);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), RADIUS, RADIUS);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        applyBtnBase(btn, fg, bg);
        btn.setBorder(BorderFactory.createEmptyBorder(PAD_SM + 1, PAD_LG, PAD_SM + 1, PAD_LG));
        return btn;
    }

    private static void applyBtnBase(JButton b, Color fg, Color bg) {
        b.setFont(FONT_BUTTON);
        b.setForeground(fg);
        b.setBackground(bg);
        b.setContentAreaFilled(false);
        b.setBorderPainted(false);
        b.setFocusPainted(false);
        b.setOpaque(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    // =========================================================================
    // INPUT FIELDS
    // =========================================================================

    public static JTextField styledTextField(int cols) {
        JTextField tf = new JTextField(cols);
        applyFieldStyle(tf);
        return tf;
    }

    public static JPasswordField styledPasswordField(int cols) {
        JPasswordField pf = new JPasswordField(cols);
        applyFieldStyle(pf);
        return pf;
    }

    public static JTextArea styledTextArea(int rows, int cols) {
        JTextArea ta = new JTextArea(rows, cols);
        ta.setFont(FONT_BODY);
        ta.setForeground(INK);
        ta.setBackground(CREAM);
        ta.setLineWrap(true);
        ta.setWrapStyleWord(true);
        ta.setCaretColor(FERN);
        ta.setBorder(BorderFactory.createCompoundBorder(
                new RoundedBorder(CREAM_BORDER, RADIUS - 2, 1f),
                BorderFactory.createEmptyBorder(PAD_SM, PAD_SM, PAD_SM, PAD_SM)));
        return ta;
    }

    private static void applyFieldStyle(JTextComponent tc) {
        tc.setFont(FONT_BODY);
        tc.setForeground(INK);
        tc.setBackground(CREAM);
        tc.setCaretColor(FERN);
        tc.setBorder(BorderFactory.createCompoundBorder(
                new RoundedBorder(CREAM_BORDER, RADIUS - 2, 1f),
                BorderFactory.createEmptyBorder(PAD_SM, PAD_SM, PAD_SM, PAD_SM)));
    }

    public static <T> JComboBox<T> styledComboBox() {
        JComboBox<T> cb = new JComboBox<>();
        cb.setFont(FONT_BODY);
        cb.setBackground(CREAM);
        cb.setForeground(INK);
        return cb;
    }

    // =========================================================================
    // LABELS
    // =========================================================================

    public static JLabel label(String text, Font font, Color color) {
        JLabel l = new JLabel(text);
        l.setFont(font);
        l.setForeground(color);
        return l;
    }

    // =========================================================================
    // NAVIGATION BAR
    // =========================================================================

    /**
     * Creates a gradient (CANOPY → FERN) navbar panel.
     * The title is placed in BorderLayout.WEST.
     * Caller adds right-side widgets to BorderLayout.EAST.
     */
    public static JPanel buildNavBar(String titleText) {
        JPanel bar = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setPaint(new GradientPaint(0, 0, CANOPY, getWidth(), 0, FERN));
                g2.fillRect(0, 0, getWidth(), getHeight());
                // subtle dot grid
                g2.setColor(new Color(255, 255, 255, 8));
                for (int x = 0; x < getWidth(); x += 28)
                    for (int y = 0; y < getHeight(); y += 28)
                        g2.fillOval(x, y, 2, 2);
                g2.dispose();
            }
        };
        bar.setOpaque(true);
        bar.setPreferredSize(new Dimension(0, 58));
        bar.setBorder(BorderFactory.createEmptyBorder(PAD_SM, PAD_LG, PAD_SM, PAD_LG));
        bar.add(label(titleText, FONT_HEADING, Color.WHITE), BorderLayout.WEST);
        return bar;
    }

    // =========================================================================
    // SCROLL PANE HELPER
    // =========================================================================

    public static JScrollPane scrollPane(JComponent content) {
        JScrollPane sp = new JScrollPane(content);
        sp.setBorder(null);
        sp.setBackground(PARCHMENT);
        sp.getViewport().setBackground(PARCHMENT);
        sp.getVerticalScrollBar().setUnitIncrement(16);
        sp.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        return sp;
    }

    // =========================================================================
    // IMAGE UTILITIES
    // =========================================================================

    public static ImageIcon scaleIcon(ImageIcon icon, int w, int h) {
        return new ImageIcon(icon.getImage().getScaledInstance(w, h, Image.SCALE_SMOOTH));
    }

    public static ImageIcon bytesToIcon(byte[] data, int w, int h) {
        if (data == null || data.length == 0) return placeholderIcon(w, h);
        try { return scaleIcon(new ImageIcon(data), w, h); }
        catch (Exception e) { return placeholderIcon(w, h); }
    }

    private static ImageIcon placeholderIcon(int w, int h) {
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(PARCHMENT);
        g.fillRoundRect(0, 0, w, h, RADIUS, RADIUS);
        g.setColor(CREAM_BORDER);
        g.setStroke(new BasicStroke(1.2f));
        g.drawRoundRect(1, 1, w - 2, h - 2, RADIUS, RADIUS);
        g.setColor(SAGE_LIGHT);
        int fs = Math.max(14, Math.min(w, h) / 4);
        g.setFont(new Font("Segoe UI Emoji", Font.PLAIN, fs));
        FontMetrics fm = g.getFontMetrics();
        String leaf = "🌿";
        g.drawString(leaf, (w - fm.stringWidth(leaf)) / 2, h / 2 + fm.getAscent() / 3);
        g.setColor(INK_LIGHT);
        g.setFont(FONT_SMALL);
        fm = g.getFontMetrics();
        String txt = "No Image";
        g.drawString(txt, (w - fm.stringWidth(txt)) / 2, h - PAD_MD);
        g.dispose();
        return new ImageIcon(img);
    }

    // =========================================================================
    // LOOK & FEEL
    // =========================================================================

    public static void applyLookAndFeel() {
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    UIManager.put("nimbusBase",            FERN);
                    UIManager.put("nimbusBlueGrey",        new Color(0x8A9E8E));
                    UIManager.put("control",               PARCHMENT);
                    UIManager.put("text",                  INK);
                    UIManager.put("nimbusLightBackground", CREAM);
                    UIManager.put("nimbusFocus",           SAGE);
                    return;
                }
            }
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}
    }

    // =========================================================================
    // INNER CLASS: RoundedBorder
    // =========================================================================

    /** Draws an anti-aliased rounded rectangle border. */
    public static class RoundedBorder extends AbstractBorder {
        private final Color color;
        private final int   radius;
        private final float thickness;

        public RoundedBorder(Color color, int radius, float thickness) {
            this.color     = color;
            this.radius    = radius;
            this.thickness = thickness;
        }

        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color);
            g2.setStroke(new BasicStroke(thickness));
            g2.drawRoundRect(x, y, w - 1, h - 1, radius, radius);
            g2.dispose();
        }

        @Override
        public Insets getBorderInsets(Component c) {
            int i = (int)(thickness + 3);
            return new Insets(i, i, i, i);
        }

        @Override public boolean isBorderOpaque() { return false; }
    }

    // =========================================================================
    // INNER CLASS: GradientPanel
    // =========================================================================

    /** JPanel that paints a vertical gradient — used for left decorative panels. */
    public static class GradientPanel extends JPanel {
        private final Color top, bottom;

        public GradientPanel(LayoutManager layout, Color top, Color bottom) {
            super(layout);
            this.top    = top;
            this.bottom = bottom;
            setOpaque(true);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setPaint(new GradientPaint(0, 0, top, 0, getHeight(), bottom));
            g2.fillRect(0, 0, getWidth(), getHeight());
            g2.dispose();
        }
    }
}