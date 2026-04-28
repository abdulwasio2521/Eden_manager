package edenmanager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;

/**
 * LoginFrame.java  —  v3
 *
 * ROOT layout: BorderLayout
 *   WEST   — Fixed-width (280px) GradientPanel with brand art.
 *             Uses a BoxLayout column, everything centred vertically.
 *   CENTER — Parchment panel (BorderLayout) that centres the form card.
 *             The card uses GridBagLayout so every row stretches horizontally.
 *
 * Key fixes vs v2
 * ───────────────
 * • setExtendedState(MAXIMIZED_BOTH) — opens full-screen by default.
 * • setMinimumSize still prevents collapsing below 600 x 450.
 * • The form card is placed in a BorderLayout CENTER slot of a parchment
 *   wrapper — this lets it grow in BOTH axes when the window expands.
 * • gc.weightx = 1.0 + gc.fill = HORIZONTAL on every field row ensures
 *   fields stretch edge-to-edge inside the card.
 * • No outer JScrollPane around the login form (it is short enough).
 */
public class LoginFrame extends JFrame {

    private JTextField     tfUsername;
    private JPasswordField pfPassword;
    private JLabel         lblStatus;
    private final UserDAO  userDAO = new UserDAO();

    public LoginFrame() {
        initUI();
    }

    // =========================================================================
    // UI CONSTRUCTION
    // =========================================================================

    private void initUI() {
        setTitle("Eden Manager — Sign In");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(600, 450));
        setExtendedState(JFrame.MAXIMIZED_BOTH);   // open maximised
        setLayout(new BorderLayout());
        getContentPane().setBackground(UITheme.PARCHMENT);

