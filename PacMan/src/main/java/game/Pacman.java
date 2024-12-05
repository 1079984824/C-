package game;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import java.io.IOException;
import java.net.URL;
import javax.swing.JFrame;

public class Pacman extends JFrame {

    public Pacman() {
        add(new Model());
        playBackgroundMusic();
    }

    public static void main(String[] args) {
        Pacman pac = new Pacman();
        pac.setVisible(true);
        pac.setTitle("Pacman");
        pac.setSize(380, 420);
        pac.setDefaultCloseOperation(EXIT_ON_CLOSE);
        pac.setLocationRelativeTo(null);
    }

    private void playBackgroundMusic() {
        try {
            URL url = getClass().getResource("/background.wav");
            if (url == null) {
                throw new IOException("Audio file not found");
            }
            AudioInputStream audioIn = AudioSystem.getAudioInputStream(url);
            Clip clip = AudioSystem.getClip();
            clip.open(audioIn);
            clip.loop(Clip.LOOP_CONTINUOUSLY); // Loop playback
            clip.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
