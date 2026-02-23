package cn.jstxjf_.world_importer.forge;

import cn.jstxjf_.world_importer.client.WorldImporterClient;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(modid = "world_importer", bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class WorldImporterForgeClient {
    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        WorldImporterClient.init();
    }
}

