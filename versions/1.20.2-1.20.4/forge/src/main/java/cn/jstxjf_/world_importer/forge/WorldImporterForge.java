package cn.jstxjf_.world_importer.forge;

import cn.jstxjf_.world_importer.WorldImporter;
import dev.architectury.platform.forge.EventBuses;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(WorldImporter.MOD_ID)
public class WorldImporterForge {
    public WorldImporterForge() {
        EventBuses.registerModEventBus(WorldImporter.MOD_ID, FMLJavaModLoadingContext.get().getModEventBus());
        WorldImporter.init();
    }
}
