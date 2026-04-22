package nl.dcraft.ambientthoughts;

import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;

public class ClientProxy extends CommonProxy {

    @Override
    public void preInit(FMLPreInitializationEvent event) {
        super.preInit(event);
        // Client-only setup goes here later
    }

    @Override
    public void init(FMLInitializationEvent event) {
        super.init(event);
        // Client-only init goes here later
    }

    @Override
    public void postInit(FMLPostInitializationEvent event) {
        super.postInit(event);
        // Client-only post-init goes here later
    }
}
