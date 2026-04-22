package nl.dcraft.ambientthoughts;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;

@Mod(modid = AmbientThoughts.MODID, name = AmbientThoughts.MODNAME, version = Tags.VERSION)
public class AmbientThoughts {

    public static final String MODID = "ambientthoughts";
    public static final String MODNAME = "Ambient Thoughts";

    @Mod.Instance(MODID)
    public static AmbientThoughts instance;

    @SidedProxy(
        clientSide = "nl.dcraft.ambientthoughts.ClientProxy",
        serverSide = "nl.dcraft.ambientthoughts.CommonProxy")
    public static CommonProxy proxy;

    public static final Logger LOG = LogManager.getLogger(MODID);

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        LOG.info("Ambient Thoughts preInit");
        Config.load(event);
        proxy.preInit(event);
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        LOG.info("Ambient Thoughts init");
        proxy.init(event);
    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        LOG.info("Ambient Thoughts postInit");
        proxy.postInit(event);
    }

    @Mod.EventHandler
    public void serverStarting(FMLServerStartingEvent event) {
        proxy.serverStarting(event);
    }
}
