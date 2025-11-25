import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import java.io.File;
import java.io.FileWriter;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineListener;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.util.Map;
import javax.sound.sampled.AudioFileFormat;

public class MusicPlayerManager {
    
    private MusicPlayerUI ui;
    
    protected boolean isPaused = false;
    protected boolean isLooping = false;
    protected File musicDir;
    protected DefaultListModel<String> musicModel;

    protected Clip clip;
    protected String currentFilePath;
    protected String currentUserName = "Guest";
    protected Map<String, DefaultListModel<String>> savedPlaylists;
    protected float currentVolume = 0.7f;
    
    public MusicPlayerManager(MusicPlayerUI ui) {
        this.ui = ui;
        
        musicDir = new File("music");
        if (!musicDir.exists()) {
            musicDir.mkdirs();
        }

        ui.fileChooser = new JFileChooser(musicDir);
        ui.fileChooser.setFileFilter(new FileNameExtensionFilter("WAV Files", "wav"));
        
        musicModel = new DefaultListModel<>();
        loadMusicLibrary();
    }

    private javax.swing.JLabel findNavLabel(String name) {
        if (ui == null || ui.navList == null || name == null) return null;
        for (int i = 0; i < ui.navList.getComponentCount(); i++) {
            java.awt.Component comp = ui.navList.getComponent(i);
            if (comp instanceof javax.swing.JLabel) {
                if (name.equals(((javax.swing.JLabel) comp).getText())) {
                    return (javax.swing.JLabel) comp;
                }
            }
        }
        return null;
    }

    private void registerSavedPlaylist(String name, DefaultListModel<String> model) {
        if (name == null || name.trim().isEmpty() || model == null) return;
        name = name.trim();
        if (ui.savedPlaylists == null) {
            ui.savedPlaylists = new java.util.HashMap<>();
        }
        ui.savedPlaylists.put(name, model);

        javax.swing.JLabel lbl = findNavLabel(name);
        if (lbl == null) {
            javax.swing.JLabel newPlaylistLabel = ui.createNavItem(name);
            ui.navList.add(newPlaylistLabel);
            ui.navList.revalidate();
            ui.navList.repaint();
        }
    }
    
    private void loadMusicLibrary() {
        File[] autoFiles = musicDir.listFiles((d, fname) -> fname.toLowerCase().endsWith(".wav"));
        if (autoFiles != null) {
            for (File f : autoFiles) {
                musicModel.addElement(f.getAbsolutePath());
            }
        }
    }
    
    public void setCurrentUserName(String name) {
        currentUserName = name;

        if ("Guest".equals(name)) {
            if (ui.addBtn != null) ui.addBtn.setEnabled(false);
            if (ui.createPlaylistLabel != null) {
                ui.createPlaylistLabel.setEnabled(false);
                ui.createPlaylistLabel.setForeground(new java.awt.Color(140, 140, 140));
                ui.createPlaylistLabel.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
            }
        } else {
            if (ui.addBtn != null) ui.addBtn.setEnabled(true);
            if (ui.createPlaylistLabel != null) {
                ui.createPlaylistLabel.setEnabled(true);
                ui.createPlaylistLabel.setForeground(MusicPlayerUI.Accent_CLR.darker());
                ui.createPlaylistLabel.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
            }
            loadPlaylistFromFile();
        }
    }

    
    public void createPlaylist(String playlistName) {
        if (playlistName == null || playlistName.trim().isEmpty()) return;
        if ("Guest".equals(currentUserName)) {
            ui.showStatus("Guest users cannot create playlists", 3000);
            return;
        }

        playlistName = playlistName.trim();
        if (ui.savedPlaylists == null) {
            ui.savedPlaylists = new java.util.HashMap<>();
        }

        if (ui.savedPlaylists.containsKey(playlistName)) {
            int result = javax.swing.JOptionPane.showConfirmDialog(
                ui,
                "A playlist with this name already exists. Overwrite?",
                "Playlist Exists",
                javax.swing.JOptionPane.YES_NO_OPTION
            );
            if (result != javax.swing.JOptionPane.YES_OPTION) {
                return;
            }
        }

        DefaultListModel<String> newPlaylist = new DefaultListModel<>();
        registerSavedPlaylist(playlistName, newPlaylist);
        saveCurrentPlaylist();
        ui.showStatus("Playlist '" + playlistName + "' created", 2000);
    }

