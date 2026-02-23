package cn.jstxjf_.world_importer.network.packet;

import cn.jstxjf_.world_importer.WorldImporter;
import cn.jstxjf_.world_importer.data.ImportSession;
import cn.jstxjf_.world_importer.network.WINetworking;
import dev.architectury.networking.NetworkManager;
import io.netty.buffer.Unpooled;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public class C2SUploadFinishPacket {
    public static void send() {
        RegistryFriendlyByteBuf buf = new RegistryFriendlyByteBuf(Unpooled.buffer(), net.minecraft.client.Minecraft.getInstance().level.registryAccess());
        buf.writeBoolean(true);
        NetworkManager.sendToServer(WINetworking.C2S_UPLOAD_FINISH, buf);
    }

    public static void handle(RegistryFriendlyByteBuf buf, NetworkManager.PacketContext ctx) {
        buf.readBoolean();
        ctx.queue(() -> {
            if (ctx.getPlayer() instanceof ServerPlayer player) {
                ImportSession session = ImportSession.getOrCreate(player.server);
                session.setImporting(false);
                WorldImporter.LOGGER.info("Import finished by {}: {}/{} chunks",
                        player.getName().getString(), session.getCompletedMCA(), session.getTotalMCA());
                player.sendSystemMessage(Component.literal("§e正在更新光照..."));
                S2CImportStatusPacket.send(player, 0, session.getPlacedChunks().size(), "relighting");
                RelightTask.start(player.serverLevel(), session.getPlacedChunks(), player);
            }
        });
    }
}
