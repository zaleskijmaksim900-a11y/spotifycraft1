package com.spotifycraft.music;

import java.nio.file.Path;

public class MusicTrack {
    private final String displayName;
    private final Path path;

    public MusicTrack(String displayName, Path path) {
        this.displayName = displayName;
        this.path = path;
    }

    public String getDisplayName() { return displayName; }
    public Path getPath() { return path; }

    @Override
    public String toString() { return displayName; }
}
