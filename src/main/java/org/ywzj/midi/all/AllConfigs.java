package org.ywzj.midi.all;

import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.ModConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

public class AllConfigs {

    public static CommonConfig common;

    public static void register(ModLoadingContext context) {
        Pair<CommonConfig, ModConfigSpec> specPair = new ModConfigSpec.Builder().configure(CommonConfig::new);
        common = specPair.getLeft();
        context.getActiveContainer().registerConfig(ModConfig.Type.COMMON, specPair.getRight());
    }

    public static class CommonConfig {

        public final ModConfigSpec.ConfigValue<Integer> maxSyncMusicTo;
        public final ModConfigSpec.DoubleValue soundDistanceMultiplier;

        public CommonConfig(ModConfigSpec.Builder builder) {
            maxSyncMusicTo = builder.comment("The maximum number of players one can share local music with.")
                    .define("max_sync_music_to", 3);
            soundDistanceMultiplier = builder.comment("Multiplier for the audible distance of instrument and music sounds.")
                    .defineInRange("sound_distance_multiplier", 3.0d, 0.1d, 100.0d);
        }

    }

}
