import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.Hashtable;

public class MusicPlayerGUI extends JFrame {
    public static final Color FRAME_COLOR = Color.decode("#8ACE00");
    public static final Color TEXT_COLOR = Color.WHITE;

    private MusicPlayer musicPlayer;

    private JFileChooser jFileChooser;

    private JLabel songTitle, songArtist, songImageLabel;
    private JPanel playbackBtns;
    private JSlider playbackSlider;

    public MusicPlayerGUI(){
        super("Music Player");

        setSize(400, 600);

        setDefaultCloseOperation(EXIT_ON_CLOSE);

        setLocationRelativeTo(null);

        setResizable(false);

        setLayout(null);

        // Hintergrundfarbe ändern
        getContentPane().setBackground(FRAME_COLOR);

        musicPlayer = new MusicPlayer(this);
        jFileChooser = new JFileChooser();

        jFileChooser.setCurrentDirectory(new File("src/assets/Songs"));

        jFileChooser.setFileFilter(new FileNameExtensionFilter("MP3", "mp3"));

        addGuiComponents();
    }

    private void addGuiComponents(){
        addToolbar();

        // Cover Bild Label hinzufügen
        songImageLabel = new JLabel();
        songImageLabel.setBounds(50, 50, 300, 300); // Quadrat: 300x300
        songImageLabel.setHorizontalAlignment(SwingConstants.CENTER);
        songImageLabel.setBorder(new LineBorder(Color.BLACK, 2)); 
        updateSongImage(null); 
        add(songImageLabel);

        // Song Titel
        songTitle = new JLabel("Song Title");
        songTitle.setBounds(0, 360, getWidth() - 10, 30);
        songTitle.setFont(new Font("Dialog", Font.BOLD, 24));
        songTitle.setForeground(TEXT_COLOR);
        songTitle.setHorizontalAlignment(SwingConstants.CENTER);
        add(songTitle);

        // Song Künstler
        songArtist = new JLabel("Artist");
        songArtist.setBounds(0, 390, getWidth() - 10, 30);
        songArtist.setFont(new Font("Dialog", Font.PLAIN, 24));
        songArtist.setForeground(TEXT_COLOR);
        songArtist.setHorizontalAlignment(SwingConstants.CENTER);
        add(songArtist);

        // Playback Slider
        playbackSlider = new JSlider(JSlider.HORIZONTAL, 0, 100, 0);
        playbackSlider.setBounds(getWidth()/2 - 150, 450, 300, 40);
        playbackSlider.setBackground(null);
        playbackSlider.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                musicPlayer.pauseSong();
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                
                JSlider source = (JSlider) e.getSource();

                int frame = source.getValue();

                musicPlayer.setCurrentFrame(frame);

                musicPlayer.setCurrentTimeInMilli((int) (frame / (2.08 * musicPlayer.getCurrentSong().getFrameRatePerMilliseconds())));

                musicPlayer.playCurrentSong();

                enablePauseButtonDisablePlayButton();
            }
        });
        add(playbackSlider);

        addPlaybackBtns();
    }

    private void addToolbar(){
        JToolBar toolBar = new JToolBar();
        toolBar.setBounds(0, 0, getWidth(), 20);

        toolBar.setFloatable(false);

        // Dropdown-Menü hinzufügen
        JMenuBar menuBar = new JMenuBar();
        toolBar.add(menuBar);

        // "Song" Menü hinzufügen
        JMenu songMenu = new JMenu("Song");
        menuBar.add(songMenu);

        JMenuItem loadSong = new JMenuItem("Load Song");
        loadSong.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int result = jFileChooser.showOpenDialog(MusicPlayerGUI.this);
                File selectedFile = jFileChooser.getSelectedFile();

                if(result == JFileChooser.APPROVE_OPTION && selectedFile != null){
                    Song song = new Song(selectedFile.getPath());

                    musicPlayer.loadSong(song);

                    updateSongTitleAndArtist(song);

                    updatePlaybackSlider(song);

                    updateSongImage(song.getCoverArt());

                    enablePauseButtonDisablePlayButton();
                }
            }
        });
        songMenu.add(loadSong);

        JMenu playlistMenu = new JMenu("Playlist");
        menuBar.add(playlistMenu);

        JMenuItem createPlaylist = new JMenuItem("Create Playlist");
        createPlaylist.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new MusicPlaylistDialog(MusicPlayerGUI.this).setVisible(true);
            }
        });
        playlistMenu.add(createPlaylist);

        JMenuItem loadPlaylist = new JMenuItem("Load Playlist");
        loadPlaylist.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser jFileChooser = new JFileChooser();
                jFileChooser.setFileFilter(new FileNameExtensionFilter("Playlist", "txt"));
                jFileChooser.setCurrentDirectory(new File("src/assets"));

                int result = jFileChooser.showOpenDialog(MusicPlayerGUI.this);
                File selectedFile = jFileChooser.getSelectedFile();

                if(result == JFileChooser.APPROVE_OPTION && selectedFile != null){
                    musicPlayer.stopSong();

                    musicPlayer.loadPlaylist(selectedFile);

                    Song currentSong = musicPlayer.getCurrentSong();
                    if(currentSong != null){
                        updateSongImage(currentSong.getCoverArt());
                        updateSongTitleAndArtist(currentSong);
                        updatePlaybackSlider(currentSong);
                    }
                }
            }
        });
        playlistMenu.add(loadPlaylist);

        JMenu coverMenu = new JMenu("Cover");
        menuBar.add(coverMenu);

        JMenuItem uploadCover = new JMenuItem("Upload Cover");
        uploadCover.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser coverFileChooser = new JFileChooser();
                coverFileChooser.setFileFilter(new FileNameExtensionFilter("Image Files", ImageIO.getReaderFileSuffixes()));
                coverFileChooser.setCurrentDirectory(new File("src/assets/Cover"));
                int result = coverFileChooser.showOpenDialog(MusicPlayerGUI.this);

                File selectedFile = coverFileChooser.getSelectedFile();
                if(result == JFileChooser.APPROVE_OPTION && selectedFile != null){
                    try{
                        File coverDir = new File("src/assets/Cover");
                        if(!coverDir.exists()){
                            coverDir.mkdirs();
                        }

                        String fileName = "cover_" + System.currentTimeMillis() + getFileExtension(selectedFile);
                        File destinationFile = new File(coverDir, fileName);

                        copyFile(selectedFile, destinationFile);

                        JOptionPane.showMessageDialog(MusicPlayerGUI.this, "Cover erfolgreich hochgeladen!");

                    } catch(Exception ex){
                        ex.printStackTrace();
                        JOptionPane.showMessageDialog(MusicPlayerGUI.this, "Fehler beim Hochladen des Covers.", "Fehler", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        });
        coverMenu.add(uploadCover);

        JMenuItem assignCover = new JMenuItem("Assign Cover to Current Song");
        assignCover.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Song currentSong = musicPlayer.getCurrentSong();
                if(currentSong == null){
                    JOptionPane.showMessageDialog(MusicPlayerGUI.this, "Bitte lade zuerst einen Song.", "Kein Song geladen", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                File coverDir = new File("src/assets/Cover");
                if(!coverDir.exists() || !coverDir.isDirectory()){
                    JOptionPane.showMessageDialog(MusicPlayerGUI.this, "Keine Coverbilder gefunden. Bitte lade ein Cover hoch.", "Keine Cover gefunden", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                File[] coverFiles = coverDir.listFiles(new FilenameFilter() {
                    @Override
                    public boolean accept(File dir, String name) {
                        String lowerName = name.toLowerCase();
                        return lowerName.endsWith(".png") || lowerName.endsWith(".jpg") || lowerName.endsWith(".jpeg") || lowerName.endsWith(".gif");
                    }
                });

                if(coverFiles == null || coverFiles.length == 0){
                    JOptionPane.showMessageDialog(MusicPlayerGUI.this, "Keine Coverbilder gefunden. Bitte lade ein Cover hoch.", "Keine Cover gefunden", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                String[] coverNames = new String[coverFiles.length];
                for(int i = 0; i < coverFiles.length; i++){
                    coverNames[i] = coverFiles[i].getName();
                }

                String selectedCover = (String) JOptionPane.showInputDialog(
                        MusicPlayerGUI.this,
                        "Wähle ein Coverbild aus:",
                        "Cover zuweisen",
                        JOptionPane.PLAIN_MESSAGE,
                        null,
                        coverNames,
                        coverNames[0]);

                if(selectedCover != null){
                    String coverPath = "src/assets/Cover/" + selectedCover;
                    currentSong.setCustomCoverPath(coverPath);
                    updateSongImage(currentSong.getCoverArt());
                    JOptionPane.showMessageDialog(MusicPlayerGUI.this, "Cover erfolgreich zugewiesen!");
                }
            }
        });
        coverMenu.add(assignCover);

        add(toolBar);
    }

    private void addPlaybackBtns(){
        playbackBtns = new JPanel();
        playbackBtns.setBounds(0, 500, getWidth() - 10, 80); 
        playbackBtns.setBackground(null);

        JButton prevButton = new JButton(loadImage("src/assets/Images/previous.png"));
        prevButton.setBorderPainted(false);
        prevButton.setBackground(null);
        prevButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                musicPlayer.prevSong();

                Song currentSong = musicPlayer.getCurrentSong();
                if(currentSong != null){
                    updateSongImage(currentSong.getCoverArt());
                    updateSongTitleAndArtist(currentSong);
                    updatePlaybackSlider(currentSong);
                }
            }
        });
        playbackBtns.add(prevButton);

        // Play Button
        JButton playButton = new JButton(loadImage("src/assets/Images/play.png"));
        playButton.setBorderPainted(false);
        playButton.setBackground(null);
        playButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                enablePauseButtonDisablePlayButton();

                musicPlayer.playCurrentSong();
            }
        });
        playbackBtns.add(playButton);

        // Pause Button
        JButton pauseButton = new JButton(loadImage("src/assets/Images/pause.png"));
        pauseButton.setBorderPainted(false);
        pauseButton.setBackground(null);
        pauseButton.setVisible(false);
        pauseButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                enablePlayButtonDisablePauseButton();

                musicPlayer.pauseSong();
            }
        });
        playbackBtns.add(pauseButton);

        JButton nextButton = new JButton(loadImage("src/assets/Images/next.png"));
        nextButton.setBorderPainted(false);
        nextButton.setBackground(null);
        nextButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                musicPlayer.nextSong();

                Song currentSong = musicPlayer.getCurrentSong();
                if(currentSong != null){
                    updateSongImage(currentSong.getCoverArt());
                    updateSongTitleAndArtist(currentSong);
                    updatePlaybackSlider(currentSong);
                }
            }
        });
        playbackBtns.add(nextButton);

        add(playbackBtns);
    }

    public void setPlaybackSliderValue(int frame){
        playbackSlider.setValue(frame);
    }

    public void updateSongTitleAndArtist(Song song){
        songTitle.setText(song.getSongTitle());
        songArtist.setText(song.getSongArtist());
    }

    public void updatePlaybackSlider(Song song){
        playbackSlider.setMaximum(song.getMp3File().getFrameCount());

        Hashtable<Integer, JLabel> labelTable = new Hashtable<>();

        JLabel labelBeginning = new JLabel("00:00");
        labelBeginning.setFont(new Font("Dialog", Font.BOLD, 18));
        labelBeginning.setForeground(TEXT_COLOR);

        JLabel labelEnd =  new JLabel(song.getSongLength());
        labelEnd.setFont(new Font("Dialog", Font.BOLD, 18));
        labelEnd.setForeground(TEXT_COLOR);

        labelTable.put(0, labelBeginning);
        labelTable.put(song.getMp3File().getFrameCount(), labelEnd);

        playbackSlider.setLabelTable(labelTable);
        playbackSlider.setPaintLabels(true);
    }

    public void enablePauseButtonDisablePlayButton(){
        JButton playButton = (JButton) playbackBtns.getComponent(1);
        JButton pauseButton = (JButton) playbackBtns.getComponent(2);

        playButton.setVisible(false);
        playButton.setEnabled(false);

        pauseButton.setVisible(true);
        pauseButton.setEnabled(true);
    }

    public void enablePlayButtonDisablePauseButton(){
        JButton playButton = (JButton) playbackBtns.getComponent(1);
        JButton pauseButton = (JButton) playbackBtns.getComponent(2);

        playButton.setVisible(true);
        playButton.setEnabled(true);

        pauseButton.setVisible(false);
        pauseButton.setEnabled(false);
    }

    private ImageIcon loadImage(String imagePath){
        try{
            BufferedImage image = ImageIO.read(new File(imagePath));

            // Bild skalieren, um den Button zu füllen
            Image scaledImage = image.getScaledInstance(50, 50, Image.SCALE_SMOOTH);
            return new ImageIcon(scaledImage);
        } catch(Exception e){
            e.printStackTrace();
        }

        return null;
    }

    private void updateSongImage(BufferedImage coverArt){
        BufferedImage squareImage = null;
        if(coverArt != null){
            // Bild quadratisch zuschneiden
            squareImage = cropToSquare(coverArt);

            Image scaledImage = squareImage.getScaledInstance(songImageLabel.getWidth(), songImageLabel.getHeight(), Image.SCALE_SMOOTH);
            songImageLabel.setIcon(new ImageIcon(scaledImage));
        } else {
            ImageIcon defaultIcon = loadImage("src/assets/Images/record.png");
            if(defaultIcon != null){
                Image image = defaultIcon.getImage();
                BufferedImage bufferedDefault = toBufferedImage(image);
                squareImage = cropToSquare(bufferedDefault);
                Image scaledImage = squareImage.getScaledInstance(songImageLabel.getWidth(), songImageLabel.getHeight(), Image.SCALE_SMOOTH);
                songImageLabel.setIcon(new ImageIcon(scaledImage));
            }
        }
    }

    private BufferedImage cropToSquare(BufferedImage src){
        int size = Math.min(src.getWidth(), src.getHeight());
        int x = (src.getWidth() - size) / 2;
        int y = (src.getHeight() - size) / 2;
        return src.getSubimage(x, y, size, size);
    }

    private BufferedImage toBufferedImage(Image img){
        if (img instanceof BufferedImage){
            return (BufferedImage) img;
        }

        BufferedImage bimage = new BufferedImage(
                img.getWidth(null), img.getHeight(null), BufferedImage.TYPE_INT_ARGB);

        Graphics2D bGr = bimage.createGraphics();
        bGr.drawImage(img, 0, 0, null);
        bGr.dispose();

        return bimage;
    }

    private void copyFile(File source, File dest) throws IOException{
        try (InputStream is = new FileInputStream(source);
             OutputStream os = new FileOutputStream(dest)) {
            byte[] buffer = new byte[1024];
            int length;
            while((length = is.read(buffer)) > 0){
                os.write(buffer, 0, length);
            }
        }
    }

    private String getFileExtension(File file){
        String name = file.getName();
        int lastIndex = name.lastIndexOf('.');
        if(lastIndex > 0 && lastIndex < name.length() - 1){
            return name.substring(lastIndex);
        }
        return "";
    }
}
