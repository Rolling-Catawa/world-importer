package cn.jstxjf_.world_importer.network.packet;

import cn.jstxjf_.world_importer.config.WIConfig;
import cn.jstxjf_.world_importer.data.ImportSession;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundLevelChunkWithLightPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.lighting.LevelLightEngine;

import java.util.List;

public class RelightTask {
    private static ServerLevel level;
    private static ServerPlayer player;
    private static List<ChunkPos> chunks;
    private static int index = 0;
    private static boolean active = false;

    public static void start(ServerLevel lvl, List<ChunkPos> chunkList, ServerPlayer pl) {
        level = lvl;
        player = pl;
        chunks = chunkList;
        index = 0;
        active = true;
    }

    public static boolean isActive() {
        return active;
    }

    public static void stop() {
        active = false;
        level = null;
        player = null;
        chunks = null;
    }

    public static void tick() {
        if (!active || chunks == null || chunks.isEmpty()) return;

        int perTick = WIConfig.get().relightChunksPerTick;
        int total = chunks.size();
        LevelLightEngine lightEngine = level.getLightEngine();

        int end = Math.min(index + perTick, total);
        for (int i = index; i < end; i++) {
            ChunkPos pos = chunks.get(i);
            LevelChunk chunk = level.getChunk(pos.x, pos.z);
            chunk.setUnsaved(true);
            var packet = new ClientboundLevelChunkWithLightPacket(chunk, lightEngine, null, null);
            for (ServerPlayer p : level.players()) {
                p.connection.send(packet);
            }
        }
        index = end;
        S2CImportStatusPacket.send(player, index, total, "syncing");
        if (index >= total) {
            finish();
        }
    }

    private static void finish() {
        active = false;
        if (player != null) {
            ImportSession session = ImportSession.getOrCreate(player.server);
            player.sendSystemMessage(Component.literal("§a导入完成！共处理 " + session.getCompletedMCA() + " 个区块"));
            S2CImportStatusPacket.send(player, session.getCompletedMCA(), session.getTotalMCA(), "done");
            session.reset();
        }
        level = null;
        player = null;
        chunks = null;
    }
}
