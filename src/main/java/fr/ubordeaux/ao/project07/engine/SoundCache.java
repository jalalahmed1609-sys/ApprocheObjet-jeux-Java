package fr.ubordeaux.ao.project07.engine;

import javax.sound.sampled.Clip;
import javax.sound.sampled.LineEvent;
import java.util.HashMap;
import java.util.Map;

public class SoundCache {

    private static final Map<String, Clip> cache = new HashMap<>();
    private static final int MAX_SIMULTANEOUS_SOUNDS = 10;
    private static int currentPlaying = 0;

    // Nouveau : dernier son joué
    private static Clip currentClip = null;

    public static void loadIfAbsent(String name, String path, float volume) {
        if (!cache.containsKey(name)) {
            SoundManager.getInstance().loadSound(name, path, volume);
            Clip clip = SoundManager.getInstance().createClip(name);
            if (clip != null) 
                cache.put(name, clip);
        }
    }

    public static boolean isCached(String name) {
        return cache.containsKey(name);
    }

    public static synchronized void stopCurrent() {
        if (currentClip != null && currentClip.isRunning()) {
            currentClip.stop();
        }
    }

    public static void playCached(String name) {
        Clip clip = cache.get(name);
        if (clip == null) {
            System.err.println("Sound not in cache: " + name);
            return;
        }

        synchronized (SoundCache.class) {
            // Arrêter le son précédent
            stopCurrent();

            if (currentPlaying >= MAX_SIMULTANEOUS_SOUNDS) return;
            currentPlaying++;
            currentClip = clip;
        }

        new Thread(() -> {
            try {
                clip.stop();
                clip.setFramePosition(0);
                clip.start();
                clip.addLineListener(event -> {
                    if (event.getType() == LineEvent.Type.STOP) {
                        synchronized (SoundCache.class) {
                            currentPlaying--;
                        }
                    }
                });
            } catch (Exception e) {
                System.err.println("Error playing cached sound " + name + ": " + e.getMessage());
                synchronized (SoundCache.class) {
                    currentPlaying--;
                }
            }
        }).start();
    }
}
