package cn.jstxjf_.world_importer.network.packet;

import cn.jstxjf_.world_importer.client.WIClientCommand;
import cn.jstxjf_.world_importer.network.WINetworking;
import dev.architectury.networking.NetworkManager;
import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;

public class S2CStartPacket {
    public static void send(ServerPlayer player) {
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        buf.writeBoolean(true);
        NetworkManager.sendToPlayer(player, WINetworking.S2C_START, buf);
    }

    public static void handle(FriendlyByteBuf buf, NetworkManager.PacketContext ctx) {
        buf.readBoolean();
        ctx.queue(() -> WIClientCommand.handleStart());
    }
}

