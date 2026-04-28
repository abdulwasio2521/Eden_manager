package edenmanager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * SignupFrame.java  —  v3
 *
 * ROOT layout: BorderLayout
 *   WEST   — 260px GradientPanel (brand + hint)
 *   CENTER — JScrollPane  ›  GridBagLayout wrapper  ›  form card
 *
 * Key fixes vs v2
 * ───────────────
 * • setExtendedState(MAXIMIZED_BOTH) — opens full-screen.
 * • The JScrollPane viewport background matches PARCHMENT so there is
 *   no white flash on resize.
 * • The inner wrapper panel uses GridBagLayout with weightx=1 / weighty=1
 *   so the card is centred AND the scroll area fills all available space.
 * • Every field row has gc.fill = HORIZONTAL + gc.weightx = 1.0 so text
 *   fields actually grow with the card.
 * • Preferred width on the card itself is set via a dummy Dimension so
 *   it is never squished below a readable size.
 */
public class SignupFrame extends JFrame {

    private JTextField     tfFullname, tfUsername;
    private JPasswordField pfPassword, pfConfirm, pfAdminKey;
    private JLabel         lblStatus;

    private final UserDAO    userDAO;
    private final LoginFrame loginFrame;

    public SignupFrame(LoginFrame loginFrame) {
        this.loginFrame = loginFrame;
        this.userDAO    = new UserDAO();
        initUI();
    }

    // =========================================================================
    // UI CONSTRUCTION
    // =========================================================================

    private void initUI() {
        setTitle("Eden Manager — Create Account");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setMinimumSize(new Dimension(600, 500));
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setLayout(new BorderLayout());
        getContentPane().setBackground(UITheme.PARCHMENT);

        add(buildLeftPanel(),   BorderLayout.WEST);
        add(buildScrollForm(),  BorderLayout.CENTER);

        addWindowListener(new WindowAdapter() {
            @Override public void windowClosed(WindowEvent e) {
                loginFrame.setVisible(true);
            }
        });
    }

