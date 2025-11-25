import java.io.File;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JLabel;
import java.awt.Component;
import javax.swing.JOptionPane;

public class MusicPlayerMain extends MusicPlayerUI implements ActionListener {
    
    private MusicPlayerManager controller;

    public MusicPlayerMain() {
        
        controller = new MusicPlayerManager(this);
        super.controller = controller;
        
        final javax.swing.JDialog login = buildLoginDialog();

        loginResult = null;

        confirmBtn.addActionListener(ev -> {
            String name = usernameField.getText().trim();
            if (name.isEmpty()) {
                JOptionPane.showMessageDialog(login, "Username cannot be empty.", "Input Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            loginResult = name;
            login.dispose();
        });

        guestBtn.addActionListener(ev -> {
            int result = JOptionPane.showConfirmDialog(
                login,
                "As a guest, you cannot use playlist features such as Add to List. Do you want to continue as Guest?",
                "Continue as Guest",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
            );
            if (result == JOptionPane.YES_OPTION) {
                loginResult = "Guest";
                login.dispose();
            }
        });

        login.pack();
        login.setLocationRelativeTo(this);
        login.setVisible(true);

        String name = loginResult;
        if (name == null) {
            System.exit(0);
            return;
        }
        
        currentUserName = name;
        controller.setCurrentUserName(name);
        updateWelcomeMessage();
        
        

        this.setVisible(true);

        pauseBtn.addActionListener(this);
        loopBtn.addActionListener(this);
        prevBtn.addActionListener(this);
        stopBtn.addActionListener(this);
        nextBtn.addActionListener(this);
        chooseBtn.addActionListener(this);
        addBtn.addActionListener(this);
        removeBtn.addActionListener(this);
        removePlaylistBtn.addActionListener(this);
        
        
        volumeSlider.addChangeListener(ev -> {
            if (volumeSlider == null) return;
            int val = volumeSlider.getValue();
            float level = val / 100.0f;
            if (controller != null) {
                controller.setVolume(level);
            }
            
            java.awt.Container parent = volumeSlider.getParent();
            if (parent != null) {
                for (java.awt.Component c : parent.getComponents()) {
                    if (c instanceof javax.swing.JLabel) {
                        ((javax.swing.JLabel) c).setText(val + "%");
                    }
                }
            }
            volumeSlider.setToolTipText("Volume: " + val + "%");
        });
        
        playlistList.setModel(controller.getMusicModel());
        selectNavItem(navMusicLabel);

        playlistList.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(javax.swing.JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                String full = value == null ? "" : value.toString();
                String name = full.isEmpty() ? "" : new File(full).getName();
                name = controller.stripExtension(name);
                JLabel lbl = (JLabel) super.getListCellRendererComponent(list, name, index, isSelected, cellHasFocus);
                lbl.setToolTipText(full);
                return lbl;
            }
        });
        
        playlistList.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                if (evt.getClickCount() == 2) {
                    controller.playSelectedTrack();
                }
            }
        });

        if (!"Guest".equals(currentUserName)) {
            controller.loadPlaylistFromFile();
        }
        
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (!"Guest".equals(currentUserName)) {
                    controller.saveCurrentPlaylist();
                }
                dispose();
            }
        });

    }
    

    @Override
    public void actionPerformed(ActionEvent evnt) {

        if (evnt.getSource() == pauseBtn) 
        {
            controller.pauseMusic();
        } 
        else if (evnt.getSource() == chooseBtn) 
        {
            controller.chooseFile();
        } 
        else if (evnt.getSource() == loopBtn) 
        {
            controller.toggleLoop();
        }
        else if (evnt.getSource() == stopBtn) {
            controller.stopMusic();
        }
        else if (evnt.getSource() == nextBtn) {
            controller.nextTrack();
        }
        else if (evnt.getSource() == prevBtn) {
            controller.previousTrack();
        }
        else if (evnt.getSource() == addBtn)
        {
            controller.addToPlaylist();
        }
        else if (evnt.getSource() == removeBtn)
        {
            controller.removeFromPlaylist();
        }
        else if (evnt.getSource() == saveBtn)
        {
             String playlistName = JOptionPane.showInputDialog(
                this,
                "Enter a name for this playlist:",
                "Save Playlist",
                JOptionPane.QUESTION_MESSAGE
             );
            
            if (playlistName != null && !playlistName.trim().isEmpty()) {
                controller.savePlaylistAsNew(playlistName);
            }
        }
        else if (evnt.getSource() == removePlaylistBtn)
        {
            controller.removeSelectedPlaylist();
        }
    }
    
    @Override
    public void updateWelcomeMessage() {
        super.updateWelcomeMessage();
    }

}