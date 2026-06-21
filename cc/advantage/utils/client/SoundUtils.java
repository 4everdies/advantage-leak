/*
 * Decompiled with CFR 0.152.
 */
package cc.advantage.utils.client;

import java.io.InputStream;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineEvent;

public class SoundUtils {
    public static void playSound(String resourceLocation) {
        new Thread(() -> {
            try (InputStream sound = SoundUtils.getFileFromResourceAsStream("assets/minecraft/advantage/sound/" + resourceLocation);
                 AudioInputStream audioStream = AudioSystem.getAudioInputStream(sound);){
                Clip clip = AudioSystem.getClip();
                clip.addLineListener(event -> {
                    if (event.getType() == LineEvent.Type.STOP) {
                        clip.close();
                    }
                });
                clip.open(audioStream);
                clip.start();
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private static InputStream getFileFromResourceAsStream(String fileName) {
        ClassLoader classLoader = SoundUtils.class.getClassLoader();
        InputStream inputStream = classLoader.getResourceAsStream(fileName);
        if (inputStream == null) {
            throw new IllegalArgumentException("file not found! " + fileName);
        }
        return inputStream;
    }
}

