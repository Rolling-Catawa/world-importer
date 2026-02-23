package cn.jstxjf_.world_importer.neoforge;

import cn.jstxjf_.world_importer.WorldImporter;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;

@Mod(WorldImporter.MOD_ID)
public class WorldImporterNeoForge {
    public WorldImporterNeoForge(IEventBus modEventBus) {
        WorldImporter.init();
    }
}

