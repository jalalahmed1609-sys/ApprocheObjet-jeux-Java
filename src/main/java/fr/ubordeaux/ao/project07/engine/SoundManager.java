package fr.ubordeaux.ao.project07.engine;

import javax.sound.sampled.*;
import java.io.BufferedInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Gestionnaire central des sons : charge et prépare les sons en mémoire.
 */
public class SoundManager {

    //private static final String BASEPATH = "/sounds/";
    private static SoundManager instance;

    private final Map<String, byte[]> soundData = new HashMap<>();
    private final Map<String, AudioFormat> soundFormats = new HashMap<>();
    private final Map<String, FloatControl> volumeControls = new HashMap<>();

    private SoundManager() {}

    public static SoundManager getInstance() {
        if (instance == null) instance = new SoundManager();
        return instance;
    }

    /**
     * Charge un son en mémoire et prépare son contrôle de volume.
     */
    public void loadSound(String key, String resourcePath, float volume) {
        try (InputStream is = getClass().getResourceAsStream(resourcePath)) {
            if (is == null) throw new RuntimeException("Resource not found: " + resourcePath);

            BufferedInputStream bis = new BufferedInputStream(is);
            AudioInputStream ais = AudioSystem.getAudioInputStream(bis);
            AudioFormat format = ais.getFormat();
            byte[] data = ais.readAllBytes();

            soundData.put(key, data);
            soundFormats.put(key, format);

            // Préparer un clip pour contrôle du volume
            Clip clip = AudioSystem.getClip();
            clip.open(format, data, 0, data.length);
            if (clip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
                FloatControl gain = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
                float dB = (float) (20.0 * Math.log10(volume));
                dB = Math.max(gain.getMinimum(), Math.min(gain.getMaximum(), dB));
                gain.setValue(dB);
                volumeControls.put(key, gain);
            }
            clip.close();

        } catch (Exception e) {
            System.err.println("Error loading sound " + key + ": " + e.getMessage());
        }
    }

    /**
     * Crée un Clip prêt à jouer depuis les données chargées.
     */
    public Clip createClip(String key) {
        try {
            byte[] data = soundData.get(key);
            AudioFormat format = soundFormats.get(key);
            if (data == null || format == null) return null;

            Clip clip = AudioSystem.getClip();
            clip.open(format, data, 0, data.length);

            // Appliquer le volume si présent
            FloatControl gain = volumeControls.get(key);
            if (gain != null && clip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
                FloatControl clipGain = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
                clipGain.setValue(gain.getValue());
            }

            return clip;

        } catch (Exception e) {
            System.err.println("Error creating clip for " + key + ": " + e.getMessage());
            return null;
        }
    }

    /** Supprime tous les sons chargés en mémoire */
    public void clearCache() {
        soundData.clear();
        soundFormats.clear();
        volumeControls.clear();
        System.out.println("Sound cache cleared.");
    }
}
