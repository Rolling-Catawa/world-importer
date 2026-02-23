package cn.jstxjf_.world_importer.network.packet;

import cn.jstxjf_.world_importer.network.WINetworking;
import dev.architectury.networking.NetworkManager;
import io.netty.buffer.Unpooled;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

public class S2CImportStatusPacket {
    public static void send(ServerPlayer player, int completed, int total, String status) {
        double tps = calcTps(player.server);
        RegistryFriendlyByteBuf buf = new RegistryFriendlyByteBuf(Unpooled.buffer(), player.server.registryAccess());
        buf.writeInt(completed);
        buf.writeInt(total);
        buf.writeUtf(status);
        buf.writeDouble(tps);
        NetworkManager.sendToPlayer(player, WINetworking.S2C_IMPORT_STATUS, buf);
    }

    public static void handle(RegistryFriendlyByteBuf buf, NetworkManager.PacketContext ctx) {
        int completed = buf.readInt();
        int total = buf.readInt();
        String status = buf.readUtf(256);
        double tps = buf.isReadable() ? buf.readDouble() : 20.0;
        ctx.queue(() -> ClientImportState.updateStatus(completed, total, status, tps));
    }

    private static double calcTps(MinecraftServer server) {
        double avgMs = server.getAverageTickTimeNanos() / 1_000_000.0;
        if (avgMs <= 0) return 20.0;
        return Math.min(20.0, 1000.0 / avgMs);
    }
}
