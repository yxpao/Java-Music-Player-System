import javax.sound.sampled.Clip;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JTextField;
import javax.swing.WindowConstants;
import javax.swing.BorderFactory;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import javax.swing.JPanel;
import java.awt.Dimension;
import java.awt.Color;
import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JScrollPane;
import java.awt.Font; 
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.Cursor;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import javax.swing.JDialog;
import javax.swing.JProgressBar;
import javax.swing.JSlider;
import javax.swing.ImageIcon;
import java.awt.Image;
import java.io.File;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

public class MusicPlayerUI extends JFrame {

    protected JTextField textField1;
    public JButton playBtn; 
    public JButton pauseBtn; 
    public JButton loopBtn; 
    public JButton prevBtn;
    public JButton stopBtn;
    public JButton nextBtn;
    public JButton chooseBtn; 
    protected JButton browseToggleBtn;
    public JFileChooser fileChooser; 
    protected JPanel fileSelectionPanel;
    public DefaultListModel<String> playlistModel; 
    public JList<String> playlistList; 
    public JButton addBtn; 
    public JButton removeBtn; 
    public JButton saveBtn; 
    public JButton removePlaylistBtn; 

    protected String currentUserName = "Guest"; 
    protected JDialog loginDialog;
    protected JTextField usernameField;
    protected JLabel welcomeLabel;
    public JLabel navMusicLabel; 
    public JPanel navList; 
    protected JLabel createPlaylistLabel;
    public java.util.Map<String, DefaultListModel<String>> savedPlaylists; 
    protected JButton confirmBtn;
    protected JButton guestBtn;
    protected String loginResult;
    public JLabel selectedNavItem = null; 
    protected JLabel statusLabel;
    public JProgressBar progressBar; 
    public JSlider volumeSlider;
    protected JLabel albumArtLabel;
    protected JLabel titleLabel;
    protected JLabel artistLabel;
    public javax.swing.Timer progressTimer; 
    protected boolean loopActive = false;

    public static final Color Background_CLR = new Color(225, 235, 245);
    protected static final Color Header_CLR = new Color(245, 248, 252);
    protected static final Color ControlButtons_CLR = new Color(200, 215, 230);
    public static final Color Sidebar_CLR = new Color(230, 240, 250);
    public static final Color Accent_CLR = new Color(50, 100, 180);
    
    protected MusicPlayerManager controller;

