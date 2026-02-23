package cn.jstxjf_.world_importer;

import cn.jstxjf_.world_importer.command.WICommand;
import cn.jstxjf_.world_importer.config.WIConfig;
import cn.jstxjf_.world_importer.network.WINetworking;
import cn.jstxjf_.world_importer.network.packet.RelightTask;
import dev.architectury.event.events.common.CommandRegistrationEvent;
import dev.architectury.event.events.common.TickEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WorldImporter {
    public static final String MOD_ID = "world_importer";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static void init() {
        WIConfig.load();
        WINetworking.init();
        CommandRegistrationEvent.EVENT.register((dispatcher, registry, selection) ->
                WICommand.register(dispatcher)
        );
        TickEvent.SERVER_POST.register(server -> RelightTask.tick());
        LOGGER.info("World Importer initialized");
    }
}