    // ── Left panel ────────────────────────────────────────────────────────────
    private JPanel buildLeftPanel() {
        UITheme.GradientPanel left = new UITheme.GradientPanel(
                new BorderLayout(), UITheme.CANOPY, new Color(0x1B5E20));
        left.setPreferredSize(new Dimension(260, 0));
        left.setBorder(BorderFactory.createEmptyBorder(
                UITheme.PAD_XL, UITheme.PAD_LG, UITheme.PAD_XL, UITheme.PAD_LG));

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setOpaque(false);

        JLabel seedLbl = new JLabel("🌱");
        seedLbl.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 46));
        seedLbl.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel t1 = UITheme.label("Start your", UITheme.FONT_SUBHEAD, new Color(0xB7E4C7));
        t1.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel t2 = UITheme.label("journey", UITheme.FONT_DISPLAY, Color.WHITE);
        t2.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Admin key hint box
        JPanel hintBox = new JPanel(new BorderLayout());
        hintBox.setOpaque(false);
        hintBox.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(255, 255, 255, 40), 1),
                BorderFactory.createEmptyBorder(UITheme.PAD_SM, UITheme.PAD_SM,
                        UITheme.PAD_SM, UITheme.PAD_SM)));
        JLabel hintLbl = new JLabel(
                "<html><center><span style='color:#B7E4C7;'>Use key <b>EDEN123</b>"
                + "<br>for an Admin account.<br>Leave blank for User.</span></center></html>");
        hintLbl.setFont(UITheme.FONT_SMALL);
        hintLbl.setHorizontalAlignment(SwingConstants.CENTER);
        hintBox.add(hintLbl, BorderLayout.CENTER);

        content.add(Box.createVerticalGlue());
        content.add(seedLbl);
        content.add(Box.createVerticalStrut(UITheme.PAD_SM));
        content.add(t1);
        content.add(t2);
        content.add(Box.createVerticalStrut(UITheme.PAD_LG));
        content.add(hintBox);
        content.add(Box.createVerticalGlue());

        left.add(content, BorderLayout.CENTER);
        return left;
    }

    // ── Scrollable form area ──────────────────────────────────────────────────
    private JScrollPane buildScrollForm() {
        // Wrapper: fills the scroll viewport and centres the card.
        JPanel wrapper = new JPanel(new GridBagLayout());
        wrapper.setBackground(UITheme.PARCHMENT);

        GridBagConstraints wc = new GridBagConstraints();
        wc.fill    = GridBagConstraints.NONE;
        wc.anchor  = GridBagConstraints.CENTER;
        wc.weightx = 1.0;
        wc.weighty = 1.0;
        wc.insets  = new Insets(UITheme.PAD_LG, UITheme.PAD_LG,
                                UITheme.PAD_LG, UITheme.PAD_LG);
        wrapper.add(buildFormCard(), wc);

        JScrollPane sp = new JScrollPane(wrapper);
        sp.setBorder(null);
        sp.setBackground(UITheme.PARCHMENT);
        sp.getViewport().setBackground(UITheme.PARCHMENT);
        sp.getVerticalScrollBar().setUnitIncrement(16);
        sp.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        return sp;
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
        gc.weightx = 1.0;
        int row    = 0;

        // Heading
        gc.gridy  = row++;
        gc.insets = new Insets(0, 0, UITheme.PAD_LG, 0);
        card.add(UITheme.label("Create your account",
                UITheme.FONT_TITLE, UITheme.INK), gc);

        // All five fields
        row = addField(card, gc, row, "Full Name *",         tfFullname  = field());
        row = addField(card, gc, row, "Username *",          tfUsername  = field());
        row = addField(card, gc, row, "Password *",          pfPassword  = pass());
        row = addField(card, gc, row, "Confirm Password *",  pfConfirm   = pass());
        row = addField(card, gc, row, "Admin Secret Key",    pfAdminKey  = pass());

        // Hint below admin key
        gc.gridy  = row++;
        gc.insets = new Insets(0, 2, UITheme.PAD_SM, 0);
        card.add(UITheme.label("Leave blank for a regular User account.",
                UITheme.FONT_SMALL, UITheme.INK_LIGHT), gc);

        // Status message
        gc.gridy  = row++;
        gc.insets = new Insets(2, 2, 2, 0);
        lblStatus  = new JLabel(" ");
        lblStatus.setFont(UITheme.FONT_SMALL);
        lblStatus.setForeground(UITheme.TERRACOTTA);
        card.add(lblStatus, gc);

        // Create Account button
        gc.gridy  = row++;
        gc.insets = new Insets(UITheme.PAD_SM, 0, UITheme.PAD_SM, 0);
        JButton btnReg = UITheme.primaryButton("Create Account  →");
        btnReg.setPreferredSize(new Dimension(320, 44));
        card.add(btnReg, gc);

        // Back link
        gc.gridy  = row++;
        gc.insets = new Insets(UITheme.PAD_SM, 0, 0, 0);
        JPanel backRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 4, 0));
        backRow.setOpaque(false);
        backRow.add(UITheme.label("Already have an account?",
                UITheme.FONT_SMALL, UITheme.INK_LIGHT));
        JButton btnBack = UITheme.linkButton("Sign in");
        backRow.add(btnBack);
        card.add(backRow, gc);

        btnReg.addActionListener(e -> performSignup());
        btnBack.addActionListener(e -> goBack());

        return card;
    }

    // =========================================================================
    // LOGIC
    // =========================================================================

    private void performSignup() {
        String fullname = tfFullname.getText().trim();
        String username = tfUsername.getText().trim();
        String password = new String(pfPassword.getPassword()).trim();
        String confirm  = new String(pfConfirm.getPassword()).trim();
        String adminKey = new String(pfAdminKey.getPassword()).trim();

        if (fullname.isEmpty() || username.isEmpty() || password.isEmpty()) {
            setStatus("Full name, username, and password are required.");
            return;
        }
        if (username.length() < 3) { setStatus("Username must be ≥ 3 characters."); return; }
        if (password.length() < 4) { setStatus("Password must be ≥ 4 characters."); return; }
        if (!password.equals(confirm)) { setStatus("Passwords do not match."); return; }
        if (userDAO.usernameExists(username)) {
            setStatus("Username '" + username + "' is already taken.");
            return;
        }

        boolean ok = userDAO.signup(fullname, username, password, adminKey);
        if (ok) {
            String role = UserDAO.ADMIN_SECRET_KEY.equals(adminKey) ? "Admin" : "User";
            JOptionPane.showMessageDialog(this,
                    "<html><b>Account created!</b><br>Role: <i>" + role
                    + "</i><br>You may now sign in.</html>",
                    "Welcome to Eden 🌿", JOptionPane.INFORMATION_MESSAGE);
            goBack();
        } else {
            setStatus("Registration failed. Please try again.");
        }
    }

    private void goBack() {
        loginFrame.setVisible(true);
        dispose();
    }

    private void setStatus(String msg) {
        lblStatus.setText(msg.isEmpty() ? " " : msg);
    }

    // =========================================================================
    // HELPERS
    // =========================================================================

    /** Adds a label + field pair to the GridBagLayout card and returns new row index. */
    private static int addField(JPanel card, GridBagConstraints gc,
                                int row, String label, JComponent field) {
        gc.gridy  = row++;
        gc.insets = new Insets(UITheme.PAD_SM, 0, 4, 0);
        JLabel lbl = UITheme.label(label, UITheme.FONT_LABEL, UITheme.INK_LIGHT);
        lbl.setBorder(BorderFactory.createEmptyBorder(0, 2, 0, 0));
        card.add(lbl, gc);

        gc.gridy  = row++;
        gc.insets = new Insets(0, 0, 2, 0);
        card.add(field, gc);
        return row;
    }

    private static JTextField field() {
        JTextField tf = UITheme.styledTextField(0);
        tf.setPreferredSize(new Dimension(320, 40));
        return tf;
    }

    private static JPasswordField pass() {
        JPasswordField pf = UITheme.styledPasswordField(0);
        pf.setPreferredSize(new Dimension(320, 40));
        return pf;
    }
}