    private Clip openClip(File file) throws Exception {
        AudioInputStream audioIn = AudioSystem.getAudioInputStream(file);
        javax.sound.sampled.AudioFormat baseFormat = audioIn.getFormat();
        javax.sound.sampled.AudioFormat decodeFormat = new javax.sound.sampled.AudioFormat(
                javax.sound.sampled.AudioFormat.Encoding.PCM_SIGNED,
                baseFormat.getSampleRate(),
                16,
                baseFormat.getChannels(),
                baseFormat.getChannels() * 2,
                baseFormat.getSampleRate(),
                false);

        AudioInputStream din = AudioSystem.getAudioInputStream(decodeFormat, audioIn);
        Clip newClip = AudioSystem.getClip();
        newClip.open(din);
        try {
            if (newClip.isControlSupported(javax.sound.sampled.FloatControl.Type.MASTER_GAIN)) {
                javax.sound.sampled.FloatControl fc = (javax.sound.sampled.FloatControl) newClip.getControl(javax.sound.sampled.FloatControl.Type.MASTER_GAIN);
                float defaultDb = -6.0f;
                if (fc.getValue() < defaultDb) defaultDb = fc.getMinimum();
                if (fc.getValue() > fc.getMaximum()) defaultDb = fc.getMaximum();
                fc.setValue(defaultDb);
            }
        } catch (Exception ex) {
        }

        return newClip;
    }

    public void stopMusic() {
        if (clip != null) {
            clip.stop();
            clip.setFramePosition(0);
            isPaused = false;
            ui.stopProgressTimer();
            ui.showStatus("Stopped", 1000);
        }
    }

    public void nextTrack() {
        DefaultListModel<String> model = (DefaultListModel<String>) ui.playlistList.getModel();
        int idx = -1;
        if (currentFilePath != null) idx = model.indexOf(currentFilePath);
        if (idx < 0) idx = ui.playlistList.getSelectedIndex();
        int next = idx + 1;
        if (next >= model.getSize()) next = 0;
        if (model.getSize() > 0) {
            String path = model.get(next);
            File f = new File(path);
            if (f.exists()) playFile(f);
        }
    }

    public void previousTrack() {
        DefaultListModel<String> model = (DefaultListModel<String>) ui.playlistList.getModel();
        int idx = -1;
        if (currentFilePath != null) idx = model.indexOf(currentFilePath);
        if (idx < 0) idx = ui.playlistList.getSelectedIndex();
        int prev = idx - 1;
        if (prev < 0) prev = model.getSize() - 1;
        if (model.getSize() > 0) {
            String path = model.get(prev);
            File f = new File(path);
            if (f.exists()) playFile(f);
        }
    }

    public void setVolume(float level) {
        
        currentVolume = Math.max(0.0f, Math.min(1.0f, level));
        try {
            if (clip != null && clip.isControlSupported(javax.sound.sampled.FloatControl.Type.MASTER_GAIN)) {
                javax.sound.sampled.FloatControl gainControl = (javax.sound.sampled.FloatControl) clip.getControl(javax.sound.sampled.FloatControl.Type.MASTER_GAIN);
                float min = gainControl.getMinimum();
                float max = gainControl.getMaximum();

                
                float v = Math.max(0.0001f, currentVolume);
                float perceptual = (float) Math.pow(v, 1.6); 
                float dB = (float) (20.0 * Math.log10(perceptual));
                if (dB < min) dB = min;
                if (dB > max) dB = max;
                gainControl.setValue(dB);
            }
        } catch (Exception ex) {
        }
    }
    
