package cn.jstxjf_.world_importer.network.packet;

import cn.jstxjf_.world_importer.client.WIClientCommand;
import cn.jstxjf_.world_importer.network.WINetworking;
import dev.architectury.networking.NetworkManager;
import io.netty.buffer.Unpooled;
import net.minecraft.network.RegistryRegistryFriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;

public class S2CSelectPacket {
    public static void send(ServerPlayer player, String path) {
        RegistryFriendlyByteBuf buf = new RegistryFriendlyByteBuf(Unpooled.buffer(), player.server.registryAccess());
        buf.writeUtf(path);
        NetworkManager.sendToPlayer(player, WINetworking.S2C_SELECT, buf);
    }

    public static void handle(RegistryFriendlyByteBuf buf, NetworkManager.PacketContext ctx) {
        String path = buf.readUtf(2048);
        ctx.queue(() -> WIClientCommand.handleSelect(path));
    }
}

