package cn.jstxjf_.world_importer.fabric;

import cn.jstxjf_.world_importer.WorldImporter;
import net.fabricmc.api.ModInitializer;

public class WorldImporterFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        WorldImporter.init();
    }
}