    public void handleNavItemSelected(String text) {
        if (ui.navMusicLabel != null && text.equals(ui.navMusicLabel.getText())) {
            ui.playlistList.setModel(musicModel);
        } else if (ui.savedPlaylists != null && ui.savedPlaylists.containsKey(text)) {
            ui.playlistList.setModel(ui.savedPlaylists.get(text));
        } else {
            ui.playlistList.setModel(musicModel);
        }

        ui.playlistList.clearSelection();

        boolean isCustomPlaylist = (ui.savedPlaylists != null && ui.savedPlaylists.containsKey(text));
        ui.removePlaylistBtn.setVisible(isCustomPlaylist);
    }
    
    public String stripExtension(String filename) {
        if (filename == null) return "";
        int idx = filename.lastIndexOf('.');
        if (idx > 0) {
            return filename.substring(0, idx);
        }
        return filename;
    }

    public void playMusic() {
        String path = currentFilePath;
        if (path == null || path.isEmpty()) {
            path = ui.textField1.getText(); 
        }
        if (path == null || path.isEmpty()) {
            ui.showStatus("No file selected. Please select a file or choose one from the playlist.", 3000);
            return;
        }
        
        File file = new File(path);
        if (!file.exists() && !file.isAbsolute()) {
            file = new File(musicDir, path);
        }

        if (!file.exists()) {
            ui.showStatus("File not found: " + file.getName(), 3000);
            return;
        }
        
        playFile(file);
    }
    
    public void playFile(File file) {
        if (clip != null && clip.isRunning()) 
        {
            clip.stop();
        }
        
        ui.stopProgressTimer();
        isPaused = false;
        
        try 
        {
            if (clip != null) {
                clip.close();
            }
            clip = openClip(file);

            
            setVolume(currentVolume);
            
            clip.addLineListener(new LineListener() {
                @Override
                public void update(LineEvent event) {
                    if (event.getType() == LineEvent.Type.STOP) {
                        if (!isPaused && !isLooping) {
                            ui.stopProgressTimer();
                            if (ui.progressBar != null) {
                                ui.progressBar.setValue(100);
                                long length = clip.getMicrosecondLength();
                                if (length > 0) {
                                    String totalTime = ui.formatTime(length / 1000000);
                                    ui.progressBar.setString(totalTime + " / " + totalTime);
                                }
                            }
                            isPaused = false;
                        } else if (isLooping && clip.isRunning()) {

                        }
                    } else if (event.getType() == LineEvent.Type.START && isLooping) {
                        if (ui.progressTimer == null || !ui.progressTimer.isRunning()) {
                            ui.startProgressTimer();
                        }
                    }
                }
            });
            
            currentFilePath = file.getAbsolutePath();
            ui.textField1.setText(stripExtension(file.getName()));
            updateNowPlayingInfo(file);
            
            clip.start();
            
            if (isLooping) 
            {
                clip.loop(Clip.LOOP_CONTINUOUSLY);
            }
            
            ui.startProgressTimer();
            ui.showStatus("Now playing: " + stripExtension(file.getName()), 2000);
 
        }
        catch(Exception e)
        {
            System.out.println(e);
            ui.showStatus("Error playing file: " + e.getMessage(), 3000);
        }
    }

    public void pauseMusic() 
    {
        if (clip == null) {
            ui.showStatus("No music is playing", 2000);
            return;
        }
        
        if (clip.isRunning()) 
        {
            clip.stop();
            isPaused = true;
            ui.stopProgressTimer();
            ui.showStatus("Paused", 1500);
        } 
        else if (isPaused) 
        {
            clip.start();
            
            if(isLooping)
            {
                clip.loop(Clip.LOOP_CONTINUOUSLY);
            }
            
            isPaused = false;
            ui.startProgressTimer();
            ui.showStatus("Resumed", 1500);
        } else {
            ui.showStatus("No music is playing", 2000);
        }
    }
    
