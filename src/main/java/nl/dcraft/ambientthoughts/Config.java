package nl.dcraft.ambientthoughts;

import java.io.File;

import net.minecraftforge.common.config.Configuration;

import cpw.mods.fml.common.event.FMLPreInitializationEvent;

public class Config {

    public static Configuration configuration;

    public static boolean enabled = true;
    public static int minMinutesBetweenMessages = 20;
    public static int maxMinutesBetweenMessages = 45;

    public static void load(FMLPreInitializationEvent event) {
        File configFile = event.getSuggestedConfigurationFile();
        configuration = new Configuration(configFile);

        syncConfig();
    }

    public static void syncConfig() {
        try {
            configuration.load();

            enabled = configuration.getBoolean("enabled", "general", true, "Enable or disable the mod.");

            minMinutesBetweenMessages = configuration
                .getInt("minMinutesBetweenMessages", "general", 20, 1, 1440, "Minimum minutes between messages.");

            maxMinutesBetweenMessages = configuration
                .getInt("maxMinutesBetweenMessages", "general", 45, 1, 1440, "Maximum minutes between messages.");
        } finally {
            if (configuration.hasChanged()) {
                configuration.save();
            }
        }
    }
}
