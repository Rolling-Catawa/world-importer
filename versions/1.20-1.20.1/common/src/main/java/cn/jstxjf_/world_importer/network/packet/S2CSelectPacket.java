package cn.jstxjf_.world_importer.network.packet;

import cn.jstxjf_.world_importer.client.WIClientCommand;
import cn.jstxjf_.world_importer.network.WINetworking;
import dev.architectury.networking.NetworkManager;
import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;

public class S2CSelectPacket {
    public static void send(ServerPlayer player, String path) {
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        buf.writeUtf(path);
        NetworkManager.sendToPlayer(player, WINetworking.S2C_SELECT, buf);
    }

    public static void handle(FriendlyByteBuf buf, NetworkManager.PacketContext ctx) {
        String path = buf.readUtf(2048);
        ctx.queue(() -> WIClientCommand.handleSelect(path));
    }
}

