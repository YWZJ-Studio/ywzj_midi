package org.ywzj.midi.storage;

import net.neoforged.fml.loading.FMLPaths;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class MidiFiles {

    public static final File MID_PATH;
    public static final File MUSIC_PATH;

    static {
        Path path = FMLPaths.CONFIGDIR.get().resolve("limitless_concert");
        File f = path.toFile();
        if (!f.exists()) {
            f.mkdir();
        }
        Path midPath = path.resolve("mid");
        File fMid = midPath.toFile();
        if (!fMid.exists()) {
            fMid.mkdir();
        }
        Path musicPath = path.resolve("music");
        File fMusic= musicPath.toFile();
        if (!fMusic.exists()) {
            fMusic.mkdir();
        }
        MID_PATH = fMid;
        MUSIC_PATH = fMusic;
    }

    public static List<Path> getMids() {
        List<Path> paths = new ArrayList<>();
        Path path = FMLPaths.CONFIGDIR.get().resolve("limitless_concert").resolve("mid");
        File f = path.toFile();
        if (f.isDirectory()) {
            File[] flist = f.listFiles();
            if (flist != null) {
                for (File file : flist) {
                    if (file.getAbsolutePath().endsWith(".mid")) {
                        paths.add(file.toPath());
                    }
                }
            }
        }
        return paths;
    }

    public static List<Path> getMusics() {
        List<Path> paths = new ArrayList<>();
        Path path = FMLPaths.CONFIGDIR.get().resolve("limitless_concert").resolve("music");
        File f = path.toFile();
        if (f.isDirectory()) {
            File[] flist = f.listFiles();
            if (flist != null) {
                for (File file : flist) {
                    if (file.getAbsolutePath().endsWith(".mp3")) {
                        paths.add(file.toPath());
                    }
                }
            }
        }
        return paths;
    }

}
