import com.mpatric.mp3agic.Mp3File;
import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import javax.imageio.ImageIO;

public class Song {
    private String songTitle;
    private String songArtist;
    private String songLength;
    private String filePath;
    private Mp3File mp3File;
    private double frameRatePerMilliseconds;
    private BufferedImage coverArt;
    private String customCoverPath; 

    public Song(String filePath){
        this.filePath = filePath;
        this.customCoverPath = null; 
        try{
            mp3File = new Mp3File(filePath);
            frameRatePerMilliseconds = (double) mp3File.getFrameCount() / mp3File.getLengthInMilliseconds();
            songLength = convertToSongLengthFormat();

            AudioFile audioFile = AudioFileIO.read(new File(filePath));

            // Lesen der Metadaten der Audiodatei
            Tag tag =  audioFile.getTag();
            if(tag != null){
                songTitle = tag.getFirst(FieldKey.TITLE);
                songArtist = tag.getFirst(FieldKey.ARTIST);
                // Abrufen des Cover Arts, falls verfügbar
                if(tag.getFirstArtwork() != null){
                    byte[] imageData = tag.getFirstArtwork().getBinaryData();
                    coverArt = ImageIO.read(new ByteArrayInputStream(imageData));
                }
            } else {
                songTitle = "N/A";
                songArtist = "N/A";
            }
        } catch(Exception e){
            e.printStackTrace();
        }
    }

    private String convertToSongLengthFormat(){
        long minutes = mp3File.getLengthInSeconds() / 60;
        long seconds = mp3File.getLengthInSeconds() % 60;
        String formattedTime = String.format("%02d:%02d", minutes, seconds);
        return formattedTime;
    }

    public String getSongTitle() {
        return songTitle;
    }

    public String getSongArtist() {
        return songArtist;
    }

    public String getSongLength() {
        return songLength;
    }

    public String getFilePath() {
        return filePath;
    }

    public Mp3File getMp3File() {
        return mp3File;
    }

    public double getFrameRatePerMilliseconds() {
        return frameRatePerMilliseconds;
    }

    public BufferedImage getCoverArt() {
        if(customCoverPath != null){
            try{
                return ImageIO.read(new File(customCoverPath));
            } catch(Exception e){
                e.printStackTrace();
                return coverArt;
            }
        } else {
            return coverArt;
        }
    }

    public void setCustomCoverPath(String path){
        this.customCoverPath = path;
    }

    public String getCustomCoverPath(){
        return customCoverPath;
    }
}