        add(buildLeftPanel(),   BorderLayout.WEST);
        add(buildCenterPanel(), BorderLayout.CENTER);
    }

    // ── Left decorative panel ─────────────────────────────────────────────────
    private JPanel buildLeftPanel() {
        UITheme.GradientPanel left = new UITheme.GradientPanel(
                new BorderLayout(), UITheme.CANOPY, UITheme.FERN);
        left.setPreferredSize(new Dimension(280, 0));   // fixed width; height fills
        left.setBorder(BorderFactory.createEmptyBorder(
                UITheme.PAD_XL, UITheme.PAD_LG, UITheme.PAD_XL, UITheme.PAD_LG));

        // Vertical brand stack
        JPanel brand = new JPanel();
        brand.setLayout(new BoxLayout(brand, BoxLayout.Y_AXIS));
        brand.setOpaque(false);

        JLabel iconLbl = new JLabel(makeLeafIcon(72));
        iconLbl.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel titleLbl = UITheme.label("Eden", UITheme.FONT_DISPLAY, Color.WHITE);
        titleLbl.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subLbl = UITheme.label("Manager", UITheme.FONT_SUBHEAD,
                new Color(0xB7E4C7));
        subLbl.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel tagLbl = new JLabel(
                "<html><div style='text-align:center;'>Your botanical<br>knowledge base</div></html>");
        tagLbl.setFont(UITheme.FONT_CAPTION);
        tagLbl.setForeground(new Color(0xB7E4C7));
        tagLbl.setAlignmentX(Component.CENTER_ALIGNMENT);
        tagLbl.setHorizontalAlignment(SwingConstants.CENTER);

        brand.add(Box.createVerticalGlue());
        brand.add(iconLbl);
        brand.add(Box.createVerticalStrut(UITheme.PAD_MD));
        brand.add(titleLbl);
        brand.add(Box.createVerticalStrut(4));
        brand.add(subLbl);
        brand.add(Box.createVerticalStrut(UITheme.PAD_LG));
        brand.add(tagLbl);
        brand.add(Box.createVerticalGlue());

        left.add(brand, BorderLayout.CENTER);

        JLabel ver = UITheme.label("v3.0 — Botanical Edition",
                UITheme.FONT_SMALL, new Color(255, 255, 255, 90));
        ver.setHorizontalAlignment(SwingConstants.CENTER);
        left.add(ver, BorderLayout.SOUTH);

        return left;
    }

    // ── Centre: parchment background + centred form card ─────────────────────
    private JPanel buildCenterPanel() {
        // Outer wrapper fills the CENTER slot of the root BorderLayout.
        // GridBagLayout centres the card both horizontally and vertically
        // while still letting the card claim a comfortable width.
        JPanel wrapper = new JPanel(new GridBagLayout());
        wrapper.setBackground(UITheme.PARCHMENT);

        GridBagConstraints wc = new GridBagConstraints();
        wc.fill    = GridBagConstraints.NONE;   // card keeps natural size...
        wc.anchor  = GridBagConstraints.CENTER; // ...and is centred
        wc.weightx = 1.0;
        wc.weighty = 1.0;
        wrapper.add(buildFormCard(), wc);
        return wrapper;
    }

    // ── Form card ─────────────────────────────────────────────────────────────
    private JPanel buildFormCard() {
        JPanel card = new JPanel(new GridBagLayout());
        card.setBackground(UITheme.CREAM);
        card.setBorder(BorderFactory.createCompoundBorder(
                new UITheme.RoundedBorder(UITheme.CREAM_BORDER, UITheme.RADIUS_LARGE, 1f),
                BorderFactory.createEmptyBorder(
                        UITheme.PAD_XL, UITheme.PAD_XL, UITheme.PAD_XL, UITheme.PAD_XL)));

        GridBagConstraints gc = new GridBagConstraints();
        gc.gridx   = 0;
        gc.fill    = GridBagConstraints.HORIZONTAL;
        gc.weightx = 1.0;                // every row stretches to card width
        int row    = 0;

        // Heading
        gc.gridy  = row++;
        gc.insets = new Insets(0, 0, UITheme.PAD_LG, 0);
        card.add(UITheme.label("Welcome back 🌿", UITheme.FONT_TITLE, UITheme.INK), gc);

        // Username label
        gc.gridy  = row++;
        gc.insets = new Insets(0, 0, 4, 0);
        card.add(fieldLbl("Username"), gc);

        // Username field
        gc.gridy  = row++;
        gc.insets = new Insets(0, 0, UITheme.PAD_SM, 0);
        tfUsername = UITheme.styledTextField(0);
        tfUsername.setPreferredSize(new Dimension(320, 40));
        card.add(tfUsername, gc);

        // Password label
        gc.gridy  = row++;
        gc.insets = new Insets(0, 0, 4, 0);
        card.add(fieldLbl("Password"), gc);

        // Password field
        gc.gridy  = row++;
        gc.insets = new Insets(0, 0, UITheme.PAD_SM, 0);
        pfPassword = UITheme.styledPasswordField(0);
        pfPassword.setPreferredSize(new Dimension(320, 40));
        card.add(pfPassword, gc);

        // Status message
        gc.gridy  = row++;
        gc.insets = new Insets(2, 2, 2, 0);
        lblStatus  = new JLabel(" ");
        lblStatus.setFont(UITheme.FONT_SMALL);
        lblStatus.setForeground(UITheme.TERRACOTTA);
        card.add(lblStatus, gc);

        // Sign In button  — full width
        gc.gridy  = row++;
        gc.insets = new Insets(UITheme.PAD_SM, 0, UITheme.PAD_SM, 0);
        JButton btnLogin = UITheme.primaryButton("Sign In  →");
        btnLogin.setPreferredSize(new Dimension(320, 44));
        card.add(btnLogin, gc);

        // Register link
        gc.gridy  = row++;
        gc.insets = new Insets(UITheme.PAD_MD, 0, 0, 0);
        JPanel linkRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 4, 0));
        linkRow.setOpaque(false);
        linkRow.add(UITheme.label("New to Eden?", UITheme.FONT_SMALL, UITheme.INK_LIGHT));
        JButton btnSignup = UITheme.linkButton("Create an account");
        linkRow.add(btnSignup);
        card.add(linkRow, gc);

        // Events
        ActionListener doLogin = e -> performLogin();
        btnLogin.addActionListener(doLogin);
        pfPassword.addActionListener(doLogin);
        tfUsername.addActionListener(doLogin);
        btnSignup.addActionListener(e -> openSignup());

        return card;
    }

    // =========================================================================
    // LOGIC
    // =========================================================================

    private void performLogin() {
        String username = tfUsername.getText().trim();
        String password = new String(pfPassword.getPassword()).trim();

        if (username.isEmpty() || password.isEmpty()) {
            setStatus("Please enter both username and password.");
            return;
        }
        User user = userDAO.login(username, password);
        if (user != null) {
            dispose();
            SwingUtilities.invokeLater(() -> {
                if (user.isAdmin()) new AdminDashboard(user).setVisible(true);
                else                new UserGallery(user).setVisible(true);
            });
        } else {
            setStatus("Incorrect username or password.");
            pfPassword.setText("");
        }
    }

    private void openSignup() {
        setVisible(false);
        new SignupFrame(this).setVisible(true);
    }

    private void setStatus(String msg) {
        lblStatus.setText(msg.isEmpty() ? " " : msg);
    }

    // =========================================================================
    // HELPERS
    // =========================================================================

    private static JLabel fieldLbl(String text) {
        JLabel l = UITheme.label(text, UITheme.FONT_LABEL, UITheme.INK_LIGHT);
        l.setBorder(BorderFactory.createEmptyBorder(0, 2, 0, 0));
        return l;
    }

    /** Paints a simple circle + leaf emoji icon. */
    private static ImageIcon makeLeafIcon(int size) {
        BufferedImage img = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(new Color(255, 255, 255, 25));
        g.fillOval(4, 4, size - 8, size - 8);
        g.setColor(new Color(255, 255, 255, 50));
        g.setStroke(new BasicStroke(1.5f));
        g.drawOval(4, 4, size - 8, size - 8);
        g.setFont(new Font("Segoe UI Emoji", Font.PLAIN, size / 2));
        FontMetrics fm = g.getFontMetrics();
        String leaf = "🌿";
        g.drawString(leaf, (size - fm.stringWidth(leaf)) / 2, size / 2 + fm.getAscent() / 3);
        g.dispose();
        return new ImageIcon(img);
    }
}