    public void chooseFile() 
    {
        ui.fileChooser.setCurrentDirectory(musicDir);
        int result = ui.fileChooser.showOpenDialog(ui);
        if (result == JFileChooser.APPROVE_OPTION) 
        {
            File selectedFile = ui.fileChooser.getSelectedFile();
            currentFilePath = selectedFile.getAbsolutePath();
            ui.textField1.setText(stripExtension(selectedFile.getName()));
            updateNowPlayingInfo(selectedFile);
            
            String absPath = selectedFile.getAbsolutePath();

            if (musicModel.indexOf(absPath) == -1) {
                musicModel.addElement(absPath);
            }

            if (!"Guest".equals(currentUserName)) {

                if (ui.playlistModel.indexOf(absPath) == -1) {
                    ui.playlistModel.addElement(absPath);
                    ui.showStatus("File selected and added to your playlist", 2000);
                } else {
                    ui.showStatus("File selected (already in your playlist)", 2000);
                }
            } else {
                ui.showStatus("File selected (playlist features are disabled for guests)", 2000);
            }
        }
    }
    
    public void addToPlaylist()
    {
        if ("Guest".equals(currentUserName)) {
            ui.showStatus("Guest users cannot use playlist features.", 3000);
            return;
        }
        DefaultListModel<String> currentListModel = (DefaultListModel<String>) ui.playlistList.getModel();

        String path = null;
        if (currentListModel == musicModel) {
            int sel = ui.playlistList.getSelectedIndex();
            if (sel >= 0) {
                path = musicModel.get(sel);
            }
        }

        if (path == null || path.isEmpty()) {
            path = currentFilePath;
        }

        if (path == null || path.isEmpty()) {
            ui.showStatus("No file selected. Please browse or select from Music library.", 3000);
            return;
        }

        File f = new File(path);
        if (!f.isAbsolute()) {
            f = new File(musicDir, path);
        }
        if (!f.exists()) {
            ui.showStatus("File not found", 2000);
            return;
        }

        final String absPath = f.getAbsolutePath();

        java.util.List<String> options = new java.util.ArrayList<>();

        
        
        if (ui.savedPlaylists != null) {
            for (Map.Entry<String, DefaultListModel<String>> e : ui.savedPlaylists.entrySet()) {
                String k = e.getKey();
                if (k != null && !k.trim().isEmpty()) {
                    options.add(k.trim());
                }
            }
        }

        if (options.isEmpty()) {
            javax.swing.JOptionPane.showMessageDialog(
                ui,
                "No playlists found. Please create a playlist first using 'Create Playlist +'.",
                "No Playlists",
                javax.swing.JOptionPane.INFORMATION_MESSAGE
            );
            return;
        }

        String[] arr = options.toArray(new String[0]);
        javax.swing.JComboBox<String> combo = new javax.swing.JComboBox<>(arr);
        int res = javax.swing.JOptionPane.showConfirmDialog(
            ui,
            combo,
            "Choose playlist to add to",
            javax.swing.JOptionPane.OK_CANCEL_OPTION,
            javax.swing.JOptionPane.QUESTION_MESSAGE
        );

        if (res != javax.swing.JOptionPane.OK_OPTION) return;

        String chosen = (String) combo.getSelectedItem();
        DefaultListModel<String> targetModel = null;
        if (chosen != null && ui.savedPlaylists != null) {
            targetModel = ui.savedPlaylists.get(chosen);
        }

        if (targetModel == null) {
            ui.showStatus("Selected playlist not found", 2000);
            return;
        }

        if (targetModel.indexOf(absPath) == -1) {
            targetModel.addElement(absPath);
            ui.showStatus("Added to playlist: " + chosen, 2000);
            saveCurrentPlaylist();
        } else {
            ui.showStatus("Already in the selected playlist", 2000);
        }
    }
    
