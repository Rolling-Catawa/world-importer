package cn.jstxjf_.world_importer.neoforge;

import cn.jstxjf_.world_importer.client.WorldImporterClient;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(modid = "world_importer", bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class WorldImporterNeoForgeClient {
    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        WorldImporterClient.init();
    }
}

