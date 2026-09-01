package org.ywzj.midi.all;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import org.apache.commons.lang3.tuple.Pair;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class AllConfigs {

    public static CommonConfig common;

    public static void register(ModLoadingContext context) {
        Pair<CommonConfig, ForgeConfigSpec> specPair = new ForgeConfigSpec.Builder().configure(CommonConfig::new);
        common = specPair.getLeft();
        context.registerConfig(ModConfig.Type.COMMON, specPair.getRight());
    }

    public static class CommonConfig {

        public final ForgeConfigSpec.ConfigValue<Integer> maxSyncMusicTo;
        public final ForgeConfigSpec.DoubleValue soundDistanceMultiplier;

        public CommonConfig(ForgeConfigSpec.Builder builder) {
            maxSyncMusicTo = builder.comment("The maximum number of players one can share local music with.")
                    .define("max_sync_music_to", 3);
            soundDistanceMultiplier = builder.comment("Multiplier for the audible distance of instrument and music sounds.")
                    .defineInRange("sound_distance_multiplier", 3.0d, 0.1d, 100.0d);
        }

    }

}