    public void removeFromPlaylist()
    {
        DefaultListModel<String> currentModel = (DefaultListModel<String>) ui.playlistList.getModel();
        
        if (currentModel == musicModel) {
            ui.showStatus("Cannot remove from Music library. Switch to a custom playlist to remove items.", 3000);
            return;
        }
        
        int idx = ui.playlistList.getSelectedIndex();
        if (idx >= 0) {
            String removed = currentModel.get(idx);
            currentModel.remove(idx);
            ui.showStatus("Removed from playlist", 2000);
            
            if (currentFilePath != null && currentFilePath.equals(removed)) {
                if (clip != null && clip.isRunning()) {
                    clip.stop();
                }
                ui.stopProgressTimer();
                currentFilePath = null;
                ui.updateNowPlaying("No track selected", "Unknown Artist", null);
            }
        } else {
            ui.showStatus("Please select a track to remove", 2000);
        }
    }
    
    public void playSelectedTrack()
    {
        int idx = ui.playlistList.getSelectedIndex();
        if (idx >= 0) {
            String path;
            DefaultListModel<String> currentModel = (DefaultListModel<String>) ui.playlistList.getModel();
            
            path = currentModel.get(idx);
            
            File file = new File(path);
            if (!file.exists()) {
                ui.showStatus("File not found: " + file.getName(), 3000);
                return;
            }
            
            playFile(file);
        } else {
            ui.showStatus("Please select a track from the playlist", 2000);
        }
    }

    public void toggleLoop() 
    {
        if (clip == null) {
            ui.showStatus("No music is playing", 2000);
            return;
        }
        
        isLooping = !isLooping;
        if (isLooping) 
        {

            ui.loopBtn.setToolTipText("Loop enabled");
            ui.setLoopActive(true);
            
            if (clip.isRunning() || isPaused)
            {
                clip.loop(Clip.LOOP_CONTINUOUSLY);
            }
            ui.showStatus("Loop enabled", 1500);
        }
        else 
        {
            ui.loopBtn.setToolTipText("Loop");
            ui.setLoopActive(false);
 
            if (clip.isRunning() || isPaused)
            {
                clip.loop(0);
            }
            ui.showStatus("Loop disabled", 1500);
        }
    }
    
    public boolean isLooping() {
        return isLooping;
    }
    
    private void updateNowPlayingInfo(File file) {
        try {
            String title = extractTitle(file);
            String artist = extractArtist(file);
            ImageIcon albumArt = loadAlbumArt(file);
            
            ui.updateNowPlaying(title, artist, albumArt);
        } catch (Exception e) {
            String filename = stripExtension(file.getName());
            ui.updateNowPlaying(filename, "Unknown Artist", null);
        }
    }
    
    private String extractTitle(File file) {
        try {
            AudioFileFormat format = AudioSystem.getAudioFileFormat(file);
            if (format.properties() != null) {
                Object titleProp = format.properties().get("title");
                if (titleProp != null) {
                    return titleProp.toString();
                }
            }
        } catch (Exception e) {
        }
        
        String filename = stripExtension(file.getName());
        if (filename.contains(" - ")) {
            String[] parts = filename.split(" - ", 2);
            if (parts.length > 1) {
                return parts[1].trim();
            }
        }
        return filename;
    }
    
    private String extractArtist(File file) {
        try {
            AudioFileFormat format = AudioSystem.getAudioFileFormat(file);
            if (format.properties() != null) {
                Object artistProp = format.properties().get("author");
                if (artistProp != null) {
                    return artistProp.toString();
                }
            }
        } catch (Exception e) {
        }
        
        String filename = stripExtension(file.getName());
        if (filename.contains(" - ")) {
            String[] parts = filename.split(" - ", 2);
            if (parts.length > 0) {
                return parts[0].trim();
            }
        }
        return "Unknown Artist";
    }
    
