package nl.dcraft.ambientthoughts;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatComponentText;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;

public class ServerMessageScheduler {

    private final MessageLoader messageLoader;
    private final MessageFormatter messageFormatter;
    private final ActivityDetector activityDetector;
    private final Random random;
    private final Map<String, Long> lastMessageTickByPlayer;
    private final Map<String, LinkedList<String>> recentTemplatesByPlayer;
    private final LinkedList<String> recentGlobalTemplates;

    private long totalServerTicks;
    private int tickCounter;
    private int nextMessageTick;

    public ServerMessageScheduler() {
        this.messageLoader = new MessageLoader();
        this.messageFormatter = new MessageFormatter();
        this.activityDetector = new ActivityDetector();
        this.random = new Random();
        this.lastMessageTickByPlayer = new HashMap<String, Long>();
        this.recentTemplatesByPlayer = new HashMap<String, LinkedList<String>>();
        this.recentGlobalTemplates = new LinkedList<String>();
        this.totalServerTicks = 0L;
        this.tickCounter = 0;

        messageLoader.load();
        scheduleNextMessage();
    }

    @SubscribeEvent
    public void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }

        totalServerTicks++;

        if (!Config.enabled) {
            return;
        }

        tickCounter++;

        if (tickCounter < nextMessageTick) {
            return;
        }

        tickCounter = 0;
        scheduleNextMessage();

        List<EntityPlayerMP> players = getOnlinePlayers();
        if (players.isEmpty()) {
            AmbientThoughts.LOG.info("Ambient Thoughts reached message time, but no players are online.");
            return;
        }

        List<EntityPlayerMP> eligiblePlayers = getEligiblePlayers(players);
        if (eligiblePlayers.isEmpty()) {
            AmbientThoughts.LOG.info("Ambient Thoughts skipped this cycle because all players are still on cooldown.");
            return;
        }

        EntityPlayerMP targetPlayer = eligiblePlayers.get(random.nextInt(eligiblePlayers.size()));
        sendTimedMessageToPlayer(targetPlayer);
    }

    public boolean sendManualMessageToPlayer(EntityPlayerMP targetPlayer) {
        return sendMessageToPlayer(targetPlayer, false, "manual");
    }

    public void reloadMessages() {
        tickCounter = 0;
        messageLoader.load();
        scheduleNextMessage();
        AmbientThoughts.LOG.info("Ambient Thoughts messages reloaded.");
    }

    private boolean sendTimedMessageToPlayer(EntityPlayerMP targetPlayer) {
        return sendMessageToPlayer(targetPlayer, true, "timed");
    }

    private boolean sendMessageToPlayer(EntityPlayerMP targetPlayer, boolean applyCooldown, String sourceLabel) {
        if (targetPlayer == null) {
            return false;
        }

        PlayerContext context = activityDetector.detect(targetPlayer);

        TemplateSelection selection = selectTemplate(context);
        if (selection.template == null || selection.template.isEmpty()) {
            AmbientThoughts.LOG.warn("Ambient Thoughts could not find a message template to send.");
            return false;
        }

        rememberTemplate(context.getPlayerName(), selection.template);

        String formattedMessage = messageFormatter.format(selection.template, context);
        targetPlayer.addChatMessage(new ChatComponentText(Config.messagePrefix + " " + formattedMessage));

        if (applyCooldown) {
            lastMessageTickByPlayer.put(context.getPlayerName(), Long.valueOf(totalServerTicks));
        }

        AmbientThoughts.LOG.info(
            "Ambient Thoughts sent " + sourceLabel
                + " "
                + selection.source
                + " message to "
                + context.getPlayerName()
                + " in biome "
                + context.getBiomeName()
                + " with activity "
                + context.getActivityName()
                + ".");

        return true;
    }

    private void scheduleNextMessage() {
        int minMinutes = Config.minMinutesBetweenMessages;
        int maxMinutes = Config.maxMinutesBetweenMessages;

        if (minMinutes < 1) {
            minMinutes = 1;
        }

        if (maxMinutes < minMinutes) {
            maxMinutes = minMinutes;
        }

        int selectedMinutes;
        if (minMinutes == maxMinutes) {
            selectedMinutes = minMinutes;
        } else {
            selectedMinutes = minMinutes + random.nextInt(maxMinutes - minMinutes + 1);
        }

        nextMessageTick = selectedMinutes * 60 * 20;

        AmbientThoughts.LOG.info("Ambient Thoughts scheduled next message in " + selectedMinutes + " minute(s).");
    }

    private List<EntityPlayerMP> getOnlinePlayers() {
        MinecraftServer server = MinecraftServer.getServer();
        if (server == null || server.getConfigurationManager() == null) {
            return new ArrayList<EntityPlayerMP>();
        }

        @SuppressWarnings("unchecked")
        List<EntityPlayerMP> players = server.getConfigurationManager().playerEntityList;

        if (players == null) {
            return new ArrayList<EntityPlayerMP>();
        }

        return players;
    }

    private List<EntityPlayerMP> getEligiblePlayers(List<EntityPlayerMP> players) {
        List<EntityPlayerMP> eligiblePlayers = new ArrayList<EntityPlayerMP>();
        long cooldownTicks = (long) Config.perPlayerCooldownMinutes * 60L * 20L;

        if (cooldownTicks <= 0L) {
            eligiblePlayers.addAll(players);
            return eligiblePlayers;
        }

        for (EntityPlayerMP player : players) {
            if (player == null) {
                continue;
            }

            String playerName = player.getCommandSenderName();
            Long lastTick = lastMessageTickByPlayer.get(playerName);

            if (lastTick == null) {
                eligiblePlayers.add(player);
                continue;
            }

            long ticksSinceLastMessage = totalServerTicks - lastTick.longValue();
            if (ticksSinceLastMessage >= cooldownTicks) {
                eligiblePlayers.add(player);
            }
        }

        return eligiblePlayers;
    }

    private TemplateSelection selectTemplate(PlayerContext context) {
        String playerName = context.getPlayerName();
        boolean shouldTryActivityFirst = shouldUseActivityMessage(context);

        if (shouldTryActivityFirst) {
            List<String> activityTemplates = getActivitySpecificTemplates(context);
            String template = pickTemplateWithRepeatProtection(activityTemplates, playerName);
            if (template != null && !template.isEmpty()) {
                return new TemplateSelection(template, "activity-specific");
            }
        }

        MessageCategory selectedCategory = getWeightedRandomCategory(context);
        List<String> weightedTemplates = messageLoader.getMessages(selectedCategory);
        String template = pickTemplateWithRepeatProtection(weightedTemplates, playerName);
        if (template != null && !template.isEmpty()) {
            return new TemplateSelection(
                template,
                "weighted-" + selectedCategory.name()
                    .toLowerCase(Locale.ROOT));
        }

        if (!shouldTryActivityFirst) {
            List<String> activityTemplates = getActivitySpecificTemplates(context);
            template = pickTemplateWithRepeatProtection(activityTemplates, playerName);
            if (template != null && !template.isEmpty()) {
                return new TemplateSelection(template, "activity-fallback");
            }
        }

        List<String> allTemplates = getAllTemplates();
        template = pickTemplateWithRepeatProtection(allTemplates, playerName);
        if (template != null && !template.isEmpty()) {
            return new TemplateSelection(template, "fallback-any");
        }

        return new TemplateSelection("", "none");
    }

    private boolean shouldUseActivityMessage(PlayerContext context) {
        List<String> activityTemplates = getActivitySpecificTemplates(context);
        if (activityTemplates.isEmpty()) {
            return false;
        }

        int chance = Config.activityMessageChancePercent;
        if (chance <= 0) {
            return false;
        }
        if (chance >= 100) {
            return true;
        }

        return random.nextInt(100) < chance;
    }

    private MessageCategory getWeightedRandomCategory(PlayerContext context) {
        if (shouldPreferGTNHCategory(context)) {
            List<MessageCategory> gtnhPool = getWeightedCategoryPool(true);
            MessageCategory gtnhChoice = getWeightedRandomCategoryFromPool(gtnhPool);
            if (gtnhChoice != null) {
                return gtnhChoice;
            }
        }

        List<MessageCategory> allPool = getWeightedCategoryPool(false);
        MessageCategory normalChoice = getWeightedRandomCategoryFromPool(allPool);
        if (normalChoice != null) {
            return normalChoice;
        }

        return MessageCategory.WHOLESOME;
    }

    private boolean shouldPreferGTNHCategory(PlayerContext context) {
        if (!isLikelyGTNHContext(context)) {
            return false;
        }

        int chance = Config.gtnhContextMessageChancePercent;
        if (chance <= 0) {
            return false;
        }
        if (chance >= 100) {
            return true;
        }

        return random.nextInt(100) < chance;
    }

    private boolean isLikelyGTNHContext(PlayerContext context) {
        String activity = safeLower(context.getActivityName());
        String machine = safeLower(context.getMachineName());
        String item = safeLower(context.getHeldItemName());

        if (activity.startsWith("working on ")) {
            return true;
        }

        if (containsAny(
            machine,
            "macerator",
            "compressor",
            "lathe",
            "wiremill",
            "bender",
            "centrifuge",
            "electrolyzer",
            "chemical",
            "blast furnace",
            "forge hammer",
            "assembler",
            "hatch",
            "controller",
            "boiler",
            "turbine",
            "reactor",
            "industrial",
            "gregtech",
            "steam",
            "bronze",
            "lv",
            "mv",
            "hv",
            "ev",
            "iv",
            "luv",
            "zpm",
            "uv")) {
            return true;
        }

        if (containsAny(
            item,
            "plate",
            "rod",
            "bolt",
            "screw",
            "ring",
            "circuit",
            "dust",
            "cell",
            "cable",
            "wire",
            "rotor",
            "casing",
            "machine hull",
            "coil",
            "foil",
            "pipe",
            "spring",
            "gear")) {
            return true;
        }

        return false;
    }

    private List<MessageCategory> getWeightedCategoryPool(boolean gtnhOnly) {
        List<MessageCategory> pool = new ArrayList<MessageCategory>();

        for (MessageCategory category : MessageCategory.values()) {
            if (Config.getWeight(category) <= 0) {
                continue;
            }

            if (messageLoader.getMessages(category)
                .isEmpty()) {
                continue;
            }

            if (gtnhOnly && !isGTNHCategory(category)) {
                continue;
            }

            pool.add(category);
        }

        return pool;
    }

    private MessageCategory getWeightedRandomCategoryFromPool(List<MessageCategory> pool) {
        if (pool == null || pool.isEmpty()) {
            return null;
        }

        int totalWeight = 0;
        for (MessageCategory category : pool) {
            totalWeight += Math.max(0, Config.getWeight(category));
        }

        if (totalWeight <= 0) {
            return null;
        }

        int roll = random.nextInt(totalWeight);
        int runningTotal = 0;

        for (MessageCategory category : pool) {
            runningTotal += Math.max(0, Config.getWeight(category));
            if (roll < runningTotal) {
                return category;
            }
        }

        return pool.get(0);
    }

    private boolean isGTNHCategory(MessageCategory category) {
        return category == MessageCategory.GTNH_WHOLESOME || category == MessageCategory.GTNH_SHOWER_THOUGHTS
            || category == MessageCategory.GTNH_CHAOTIC;
    }

    private List<String> getActivitySpecificTemplates(PlayerContext context) {
        String activityKey = getActivityJsonKey(context.getActivityName());
        if (activityKey == null) {
            return new ArrayList<String>();
        }

        List<String> activityMessages = messageLoader.getMessages(activityKey);
        if (activityMessages == null) {
            return new ArrayList<String>();
        }

        return activityMessages;
    }

    private List<String> getAllTemplates() {
        List<String> allTemplates = new ArrayList<String>();

        for (MessageCategory category : MessageCategory.values()) {
            List<String> categoryTemplates = messageLoader.getMessages(category);
            if (categoryTemplates != null && !categoryTemplates.isEmpty()) {
                allTemplates.addAll(categoryTemplates);
            }
        }

        return allTemplates;
    }

    private String pickTemplateWithRepeatProtection(List<String> candidates, String playerName) {
        if (candidates == null || candidates.isEmpty()) {
            return "";
        }

        LinkedList<String> playerHistory = getPlayerHistory(playerName);

        List<String> strictCandidates = new ArrayList<String>();
        for (String candidate : candidates) {
            if (!playerHistory.contains(candidate) && !recentGlobalTemplates.contains(candidate)) {
                strictCandidates.add(candidate);
            }
        }

        if (!strictCandidates.isEmpty()) {
            return strictCandidates.get(random.nextInt(strictCandidates.size()));
        }

        List<String> playerOnlyCandidates = new ArrayList<String>();
        for (String candidate : candidates) {
            if (!playerHistory.contains(candidate)) {
                playerOnlyCandidates.add(candidate);
            }
        }

        if (!playerOnlyCandidates.isEmpty()) {
            return playerOnlyCandidates.get(random.nextInt(playerOnlyCandidates.size()));
        }

        return candidates.get(random.nextInt(candidates.size()));
    }

    private LinkedList<String> getPlayerHistory(String playerName) {
        LinkedList<String> history = recentTemplatesByPlayer.get(playerName);
        if (history == null) {
            history = new LinkedList<String>();
            recentTemplatesByPlayer.put(playerName, history);
        }
        return history;
    }

    private void rememberTemplate(String playerName, String template) {
        if (template == null || template.isEmpty()) {
            return;
        }

        if (Config.perPlayerRepeatHistorySize > 0) {
            LinkedList<String> playerHistory = getPlayerHistory(playerName);
            playerHistory.addLast(template);

            while (playerHistory.size() > Config.perPlayerRepeatHistorySize) {
                playerHistory.removeFirst();
            }
        }

        if (Config.globalRepeatHistorySize > 0) {
            recentGlobalTemplates.addLast(template);

            while (recentGlobalTemplates.size() > Config.globalRepeatHistorySize) {
                recentGlobalTemplates.removeFirst();
            }
        }
    }

    private String getActivityJsonKey(String activityName) {
        if (activityName == null) {
            return null;
        }

        String normalized = activityName.trim()
            .toLowerCase(Locale.ROOT);

        if ("mining".equals(normalized)) {
            return "activity_mining";
        }

        if ("building".equals(normalized)) {
            return "activity_building";
        }

        if ("farming".equals(normalized)) {
            return "activity_farming";
        }

        if ("exploring".equals(normalized)) {
            return "activity_exploring";
        }

        if ("fighting for your life".equals(normalized)) {
            return "activity_combat";
        }

        if ("organizing chests".equals(normalized)) {
            return "activity_chest_sorting";
        }

        return null;
    }

    private String safeLower(String value) {
        if (value == null) {
            return "";
        }
        return value.toLowerCase(Locale.ROOT);
    }

    private boolean containsAny(String haystack, String... needles) {
        if (haystack == null || haystack.isEmpty()) {
            return false;
        }

        for (String needle : needles) {
            if (haystack.contains(needle)) {
                return true;
            }
        }

        return false;
    }

    private static class TemplateSelection {

        private final String template;
        private final String source;

        private TemplateSelection(String template, String source) {
            this.template = template;
            this.source = source;
        }
    }
}
