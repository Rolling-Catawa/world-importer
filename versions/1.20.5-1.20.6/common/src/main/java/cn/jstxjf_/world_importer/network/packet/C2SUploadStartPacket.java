package cn.jstxjf_.world_importer.network.packet;

import cn.jstxjf_.world_importer.WorldImporter;
import cn.jstxjf_.world_importer.data.ImportSession;
import cn.jstxjf_.world_importer.network.WINetworking;
import dev.architectury.networking.NetworkManager;
import io.netty.buffer.Unpooled;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public class C2SUploadStartPacket {
    public static void send(int totalMCA) {
        RegistryFriendlyByteBuf buf = new RegistryFriendlyByteBuf(Unpooled.buffer(), net.minecraft.client.Minecraft.getInstance().level.registryAccess());
        buf.writeInt(totalMCA);
        NetworkManager.sendToServer(WINetworking.C2S_UPLOAD_START, buf);
    }

    public static void handle(RegistryFriendlyByteBuf buf, NetworkManager.PacketContext ctx) {
        int totalMCA = buf.readInt();
        ctx.queue(() -> {
            if (ctx.getPlayer() instanceof ServerPlayer player) {
                if (!player.hasPermissions(2)) {
                    player.sendSystemMessage(Component.literal("§c权限不足"));
                    return;
                }
                ImportSession session = ImportSession.getOrCreate(player.server);
                if (session.isImporting()) {
                    player.sendSystemMessage(Component.literal("§c已有导入任务进行�?));
                    return;
                }
                session.setTotal(totalMCA);
                session.setImporting(true);
                WorldImporter.LOGGER.info("Player {} started import of {} MCA files", player.getName().getString(), totalMCA);
                player.sendSystemMessage(Component.literal("§a开始导�?" + totalMCA + " 个MCA文件"));
                S2CRequestNextPacket.send(player);
            }
        });
    }
}