    private ImageIcon loadAlbumArt(File audioFile) {
        try {
            String basePath = audioFile.getParent();
            String baseName = stripExtension(audioFile.getName());
            
            String[] imageExtensions = {".jpg", ".jpeg", ".png", ".gif", ".bmp"};
            for (String ext : imageExtensions) {
                File imageFile = new File(basePath, baseName + ext);
                if (imageFile.exists()) {
                    ImageIcon icon = new ImageIcon(imageFile.getAbsolutePath());
                    return icon;
                }
            }
            
            File coverFile = new File(basePath, "cover.jpg");
            if (coverFile.exists()) {
                ImageIcon icon = new ImageIcon(coverFile.getAbsolutePath());
                return icon;
            }
        } catch (Exception e) {
        }
        return null;
    }
    
    public void saveCurrentPlaylist() {
        try {
            File playlistFile = getUserPlaylistFile();
            if (playlistFile == null) {
                return;
            }

            FileWriter writer = new FileWriter(playlistFile);
            
            writer.write(currentUserName + "\n");
            
            
            if (ui.savedPlaylists != null) {
                for (Map.Entry<String, DefaultListModel<String>> entry : ui.savedPlaylists.entrySet()) {
                    writer.write("::PLAYLIST::" + entry.getKey() + "\n");
                    for (int i = 0; i < entry.getValue().getSize(); i++) {
                        writer.write(entry.getValue().get(i) + "\n");
                    }
                }
            }
            
            writer.close();
            System.out.println("All playlists saved to " + playlistFile.getName());
        } catch (IOException e) {
            System.err.println("Error saving playlists: " + e.getMessage());
        }
    }
    
