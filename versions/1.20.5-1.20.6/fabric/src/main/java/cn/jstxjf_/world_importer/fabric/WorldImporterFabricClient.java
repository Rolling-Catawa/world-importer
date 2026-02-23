package cn.jstxjf_.world_importer.fabric;

import cn.jstxjf_.world_importer.client.WorldImporterClient;
import net.fabricmc.api.ClientModInitializer;

public class WorldImporterFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        WorldImporterClient.init();
    }
}

