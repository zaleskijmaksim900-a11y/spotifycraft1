package com.spotifycraft.music;

import net.minecraft.client.Minecraft;

import javax.sound.sampled.*;
import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MusicManager {

    private static MusicManager INSTANCE;

    private final List<MusicTrack> tracks = new ArrayList<>();
    private int currentIndex = -1;
    private boolean playing = false;
    private boolean looping = false;
    private boolean shuffling = false;
    private float volume = 0.7f;
    private long trackStartTime = 0;
    private long trackDuration = 0;

    private Clip currentClip;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    private Path musicFolder;

    public static MusicManager getInstance() {
        if (INSTANCE == null) INSTANCE = new MusicManager();
        return INSTANCE;
    }

    public void initialize() {
        // Get .minecraft folder
        Path minecraftDir = Minecraft.getInstance().gameDirectory.toPath();
        musicFolder = minecraftDir.resolve("spotifycraft_music");

        // Create folder if it doesn't exist
        if (!Files.exists(musicFolder)) {
            try {
                Files.createDirectories(musicFolder);
                // Create a README file explaining what to do
                Path readme = musicFolder.resolve("README.txt");
                Files.writeString(readme,
                    "=== SpotifyCraft Music Folder ===\n\n" +
                    "Place your .wav or .ogg music files here!\n" +
                    "Supported formats: .wav, .ogg, .au, .aiff\n\n" +
                    "Folder location: .minecraft/spotifycraft_music/\n\n" +
                    "Press M in-game to open the music player.\n"
                );
                System.out.println("[SpotifyCraft] Created music folder: " + musicFolder);
            } catch (IOException e) {
                System.err.println("[SpotifyCraft] Could not create music folder: " + e.getMessage());
            }
        }

        scanMusic();
    }

    public void scanMusic() {
        tracks.clear();
        if (musicFolder == null || !Files.exists(musicFolder)) return;

        try {
            Files.walk(musicFolder, 1)
                .filter(p -> isAudioFile(p.toString()))
                .forEach(p -> {
                    String name = p.getFileName().toString();
                    // Remove extension for display name
                    String displayName = name.contains(".")
                        ? name.substring(0, name.lastIndexOf('.'))
                        : name;
                    tracks.add(new MusicTrack(displayName, p));
                });
        } catch (IOException e) {
            System.err.println("[SpotifyCraft] Error scanning music: " + e.getMessage());
        }

        System.out.println("[SpotifyCraft] Found " + tracks.size() + " tracks.");
    }

    private boolean isAudioFile(String name) {
        String lower = name.toLowerCase();
        return lower.endsWith(".wav") || lower.endsWith(".au") || lower.endsWith(".aiff");
    }

    public void play(int index) {
        if (index < 0 || index >= tracks.size()) return;
        currentIndex = index;
        stopClip();

        executor.submit(() -> {
            try {
                MusicTrack track = tracks.get(index);
                AudioInputStream ais = AudioSystem.getAudioInputStream(track.getPath().toFile());

                // Convert to PCM if needed
                AudioFormat baseFormat = ais.getFormat();
                AudioFormat decodedFormat = new AudioFormat(
                    AudioFormat.Encoding.PCM_SIGNED,
                    baseFormat.getSampleRate(),
                    16,
                    baseFormat.getChannels(),
                    baseFormat.getChannels() * 2,
                    baseFormat.getSampleRate(),
                    false
                );
                AudioInputStream decoded = AudioSystem.getAudioInputStream(decodedFormat, ais);

                currentClip = AudioSystem.getClip();
                currentClip.open(decoded);

                // Set volume
                FloatControl gainControl = (FloatControl) currentClip.getControl(FloatControl.Type.MASTER_GAIN);
                float dB = (float) (Math.log(Math.max(volume, 0.001f)) / Math.log(10.0) * 20.0);
                gainControl.setValue(Math.max(gainControl.getMinimum(), Math.min(gainControl.getMaximum(), dB)));

                trackDuration = currentClip.getMicrosecondLength() / 1_000_000;
                trackStartTime = System.currentTimeMillis();
                playing = true;
                currentClip.start();

                // Listen for end of track
                currentClip.addLineListener(event -> {
                    if (event.getType() == LineEvent.Type.STOP && playing) {
                        if (currentClip.getMicrosecondPosition() >= currentClip.getMicrosecondLength() - 100_000) {
                            onTrackEnd();
                        }
                    }
                });

            } catch (Exception e) {
                System.err.println("[SpotifyCraft] Error playing track: " + e.getMessage());
                playing = false;
            }
        });
    }

    private void onTrackEnd() {
        if (looping) {
            play(currentIndex);
        } else if (shuffling) {
            int next = (int)(Math.random() * tracks.size());
            play(next);
        } else {
            next();
        }
    }

    public void playOrPause() {
        if (currentClip == null) {
            if (!tracks.isEmpty()) play(Math.max(0, currentIndex));
            return;
        }
        if (playing) {
            currentClip.stop();
            playing = false;
        } else {
            currentClip.start();
            playing = true;
        }
    }

    public void next() {
        if (tracks.isEmpty()) return;
        int next = shuffling
            ? (int)(Math.random() * tracks.size())
            : (currentIndex + 1) % tracks.size();
        play(next);
    }

    public void previous() {
        if (tracks.isEmpty()) return;
        int prev = (currentIndex - 1 + tracks.size()) % tracks.size();
        play(prev);
    }

    private void stopClip() {
        if (currentClip != null) {
            currentClip.stop();
            currentClip.close();
            currentClip = null;
        }
        playing = false;
    }

    public void setVolume(float v) {
        this.volume = Math.max(0f, Math.min(1f, v));
        if (currentClip != null && currentClip.isOpen()) {
            try {
                FloatControl gainControl = (FloatControl) currentClip.getControl(FloatControl.Type.MASTER_GAIN);
                float dB = (float) (Math.log(Math.max(volume, 0.001f)) / Math.log(10.0) * 20.0);
                gainControl.setValue(Math.max(gainControl.getMinimum(), Math.min(gainControl.getMaximum(), dB)));
            } catch (Exception ignored) {}
        }
    }

    public void seekTo(float percent) {
        if (currentClip == null) return;
        long pos = (long)(currentClip.getMicrosecondLength() * percent);
        currentClip.setMicrosecondPosition(pos);
    }

    // Getters
    public List<MusicTrack> getTracks() { return tracks; }
    public int getCurrentIndex() { return currentIndex; }
    public boolean isPlaying() { return playing; }
    public boolean isLooping() { return looping; }
    public boolean isShuffling() { return shuffling; }
    public float getVolume() { return volume; }
    public Path getMusicFolder() { return musicFolder; }

    public void setLooping(boolean v) { looping = v; }
    public void setShuffling(boolean v) { shuffling = v; }

    public float getProgress() {
        if (currentClip == null || currentClip.getMicrosecondLength() == 0) return 0;
        return (float) currentClip.getMicrosecondPosition() / currentClip.getMicrosecondLength();
    }

    public String getCurrentTimeStr() {
        if (currentClip == null) return "0:00";
        long sec = currentClip.getMicrosecondPosition() / 1_000_000;
        return sec / 60 + ":" + String.format("%02d", sec % 60);
    }

    public String getTotalTimeStr() {
        if (currentClip == null) return "0:00";
        long sec = currentClip.getMicrosecondLength() / 1_000_000;
        return sec / 60 + ":" + String.format("%02d", sec % 60);
    }

    public MusicTrack getCurrentTrack() {
        if (currentIndex < 0 || currentIndex >= tracks.size()) return null;
        return tracks.get(currentIndex);
    }
}