    public void loadPlaylistFromFile() {
        try {
            File playlistFile = getUserPlaylistFile();
            if (playlistFile == null || !playlistFile.exists()) {
                return;
            }
            
            BufferedReader reader = new BufferedReader(new FileReader(playlistFile));
            
            String savedUsername = reader.readLine();
            if (savedUsername == null || !savedUsername.equals(currentUserName)) {
                reader.close();
                return;
            }
            
            ui.playlistModel.clear();
            if (ui.savedPlaylists != null) {
                ui.savedPlaylists.clear();
            } else {
                ui.savedPlaylists = new java.util.HashMap<>();
            }
            
            String line;
            DefaultListModel<String> currentModel = null;
            String playlistName = null;
            
            java.util.List<java.awt.Component> toRemove = new java.util.ArrayList<>();
            for (java.awt.Component comp : ui.navList.getComponents()) {
                if (comp instanceof javax.swing.JLabel) {
                    String text = ((javax.swing.JLabel) comp).getText();
                    if (!"Music".equals(text)) {
                        toRemove.add(comp);
                    }
                }
            }
            for (java.awt.Component rem : toRemove) {
                ui.navList.remove(rem);
            }
            
            int loadedTracksCount = 0;
            
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                
                if (line.startsWith("::PLAYLIST::")) {
                    playlistName = line.substring("::PLAYLIST::".length()).trim();
                        currentModel = new DefaultListModel<>();
                        ui.savedPlaylists.put(playlistName, currentModel);
                        javax.swing.JLabel newPlaylistLabel = ui.createNavItem(playlistName);
                        ui.navList.add(newPlaylistLabel);
                } else if (!line.isEmpty() && currentModel != null) {
                    File f = new File(line);
                    if (f.exists()) {
                        currentModel.addElement(line);
                        if (currentModel == ui.playlistModel) {
                            loadedTracksCount++;
                        }
                    }
                }
            }
            
            reader.close();
            ui.navList.revalidate();
            ui.navList.repaint();
            
            if (loadedTracksCount > 0) {
                ui.showStatus("Loaded " + loadedTracksCount + " tracks from your default playlist", 3000);
            }
        } catch (IOException e) {
            System.err.println("Error loading playlists: " + e.getMessage());
        }
    }
    
    private File getUserPlaylistFile() {
        if (currentUserName == null || currentUserName.trim().isEmpty() || "Guest".equals(currentUserName)) {
            return null;
        }
        String safeName = currentUserName.trim().replaceAll("[^a-zA-Z0-9-_]", "_");
        if (safeName.isEmpty()) {
            safeName = "user";
        }
        File dir = new File("data/usercustomplaylist");
        if (!dir.exists()) {
            dir.mkdirs();
        }
        return new File(dir, "playlists_" + safeName + ".txt");
    }
    
    public void savePlaylistAsNew(String playlistName) {
        DefaultListModel<String> currentModel = (DefaultListModel<String>) ui.playlistList.getModel();
        
        if (currentModel == musicModel) {
            ui.showStatus("Cannot save the Music library. Switch to your default playlist first.", 2000);
            return;
        }
        
        if (currentModel.getSize() == 0) {
            ui.showStatus("Current playlist is empty. Add some tracks first.", 2000);
            return;
        }
        
        if (playlistName == null || playlistName.trim().isEmpty()) {
            return;
        }
        
        playlistName = playlistName.trim();

        String defaultName = currentUserName + "'s Playlist";
        if (playlistName.equals(defaultName)) {
            ui.showStatus("Cannot use reserved playlist name", 2000);
            return;
        }

        if (ui.savedPlaylists == null) {
            ui.savedPlaylists = new java.util.HashMap<>();
        }

        if (ui.savedPlaylists.containsKey(playlistName)) {
            int result = javax.swing.JOptionPane.showConfirmDialog(
                ui,
                "A playlist with this name already exists. Overwrite?",
                "Playlist Exists",
                javax.swing.JOptionPane.YES_NO_OPTION
            );
            if (result != javax.swing.JOptionPane.YES_OPTION) {
                return;
            }
        }

        DefaultListModel<String> newPlaylist = new DefaultListModel<>();
        for (int i = 0; i < currentModel.getSize(); i++) {
            newPlaylist.addElement(currentModel.get(i));
        }

        registerSavedPlaylist(playlistName, newPlaylist);

        saveCurrentPlaylist();
        ui.showStatus("Playlist '" + playlistName + "' saved successfully", 2000);
    }
    
    public void removeSelectedPlaylist() {
        if (ui.selectedNavItem == null) {
            ui.showStatus("Please select a playlist to remove", 2000);
            return;
        }
        
        String playlistName = ui.selectedNavItem.getText();
        
        if (playlistName.equals("Music")) {
            ui.showStatus("Cannot remove the Music library", 2000);
            return;
        }
        
        
        
        if (ui.savedPlaylists.containsKey(playlistName)) {
            int result = javax.swing.JOptionPane.showConfirmDialog(
                ui,
                "Are you sure you want to remove the playlist '" + playlistName + "'? This action is permanent.",
                "Remove Playlist",
                javax.swing.JOptionPane.YES_NO_OPTION
            );
            
            if (result == javax.swing.JOptionPane.YES_OPTION) {
                DefaultListModel<String> playlistToRemove = ui.savedPlaylists.get(playlistName);
                boolean isCurrentlyViewed = (ui.playlistList.getModel() == playlistToRemove);
                
                ui.savedPlaylists.remove(playlistName);
                ui.navList.remove(ui.selectedNavItem);
                ui.navList.revalidate();
                ui.navList.repaint();
                
                if (isCurrentlyViewed) {
                    ui.playlistList.setModel(musicModel);
                    ui.selectNavItem(ui.navMusicLabel);
                }
                
                ui.selectedNavItem = null;
                saveCurrentPlaylist();
                ui.showStatus("Playlist '" + playlistName + "' removed", 2000);
            }
        } else {
            ui.showStatus("Playlist not found", 2000);
        }
    }
    
    public Clip getClip() {
        return clip;
    }
    
    public DefaultListModel<String> getMusicModel() {
        return musicModel;
    }
    
    public String getCurrentFilePath() {
        return currentFilePath;
    }
}