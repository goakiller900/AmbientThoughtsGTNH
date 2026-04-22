package nl.dcraft.ambientthoughts;

import java.io.File;

import net.minecraftforge.common.config.Configuration;

import cpw.mods.fml.common.event.FMLPreInitializationEvent;

public class Config {

    public static Configuration configuration;

    public static boolean enabled = true;
    public static boolean enableJoinMessages = true;
    public static int minMinutesBetweenMessages = 20;
    public static int maxMinutesBetweenMessages = 45;
    public static int perPlayerCooldownMinutes = 30;
    public static int perPlayerRepeatHistorySize = 3;
    public static int globalRepeatHistorySize = 8;
    public static int activityMessageChancePercent = 65;
    public static int gtnhContextMessageChancePercent = 80;
    public static int joinMessageDelaySeconds = 8;
    public static int joinMessageCooldownSeconds = 60;
    public static String messagePrefix = "[Ambient Thoughts]";

    public static int weightWholesome = 20;
    public static int weightShowerThoughts = 15;
    public static int weightChaotic = 10;
    public static int weightCreepy = 5;
    public static int weightGTNHWholesome = 20;
    public static int weightGTNHShowerThoughts = 15;
    public static int weightGTNHChaotic = 15;

    public static File modConfigDirectory;
    public static File messagesFile;

    public static void load(FMLPreInitializationEvent event) {
        File configFile = event.getSuggestedConfigurationFile();
        configuration = new Configuration(configFile);

        modConfigDirectory = new File(event.getModConfigurationDirectory(), AmbientThoughts.MODID);
        if (!modConfigDirectory.exists() && !modConfigDirectory.mkdirs()) {
            AmbientThoughts.LOG.error("Could not create config directory: " + modConfigDirectory.getAbsolutePath());
        }

        messagesFile = new File(modConfigDirectory, "messages.json");

        syncConfig();
    }

    public static void syncConfig() {
        try {
            configuration.load();

            enabled = configuration.getBoolean("enabled", "general", true, "Enable or disable the mod.");

            enableJoinMessages = configuration
                .getBoolean("enableJoinMessages", "general", true, "Enable or disable join messages.");

            minMinutesBetweenMessages = configuration
                .getInt("minMinutesBetweenMessages", "general", 20, 1, 1440, "Minimum minutes between messages.");

            maxMinutesBetweenMessages = configuration
                .getInt("maxMinutesBetweenMessages", "general", 45, 1, 1440, "Maximum minutes between messages.");

            perPlayerCooldownMinutes = configuration.getInt(
                "perPlayerCooldownMinutes",
                "general",
                30,
                0,
                1440,
                "Minimum minutes before the same player can receive another ambient message.");

            perPlayerRepeatHistorySize = configuration.getInt(
                "perPlayerRepeatHistorySize",
                "general",
                3,
                0,
                50,
                "How many recent messages per player should be avoided.");

            globalRepeatHistorySize = configuration.getInt(
                "globalRepeatHistorySize",
                "general",
                8,
                0,
                200,
                "How many recently sent messages should be avoided globally.");

            activityMessageChancePercent = configuration.getInt(
                "activityMessageChancePercent",
                "general",
                65,
                0,
                100,
                "Chance that the mod prefers an activity-specific message when one exists.");

            gtnhContextMessageChancePercent = configuration.getInt(
                "gtnhContextMessageChancePercent",
                "general",
                80,
                0,
                100,
                "Chance that GTNH categories are preferred when the context looks strongly GTNH-like.");

            joinMessageDelaySeconds = configuration.getInt(
                "joinMessageDelaySeconds",
                "general",
                8,
                0,
                300,
                "How many seconds after login to send the join message.");

            joinMessageCooldownSeconds = configuration.getInt(
                "joinMessageCooldownSeconds",
                "general",
                60,
                0,
                3600,
                "How many seconds must pass before the same player can receive another join message.");

            messagePrefix = configuration
                .getString("messagePrefix", "general", "[Ambient Thoughts]", "Prefix shown before ambient messages.");

            weightWholesome = configuration
                .getInt("weightWholesome", "weights", 20, 0, 1000, "Weight for wholesome messages.");

            weightShowerThoughts = configuration
                .getInt("weightShowerThoughts", "weights", 15, 0, 1000, "Weight for shower thought messages.");

            weightChaotic = configuration
                .getInt("weightChaotic", "weights", 10, 0, 1000, "Weight for chaotic messages.");

            weightCreepy = configuration.getInt("weightCreepy", "weights", 5, 0, 1000, "Weight for creepy messages.");

            weightGTNHWholesome = configuration
                .getInt("weightGTNHWholesome", "weights", 20, 0, 1000, "Weight for GTNH wholesome messages.");

            weightGTNHShowerThoughts = configuration
                .getInt("weightGTNHShowerThoughts", "weights", 15, 0, 1000, "Weight for GTNH shower thought messages.");

            weightGTNHChaotic = configuration
                .getInt("weightGTNHChaotic", "weights", 15, 0, 1000, "Weight for GTNH chaotic messages.");
        } finally {
            if (configuration.hasChanged()) {
                configuration.save();
            }
        }
    }

    public static int getWeight(MessageCategory category) {
        switch (category) {
            case WHOLESOME:
                return weightWholesome;
            case SHOWER_THOUGHTS:
                return weightShowerThoughts;
            case CHAOTIC:
                return weightChaotic;
            case CREEPY:
                return weightCreepy;
            case GTNH_WHOLESOME:
                return weightGTNHWholesome;
            case GTNH_SHOWER_THOUGHTS:
                return weightGTNHShowerThoughts;
            case GTNH_CHAOTIC:
                return weightGTNHChaotic;
            default:
                return 0;
        }
    }
}
