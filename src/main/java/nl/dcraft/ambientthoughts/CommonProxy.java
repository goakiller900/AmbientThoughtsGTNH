package nl.dcraft.ambientthoughts;

import net.minecraft.entity.player.EntityPlayerMP;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;

public class CommonProxy {

    private ServerMessageScheduler serverMessageScheduler;
    private JoinMessageHandler joinMessageHandler;

    public void preInit(FMLPreInitializationEvent event) {
        AmbientThoughts.LOG.info("Ambient Thoughts common preInit");
    }

    public void init(FMLInitializationEvent event) {
        AmbientThoughts.LOG.info("Ambient Thoughts common init");

        serverMessageScheduler = new ServerMessageScheduler();
        FMLCommonHandler.instance()
            .bus()
            .register(serverMessageScheduler);

        joinMessageHandler = new JoinMessageHandler();
        FMLCommonHandler.instance()
            .bus()
            .register(joinMessageHandler);

        AmbientThoughts.LOG.info("Ambient Thoughts server scheduler registered");
        AmbientThoughts.LOG.info("Ambient Thoughts join message handler registered");
    }

    public void postInit(FMLPostInitializationEvent event) {
        AmbientThoughts.LOG.info("Ambient Thoughts common postInit");
    }

    public void serverStarting(FMLServerStartingEvent event) {
        event.registerServerCommand(new ReloadMessagesCommand());
        event.registerServerCommand(new SendMessageCommand());
        AmbientThoughts.LOG.info("Ambient Thoughts reload command registered");
        AmbientThoughts.LOG.info("Ambient Thoughts send-message command registered");
    }

    public void reloadMessages() {
        if (serverMessageScheduler != null) {
            serverMessageScheduler.reloadMessages();
        }

        if (joinMessageHandler != null) {
            joinMessageHandler.reloadMessages();
        }
    }

    public boolean sendManualMessageToPlayer(EntityPlayerMP player) {
        if (serverMessageScheduler == null || player == null) {
            return false;
        }

        return serverMessageScheduler.sendManualMessageToPlayer(player);
    }
}
