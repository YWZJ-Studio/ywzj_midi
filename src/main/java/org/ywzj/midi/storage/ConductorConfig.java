package org.ywzj.midi.storage;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fml.loading.FMLPaths;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class ConductorConfig {

    private static final Gson gson = new Gson();

    static {
        Path path = FMLPaths.CONFIGDIR.get().resolve("limitless_concert");
        File f = path.toFile();
        if (!f.exists()) {
            f.mkdir();
        }
        Path cachePath = path.resolve("conductor_config");
        File fCache = cachePath.toFile();
        if (!fCache.exists()) {
            fCache.mkdir();
        }
    }

    public static void save(String name, MidConfig midConfig) {
        Path path = FMLPaths.CONFIGDIR.get().resolve("limitless_concert").resolve("conductor_config").resolve(name + ".cc");
        try (FileWriter writer = new FileWriter(path.toFile())) {
            gson.toJson(midConfig, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static MidConfig get(String name) {
        Path path = FMLPaths.CONFIGDIR.get().resolve("limitless_concert").resolve("conductor_config");
        File f = path.toFile();
        if (f.isDirectory()) {
            File[] flist = f.listFiles();
            if (flist != null) {
                for (File file : flist) {
                    if (file.getAbsolutePath().endsWith(name + ".cc")) {
                        try {
                            FileReader reader = new FileReader(file.getAbsolutePath());
                            java.lang.reflect.Type type = new TypeToken<MidConfig>(){}.getType();
                            MidConfig midConfig = gson.fromJson(reader, type);
                            if (midConfig != null) {
                                return midConfig;
                            }
                        } catch (Exception exception) {
                            exception.printStackTrace();
                        }
                    }
                }
            }
        }
        return null;
    }

    public static List<Path> getConfigs() {
        List<Path> paths = new ArrayList<>();
        Path path = FMLPaths.CONFIGDIR.get().resolve("limitless_concert").resolve("conductor_config");
        File f = path.toFile();
        if (f.isDirectory()) {
            File[] flist = f.listFiles();
            if (flist != null) {
                for (File file : flist) {
                    if (file.getAbsolutePath().endsWith(".cc")) {
                        paths.add(file.toPath());
                    }
                }
            }
        }
        return paths;
    }

    public static class MidConfig {

        private List<ChannelUnit> channelUnits;
        private List<String> channelFilter;

        public List<ChannelUnit> getChannelUnits() {
            return channelUnits;
        }

        public void setChannelUnits(List<ChannelUnit> channelUnits) {
            this.channelUnits = channelUnits;
        }

        public List<String> getChannelFilter() {
            return channelFilter;
        }

        public void setChannelFilter(List<String> channelFilter) {
            this.channelFilter = channelFilter;
        }

    }

    public static class ChannelUnit {

        private String instrumentId;
        private int vol;
        private List<String> playerNames;

        public ChannelUnit(String instrumentId, int vol, List<String> playerNames) {
            this.instrumentId = instrumentId;
            this.vol = vol;
            this.playerNames = playerNames;
        }

        public String getInstrumentId() {
            return instrumentId;
        }

        public ResourceLocation getInstrumentIdAsResourceLocation() {
            return instrumentId != null ? ResourceLocation.tryParse(instrumentId) : null;
        }

        public void setInstrumentId(String instrumentId) {
            this.instrumentId = instrumentId;
        }

        public int getVol() {
            return vol;
        }

        public void setVol(int vol) {
            this.vol = vol;
        }

        public List<String> getPlayerNames() {
            return playerNames == null ? new ArrayList<>() : playerNames;
        }

        public void setPlayerNames(List<String> playerNames) {
            this.playerNames = playerNames;
        }

    }

}