    public MusicPlayerUI() {

        if (savedPlaylists == null) {
            savedPlaylists = new java.util.HashMap<>();
        }

        welcomeLabel = new JLabel("Welcome to Java Harmony, " + currentUserName, SwingConstants.LEFT);
        welcomeLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        welcomeLabel.setForeground(Accent_CLR.darker());
        welcomeLabel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        setTitle("Java Harmony");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        getContentPane().setBackground(Background_CLR);

        setLayout(new BorderLayout(5, 5)); 

        textField1 = new JTextField(25);
        
        textField1.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(173, 173, 173), 1), 
            BorderFactory.createEmptyBorder(3, 6, 3, 6)  
        ));
        textField1.setBackground(Header_CLR);
        textField1.setForeground(Color.BLACK);
        textField1.setFont(new Font("Segoe UI", Font.PLAIN, 12)); 
        textField1.setEditable(false);
        textField1.setFocusable(false);

        playBtn = new JButton("Play Track");
        playBtn.setToolTipText("Play selected track or current file");
        pauseBtn = new JButton("Pause");
        pauseBtn.setToolTipText("Pause/Resume playback");
        loopBtn = new JButton("Loop");
        loopBtn.setToolTipText("Toggle loop mode");
        chooseBtn = new JButton("Browse...");
        chooseBtn.setToolTipText("Browse for music file");
        fileChooser = new JFileChooser();
        
        browseToggleBtn = new JButton("...");
        browseToggleBtn.setToolTipText("Browse for music file");
        browseToggleBtn.setFont(new Font("Segoe UI", Font.PLAIN, 20));
        browseToggleBtn.setPreferredSize(new Dimension(40, 40));
        browseToggleBtn.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        browseToggleBtn.setBackground(Background_CLR);
        browseToggleBtn.setForeground(Accent_CLR.darker());
        browseToggleBtn.setFocusPainted(false);
        
        styleControlButton(playBtn);
        styleControlButton(pauseBtn);
        styleControlButton(loopBtn);
        styleSimpleButton(chooseBtn);

        playlistModel = new DefaultListModel<>();
        playlistList = new JList<>(playlistModel);
        addBtn = new JButton("Add to List");
        addBtn.setToolTipText("Add selected track to your playlist");
        removeBtn = new JButton("Remove");
        removeBtn.setToolTipText("Remove selected track from your playlist");
        
        styleSimpleButton(addBtn);
        styleSimpleButton(removeBtn);
        
        JPanel headerPanel = new JPanel();
        headerPanel.removeAll();
        headerPanel.setOpaque(false);
        headerPanel.setLayout(new BorderLayout());
        
        fileSelectionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        fileSelectionPanel.setOpaque(false);
        JLabel filePathLabel = new JLabel("File Path:");
        filePathLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        filePathLabel.setForeground(Accent_CLR.darker());
        fileSelectionPanel.add(filePathLabel);
        fileSelectionPanel.add(textField1);
        fileSelectionPanel.add(chooseBtn);
        fileSelectionPanel.setVisible(false);
        
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false);
        topPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        topPanel.add(welcomeLabel, BorderLayout.WEST);
        
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        rightPanel.setOpaque(false);
        rightPanel.add(browseToggleBtn);
        topPanel.add(rightPanel, BorderLayout.EAST);
        
        browseToggleBtn.addActionListener(e -> {
            boolean isVisible = fileSelectionPanel.isVisible();
            fileSelectionPanel.setVisible(!isVisible);
            headerPanel.revalidate();
            headerPanel.repaint();
        });
        
        headerPanel.add(topPanel, BorderLayout.NORTH);
        headerPanel.add(fileSelectionPanel, BorderLayout.SOUTH);

        JPanel sidebar = createSidebar();
        
        playlistList.setBackground(Color.WHITE);
        playlistList.setForeground(Color.BLACK);
        playlistList.setSelectionBackground(new Color(180, 210, 240));
        playlistList.setSelectionForeground(Accent_CLR);
        playlistList.setBorder(BorderFactory.createEmptyBorder()); 
        
        JScrollPane playlistScroll = new JScrollPane(playlistList);
        playlistScroll.setPreferredSize(new Dimension(300, 150));
        playlistScroll.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(173, 173, 173)),
            BorderFactory.createEmptyBorder(0, 0, 0, 5)
        )); 

        JPanel playlistControls = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        playlistControls.setOpaque(false);
        playlistControls.add(addBtn);
        playlistControls.add(removeBtn);

        saveBtn = new JButton("Create Playlist");
        saveBtn.setToolTipText("Save your playlist as a new playlist");
        removePlaylistBtn = new JButton("Remove Playlist");
        removePlaylistBtn.setToolTipText("Remove the selected playlist from navigation");
        styleSimpleButton(saveBtn);
        styleSimpleButton(removePlaylistBtn);
        saveBtn.setVisible(false);
        removePlaylistBtn.setVisible(false);
        playlistControls.add(saveBtn);
        playlistControls.add(removePlaylistBtn);

        JPanel nowPlayingPanel = createNowPlayingPanel();
        
        JPanel mainContentPanel = new JPanel(new BorderLayout(5, 5));
        mainContentPanel.setOpaque(false);
        mainContentPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 10));
        mainContentPanel.add(nowPlayingPanel, BorderLayout.NORTH);
        mainContentPanel.add(playlistScroll, BorderLayout.CENTER);
        mainContentPanel.add(playlistControls, BorderLayout.SOUTH);

        JPanel bottomControls = new JPanel(new BorderLayout());
        bottomControls.setBackground(ControlButtons_CLR);
        bottomControls.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(150, 160, 180)),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));

        JPanel playbackControlsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        playbackControlsPanel.setOpaque(false);

        Dimension controlBtnSize = new Dimension(100, 40);
        playBtn.setPreferredSize(controlBtnSize);
        playBtn.setMinimumSize(controlBtnSize);
        playBtn.setMaximumSize(controlBtnSize);
        pauseBtn.setPreferredSize(controlBtnSize);
        pauseBtn.setMinimumSize(controlBtnSize);
        pauseBtn.setMaximumSize(controlBtnSize);
        loopBtn.setPreferredSize(controlBtnSize);
        loopBtn.setMinimumSize(controlBtnSize);
        loopBtn.setMaximumSize(controlBtnSize);
        
        
        ImageIcon prevIcon = loadIcon("previous-button.png", 28, 28);
        ImageIcon playIcon = loadIcon("play-button-arrowhead.png", 28, 28);
        ImageIcon pauseIcon = loadIcon("pause-play-button.png", 28, 28);
        if (pauseIcon == null) pauseIcon = loadIcon("pause-button.png", 28, 28);
        ImageIcon nextIcon = loadIcon("next-button.png", 28, 28);
        ImageIcon loopIcon = loadIcon("loop-button.png", 22, 22);
        ImageIcon stopIcon = loadIcon("stop-button.png", 28, 28);

        prevBtn = new JButton(prevIcon != null ? prevIcon : new ImageIcon());
        playBtn = new JButton(playIcon != null ? playIcon : new ImageIcon());
        pauseBtn = new JButton(pauseIcon != null ? pauseIcon : new ImageIcon());
        nextBtn = new JButton(nextIcon != null ? nextIcon : new ImageIcon());
        stopBtn = new JButton(stopIcon != null ? stopIcon : new ImageIcon());
        loopBtn = new JButton(loopIcon != null ? loopIcon : new ImageIcon());

        prevBtn.setToolTipText("Previous");
        playBtn.setToolTipText("Play");
        pauseBtn.setToolTipText("Pause/Resume");
        nextBtn.setToolTipText("Next");
        stopBtn.setToolTipText("Stop");
        loopBtn.setToolTipText("Loop");

        styleControlButton(prevBtn);
        styleControlButton(playBtn);
        styleControlButton(pauseBtn);
        styleControlButton(nextBtn);
        styleControlButton(stopBtn);
        styleControlButton(loopBtn);

        Dimension smallBtn = new Dimension(80, 36);
        prevBtn.setPreferredSize(smallBtn);
        playBtn.setPreferredSize(new Dimension(100, 40));
        pauseBtn.setPreferredSize(new Dimension(100, 40));
        nextBtn.setPreferredSize(smallBtn);
        stopBtn.setPreferredSize(smallBtn);
        loopBtn.setPreferredSize(new Dimension(80, 36));

        playbackControlsPanel.add(prevBtn);
        playbackControlsPanel.add(pauseBtn);
        playbackControlsPanel.add(nextBtn);
        playbackControlsPanel.add(stopBtn);
        playbackControlsPanel.add(loopBtn);

        JPanel volumePanel = new JPanel(new GridBagLayout());
        volumePanel.setOpaque(false);

        volumeSlider = new JSlider(0, 100, 70);
        volumeSlider.setPreferredSize(new Dimension(140, 36));
        volumeSlider.setToolTipText("Volume: 70%");
        volumeSlider.setOpaque(false);
        JLabel volumeLabel = new JLabel("70%");
        volumeLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        volumeLabel.setForeground(Accent_CLR.darker());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.weightx = 0.0;
        gbc.insets = new Insets(0, 0, 0, 5);

        gbc.gridx = 0;
        volumePanel.add(volumeSlider, gbc);

        gbc.gridx = 1;
        gbc.insets = new Insets(0, 0, 0, 0);
        volumePanel.add(volumeLabel, gbc);

        bottomControls.add(playbackControlsPanel, BorderLayout.CENTER);
        bottomControls.add(volumePanel, BorderLayout.EAST);
        
        progressBar = new JProgressBar(0, 100) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                int width = getWidth();
                int height = getHeight();
                int arc = 8;
                
                g2d.setColor(Header_CLR);
                g2d.fillRoundRect(0, 0, width, height, arc, arc);

                int progressWidth = (int) (width * (getValue() / 100.0));
                if (progressWidth > 0) {
                    java.awt.GradientPaint gradient = new java.awt.GradientPaint(
                        0, 0, Accent_CLR,
                        0, height, Accent_CLR.darker()
                    );
                    g2d.setPaint(gradient);
                    g2d.fillRoundRect(0, 0, progressWidth, height, arc, arc);
                    
                    g2d.setColor(new Color(255, 255, 255, 30));
                    g2d.fillRoundRect(0, 0, progressWidth, height / 2, arc, arc);
                }
                
                g2d.setColor(new Color(180, 190, 200));
                g2d.setStroke(new java.awt.BasicStroke(1.0f));
                g2d.drawRoundRect(0, 0, width - 1, height - 1, arc, arc);
                
                if (isStringPainted()) {
                    g2d.setFont(getFont());
                    g2d.setColor(Accent_CLR.darker());
                    String text = getString();
                    java.awt.FontMetrics fm = g2d.getFontMetrics();
                    int x = (width - fm.stringWidth(text)) / 2;
                    int y = (height + fm.getAscent()) / 2 - 2;
                    g2d.drawString(text, x, y);
                }
                
                g2d.dispose();
            }
        };
        progressBar.setValue(0);
        progressBar.setStringPainted(true);
        progressBar.setString("0:00 / 0:00");
        progressBar.setFont(new Font("Segoe UI", Font.BOLD, 11));
        progressBar.setPreferredSize(new Dimension(0, 28));
        progressBar.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
        progressBar.setOpaque(false);
        
        statusLabel = new JLabel("");
        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        statusLabel.setForeground(Accent_CLR.darker());
        statusLabel.setHorizontalAlignment(SwingConstants.CENTER);
        statusLabel.setBorder(BorderFactory.createEmptyBorder(4, 6, 4, 6));
        statusLabel.setOpaque(true);
        statusLabel.setBackground(ControlButtons_CLR);

        JPanel footerPanel = new JPanel(new BorderLayout());
        footerPanel.setOpaque(false);
        footerPanel.add(statusLabel, BorderLayout.NORTH);
        footerPanel.add(progressBar, BorderLayout.CENTER);
        footerPanel.add(bottomControls, BorderLayout.SOUTH);

        add(headerPanel, BorderLayout.NORTH); 
        add(sidebar, BorderLayout.WEST);
        add(mainContentPanel, BorderLayout.CENTER); 
        add(footerPanel, BorderLayout.SOUTH);

        setSize(850, 500);
        setLocationRelativeTo(null);

    }
    
    private void styleControlButton(JButton btn) {
        Color baseBg = Color.WHITE;
        Color hoverBg = new Color(245, 248, 252);
        Color pressedBg = new Color(235, 240, 245);
        Color baseBorder = new Color(180, 190, 200);
        Color hoverBorder = Accent_CLR;

        btn.setBackground(baseBg);
        btn.setForeground(Accent_CLR.darker()); 
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setFocusPainted(false); 
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setContentAreaFilled(true);
        btn.setOpaque(true);

        btn.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(baseBorder, 1),
            BorderFactory.createEmptyBorder(6, 10, 6, 10)
        ));

        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (btn == loopBtn && loopActive) {
                    btn.setBackground(Accent_CLR.darker());
                    btn.setForeground(Color.WHITE);
                    btn.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(Accent_CLR.darker(), 1),
                        BorderFactory.createEmptyBorder(6, 10, 6, 10)
                    ));
                } else {
                    btn.setBackground(hoverBg);
                    btn.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(hoverBorder, 1),
                        BorderFactory.createEmptyBorder(6, 10, 6, 10)
                    ));
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                if (btn == loopBtn && loopActive) {
                    btn.setBackground(Accent_CLR);
                    btn.setForeground(Color.WHITE);
                    btn.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(Accent_CLR.darker(), 1),
                        BorderFactory.createEmptyBorder(6, 10, 6, 10)
                    ));
                } else {
                    btn.setBackground(baseBg);
                    btn.setForeground(Accent_CLR.darker());
                    btn.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(baseBorder, 1),
                        BorderFactory.createEmptyBorder(6, 10, 6, 10)
                    ));
                }
            }

            @Override
            public void mousePressed(MouseEvent e) {
                if (btn == loopBtn && loopActive) {
                    btn.setBackground(Accent_CLR.darker());
                } else {
                    btn.setBackground(pressedBg);
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (btn.getBounds().contains(e.getPoint())) {
                    if (btn == loopBtn && loopActive) {
                        btn.setBackground(Accent_CLR.darker());
                        btn.setForeground(Color.WHITE);
                        btn.setBorder(BorderFactory.createCompoundBorder(
                            BorderFactory.createLineBorder(Accent_CLR.darker(), 1),
                            BorderFactory.createEmptyBorder(6, 10, 6, 10)
                        ));
                    } else {
                        btn.setBackground(hoverBg);
                        btn.setForeground(Accent_CLR.darker());
                        btn.setBorder(BorderFactory.createCompoundBorder(
                            BorderFactory.createLineBorder(hoverBorder, 1),
                            BorderFactory.createEmptyBorder(6, 10, 6, 10)
                        ));
                    }
                } else {
                    if (btn == loopBtn && loopActive) {
                        btn.setBackground(Accent_CLR);
                        btn.setForeground(Color.WHITE);
                        btn.setBorder(BorderFactory.createCompoundBorder(
                            BorderFactory.createLineBorder(Accent_CLR.darker(), 1),
                            BorderFactory.createEmptyBorder(6, 10, 6, 10)
                        ));
                    } else {
                        btn.setBackground(baseBg);
                        btn.setForeground(Accent_CLR.darker());
                        btn.setBorder(BorderFactory.createCompoundBorder(
                            BorderFactory.createLineBorder(baseBorder, 1),
                            BorderFactory.createEmptyBorder(6, 10, 6, 10)
                        ));
                    }
                }
            }
        });
    }
    
    public void setLoopActive(boolean active) {
        loopActive = active;
        if (loopBtn == null) return;

        if (loopActive) {
            loopBtn.setBackground(Accent_CLR);
            loopBtn.setForeground(Color.WHITE);
            loopBtn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Accent_CLR.darker(), 1),
                BorderFactory.createEmptyBorder(6, 10, 6, 10)
            ));
        } else {
            Color baseBg = Color.WHITE;
            Color baseBorder = new Color(180, 190, 200);
            loopBtn.setBackground(baseBg);
            loopBtn.setForeground(Accent_CLR.darker());
            loopBtn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(baseBorder, 1),
                BorderFactory.createEmptyBorder(6, 10, 6, 10)
            ));
        }
    }
    
    private void styleSimpleButton(JButton btn) {
        Color baseBg = Background_CLR;
        Color hoverBg = new Color(210, 225, 240);
        Color pressedBg = new Color(200, 215, 230);
        Color baseText = Color.BLACK;
        Color hoverText = Accent_CLR.darker();

        btn.setBackground(baseBg); 
        btn.setForeground(baseText);
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btn.setFocusPainted(false); 
        btn.setBorder(BorderFactory.createEmptyBorder(5, 8, 5, 8));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setContentAreaFilled(true);
        btn.setOpaque(true);

        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                btn.setBackground(hoverBg);
                btn.setForeground(hoverText);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                btn.setBackground(baseBg);
                btn.setForeground(baseText);
            }

            @Override
            public void mousePressed(MouseEvent e) {
                btn.setBackground(pressedBg);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (btn.getBounds().contains(e.getPoint())) {
                    btn.setBackground(hoverBg);
                    btn.setForeground(hoverText);
                } else {
                    btn.setBackground(baseBg);
                    btn.setForeground(baseText);
                }
            }
        });
    }

    private ImageIcon loadIcon(String filename, int w, int h) {
        try {
            File f = new File("icons", filename);
            if (!f.exists()) return null;
            ImageIcon ic = new ImageIcon(f.getAbsolutePath());
            Image img = ic.getImage().getScaledInstance(w, h, Image.SCALE_SMOOTH);
            return new ImageIcon(img);
        } catch (Exception ex) {
            return null;
        }
    }
    
    private JPanel createSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BorderLayout());
        sidebar.setPreferredSize(new Dimension(150, 0));
        sidebar.setBackground(Sidebar_CLR);
        sidebar.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 0, 2, new Color(180, 190, 200)),
            BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));

        navList = new JPanel();
        navList.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 5));
        navList.setBackground(Sidebar_CLR);

        navMusicLabel = createNavItem("Music");
        navList.add(navMusicLabel);
        
        JLabel libraryLabel = new JLabel("Library");
        libraryLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        libraryLabel.setForeground(Accent_CLR.darker());
        libraryLabel.setBorder(BorderFactory.createEmptyBorder(8, 10, 5, 5));
        sidebar.add(libraryLabel, BorderLayout.NORTH);
        sidebar.add(navList, BorderLayout.CENTER);

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        footer.setOpaque(false);
        createPlaylistLabel = new JLabel("Create Playlist +");
        createPlaylistLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        createPlaylistLabel.setForeground(new Color(140, 140, 140));
        createPlaylistLabel.setEnabled(false);
        createPlaylistLabel.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
        createPlaylistLabel.setPreferredSize(new Dimension(140, 25));
        createPlaylistLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (!createPlaylistLabel.isEnabled()) return;
                String name = javax.swing.JOptionPane.showInputDialog(MusicPlayerUI.this, "Enter playlist name:", "Create Playlist", javax.swing.JOptionPane.QUESTION_MESSAGE);
                if (name != null && !name.trim().isEmpty()) {
                    if (controller != null) {
                        controller.createPlaylist(name.trim());
                    } else {
                        javax.swing.JOptionPane.showMessageDialog(MusicPlayerUI.this, "Controller not initialized yet", "Error", javax.swing.JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        });
        footer.add(createPlaylistLabel);
        sidebar.add(footer, BorderLayout.SOUTH);

        return sidebar;
    }

    public JLabel createNavItem(String text) { 
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.BOLD, 12));
        label.setForeground(Accent_CLR);
        label.setHorizontalAlignment(SwingConstants.LEFT);
        label.setPreferredSize(new Dimension(140, 25));
        label.setOpaque(true);
        label.setBackground(Sidebar_CLR);
        label.setCursor(new Cursor(Cursor.HAND_CURSOR));

        label.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                selectNavItem(label);
            }
        });

        return label;
    }

    public void selectNavItem(JLabel lbl) { 
        if (selectedNavItem != null) {
            selectedNavItem.setBackground(Sidebar_CLR);
            selectedNavItem.setForeground(Accent_CLR);
        }
        selectedNavItem = lbl;
        if (selectedNavItem != null) {
            selectedNavItem.setBackground(Accent_CLR);
            selectedNavItem.setForeground(Color.WHITE);
        }
        
        if (addBtn != null) {
            boolean isMusicLibrary = (lbl == navMusicLabel);
            addBtn.setVisible(isMusicLibrary);
        }
        
        if (saveBtn != null) {

             if (controller != null) {
                 controller.handleNavItemSelected(lbl.getText());
             }
        }
        
        if (controller != null) {
            controller.handleNavItemSelected(lbl.getText());
        }
    }

    public void showStatus(String message, int ms) { 
        if (statusLabel == null) return;
        statusLabel.setText(message);
        javax.swing.Timer t = new javax.swing.Timer(ms, e -> statusLabel.setText(""));
        t.setRepeats(false);
        t.start();
    }

    public JDialog buildLoginDialog() {
        loginDialog = new JDialog(this, "Login Required", true);
        loginDialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        loginDialog.setLayout(new BorderLayout(10, 10));
        loginDialog.setBackground(Background_CLR);
        loginDialog.setResizable(false);

        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 15, 8, 15));
        panel.setBackground(Background_CLR);

        usernameField = new JTextField(20);
        usernameField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(173, 173, 173), 1), 
            BorderFactory.createEmptyBorder(5, 8, 5, 8)  
        ));
        usernameField.setBackground(Header_CLR);
        usernameField.setForeground(Color.BLACK);
        usernameField.setFont(new Font("Segoe UI", Font.PLAIN, 12)); 

        JPanel inputPanel = new JPanel(new BorderLayout(5, 5));
        inputPanel.setOpaque(false);
        inputPanel.add(new JLabel("Enter Username:"), BorderLayout.NORTH);
        inputPanel.add(usernameField, BorderLayout.CENTER);

        confirmBtn = new JButton("Confirm");
        styleControlButton(confirmBtn); 
        confirmBtn.setPreferredSize(new Dimension(confirmBtn.getPreferredSize().width, 35));

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        buttonPanel.setOpaque(false);
        buttonPanel.add(confirmBtn);

        guestBtn = new JButton("Continue as Guest");
        styleSimpleButton(guestBtn);
        guestBtn.setFont(new Font("Arial", Font.PLAIN, 12));
        guestBtn.setPreferredSize(new Dimension(guestBtn.getPreferredSize().width, 35));
        buttonPanel.add(guestBtn);

        panel.add(inputPanel, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        loginDialog.add(panel, BorderLayout.CENTER);

        usernameField.requestFocusInWindow();

        return loginDialog;
    }
    public void updateWelcomeMessage() {
        welcomeLabel.setText("Welcome to Java Harmony, " + currentUserName + "!");
    }
    
    private JPanel createNowPlayingPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 5));
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(180, 190, 200)),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        
        albumArtLabel = new JLabel();
        albumArtLabel.setPreferredSize(new Dimension(120, 120));
        albumArtLabel.setMinimumSize(new Dimension(120, 120));
        albumArtLabel.setMaximumSize(new Dimension(120, 120));
        albumArtLabel.setHorizontalAlignment(SwingConstants.CENTER);
        albumArtLabel.setVerticalAlignment(SwingConstants.CENTER);
        albumArtLabel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(180, 190, 200), 1),
            BorderFactory.createEmptyBorder(2, 2, 2, 2)
        ));
        albumArtLabel.setBackground(Color.WHITE);
        albumArtLabel.setOpaque(true);
        setDefaultAlbumArt();
        
        JPanel infoPanel = new JPanel(new BorderLayout(5, 5));
        infoPanel.setOpaque(false);
        
        titleLabel = new JLabel("No track selected");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        titleLabel.setForeground(Accent_CLR.darker());
        
        artistLabel = new JLabel("Unknown Artist");
        artistLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        artistLabel.setForeground(Color.GRAY);
        
        infoPanel.add(titleLabel, BorderLayout.NORTH);
        infoPanel.add(artistLabel, BorderLayout.CENTER);
        
        panel.add(albumArtLabel, BorderLayout.WEST);
        panel.add(infoPanel, BorderLayout.CENTER);
        
        return panel;
    }
    
    protected void setDefaultAlbumArt() {
        setDefaultAlbumArt(null, null);
    }
    
    protected void setDefaultAlbumArt(String title, String artist) {
        ImageIcon defaultIcon = createDefaultAlbumArtIcon(120, 120, title, artist);
        albumArtLabel.setIcon(defaultIcon);
    }
    
    private ImageIcon createDefaultAlbumArtIcon(int width, int height, String title, String artist) {
        java.awt.image.BufferedImage img = new java.awt.image.BufferedImage(width, height, java.awt.image.BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = img.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        
        Color[] gradientColors = generateColorsFromText(title != null ? title : artist != null ? artist : "Music");
        
        java.awt.GradientPaint gradient = new java.awt.GradientPaint(
            0, 0, gradientColors[0],
            width, height, gradientColors[1]
        );
        g2d.setPaint(gradient);
        g2d.fillRoundRect(0, 0, width, height, 8, 8);
        
        String displayText = "";
        if (title != null && !title.isEmpty()) {
            displayText = title.substring(0, 1).toUpperCase();
        } else if (artist != null && !artist.isEmpty() && !artist.equals("Unknown Artist")) {
            displayText = artist.substring(0, 1).toUpperCase();
        }
        
        g2d.setColor(Color.WHITE);
        Font displayFont = new Font("Segoe UI", Font.BOLD, displayText.length() == 1 ? 48 : 36);
        g2d.setFont(displayFont);
        java.awt.FontMetrics fm = g2d.getFontMetrics();
        int x = (width - fm.stringWidth(displayText)) / 2;
        int y = (height + fm.getAscent() - fm.getDescent()) / 2;
        g2d.drawString(displayText, x, y);
        
        g2d.setColor(new Color(255, 255, 255, 30));
        g2d.setStroke(new java.awt.BasicStroke(2.0f));
        g2d.drawRoundRect(1, 1, width - 3, height - 3, 8, 8);
        
        g2d.dispose();
        return new ImageIcon(img);
    }
    
    private Color[] generateColorsFromText(String text) {
        Color lightBlue = new Color(70, 130, 200);
        Color darkBlue = new Color(40, 80, 150);
        
        return new Color[] { lightBlue, darkBlue };
    }
    
    public void updateNowPlaying(String title, String artist, ImageIcon albumArt) { 
        if (titleLabel != null) {
            titleLabel.setText(title != null && !title.isEmpty() ? title : "No track selected");
        }
        if (artistLabel != null) {
            artistLabel.setText(artist != null && !artist.isEmpty() ? artist : "Unknown Artist");
        }
        if (albumArtLabel != null) {
            if (albumArt != null) {
                Image img = albumArt.getImage();
                Image scaledImg = img.getScaledInstance(120, 120, Image.SCALE_SMOOTH);
                albumArtLabel.setIcon(new ImageIcon(scaledImg));
            } else {
                setDefaultAlbumArt(title, artist);
            }
        }
    }
    
    public void startProgressTimer() { 
        if (progressTimer != null) {
            progressTimer.stop();
        }
        progressTimer = new javax.swing.Timer(100, e -> updateProgressBar());
        progressTimer.start();
    }
    
    public void stopProgressTimer() { 
        if (progressTimer != null) {
            progressTimer.stop();
        }
    }
    
    public void updateProgressBar() { 
        if (controller == null) return;
        Clip clip = controller.getClip();
        
        if (clip == null || progressBar == null) {
            return;
        }
        
        long position = clip.getMicrosecondPosition();
        long length = clip.getMicrosecondLength();
        
        if (length > 0) {

            boolean looping = controller.isLooping();
            long actualPosition = looping ? (position % length) : position;
            
            int progress = (int) ((actualPosition * 100) / length);
            if (progress > 100) progress = 100;
            if (progress < 0) progress = 0;
            
            progressBar.setValue(progress);
            
            String currentTime = formatTime(actualPosition / 1000000);
            String totalTime = formatTime(length / 1000000);
            progressBar.setString(currentTime + " / " + totalTime);
        } else {
            progressBar.setValue(0);
            progressBar.setString("0:00 / 0:00");
        }
    }
    
    public String formatTime(long seconds) { 
        long minutes = seconds / 60;
        long secs = seconds % 60;
        return String.format("%d:%02d", minutes, secs);
    }
}