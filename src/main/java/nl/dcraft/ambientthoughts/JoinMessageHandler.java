package nl.dcraft.ambientthoughts;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatComponentText;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent;
import cpw.mods.fml.common.gameevent.TickEvent;

public class JoinMessageHandler {

    private final MessageLoader messageLoader;
    private final MessageFormatter messageFormatter;
    private final ActivityDetector activityDetector;
    private final Map<String, Long> pendingJoinMessages;
    private final Map<String, Long> lastJoinMessageTickByPlayer;

    private long totalServerTicks;

    public JoinMessageHandler() {
        this.messageLoader = new MessageLoader();
        this.messageFormatter = new MessageFormatter();
        this.activityDetector = new ActivityDetector();
        this.pendingJoinMessages = new HashMap<String, Long>();
        this.lastJoinMessageTickByPlayer = new HashMap<String, Long>();
        this.totalServerTicks = 0L;
        this.messageLoader.load();
    }

    @SubscribeEvent
    public void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (!Config.enabled || !Config.enableJoinMessages) {
            return;
        }

        if (!(event.player instanceof EntityPlayerMP)) {
            return;
        }

        EntityPlayerMP player = (EntityPlayerMP) event.player;
        String uuid = player.getUniqueID()
            .toString();

        if (pendingJoinMessages.containsKey(uuid)) {
            AmbientThoughts.LOG
                .info("Ambient Thoughts ignored duplicate join queue for " + player.getCommandSenderName() + ".");
            return;
        }

        long cooldownTicks = (long) Math.max(0, Config.joinMessageCooldownSeconds) * 20L;
        Long lastSentTick = lastJoinMessageTickByPlayer.get(uuid);

        if (lastSentTick != null && cooldownTicks > 0L) {
            long ticksSinceLastJoinMessage = totalServerTicks - lastSentTick.longValue();
            if (ticksSinceLastJoinMessage < cooldownTicks) {
                AmbientThoughts.LOG.info(
                    "Ambient Thoughts skipped join message for " + player.getCommandSenderName()
                        + " because join cooldown is still active.");
                return;
            }
        }

        long delayTicks = (long) Math.max(0, Config.joinMessageDelaySeconds) * 20L;
        long sendAtTick = totalServerTicks + delayTicks;

        pendingJoinMessages.put(uuid, Long.valueOf(sendAtTick));

        AmbientThoughts.LOG.info(
            "Ambient Thoughts queued join message for " + player.getCommandSenderName()
                + " in "
                + Config.joinMessageDelaySeconds
                + " second(s).");
    }

    @SubscribeEvent
    public void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }

        totalServerTicks++;

        if (!Config.enabled || !Config.enableJoinMessages) {
            return;
        }

        if (pendingJoinMessages.isEmpty()) {
            return;
        }

        Iterator<Map.Entry<String, Long>> iterator = pendingJoinMessages.entrySet()
            .iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, Long> entry = iterator.next();

            if (totalServerTicks < entry.getValue()
                .longValue()) {
                continue;
            }

            EntityPlayerMP player = getOnlinePlayerByUuid(entry.getKey());
            iterator.remove();

            if (player == null) {
                continue;
            }

            String template = messageLoader.getRandomJoinMessageTemplate();
            if (template == null || template.isEmpty()) {
                continue;
            }

            PlayerContext context = activityDetector.detect(player);
            String message = messageFormatter.format(template, context);

            player.addChatMessage(new ChatComponentText(Config.messagePrefix + " " + message));
            lastJoinMessageTickByPlayer.put(entry.getKey(), Long.valueOf(totalServerTicks));

            AmbientThoughts.LOG
                .info("Ambient Thoughts sent delayed join message to " + player.getCommandSenderName() + ".");
        }
    }

    public void reloadMessages() {
        messageLoader.load();
        AmbientThoughts.LOG.info("Ambient Thoughts join messages reloaded.");
    }

    private EntityPlayerMP getOnlinePlayerByUuid(String uuidString) {
        MinecraftServer server = MinecraftServer.getServer();
        if (server == null || server.getConfigurationManager() == null) {
            return null;
        }

        @SuppressWarnings("unchecked")
        java.util.List<EntityPlayerMP> players = server.getConfigurationManager().playerEntityList;

        if (players == null) {
            return null;
        }

        for (EntityPlayerMP player : players) {
            if (player != null && player.getUniqueID() != null
                && uuidString.equals(
                    player.getUniqueID()
                        .toString())) {
                return player;
            }
        }

        return null;
    }
}
