package cn.jstxjf_.world_importer.network.packet;

import cn.jstxjf_.world_importer.config.WIConfig;
import cn.jstxjf_.world_importer.network.WINetworking;
import dev.architectury.networking.NetworkManager;
import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;

public class S2CRequestNextPacket {
    private static int delayCounter = 0;
    private static boolean waiting = false;

    public static void send(ServerPlayer player) {
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        buf.writeBoolean(true);
        NetworkManager.sendToPlayer(player, WINetworking.S2C_REQUEST_NEXT, buf);
    }

    public static void handle(FriendlyByteBuf buf, NetworkManager.PacketContext ctx) {
        buf.readBoolean();
        ctx.queue(() -> {
            int delay = computeDelay();
            if (delay <= 1) {
                ClientUploadManager.sendNext();
            } else {
                waiting = true;
                delayCounter = delay;
            }
        });
    }

    public static void tick() {
        if (!waiting) return;
        delayCounter--;
        if (delayCounter <= 0) {
            waiting = false;
            ClientUploadManager.sendNext();
        }
    }

    private static int computeDelay() {
        WIConfig cfg = WIConfig.get();
        if (!cfg.autoThrottle) {
            return cfg.uploadIntervalTicks;
        }
        double tps = ClientImportState.getServerTps();
        if (tps >= cfg.tpsThresholdHigh) {
            return cfg.uploadIntervalTicks;
        } else if (tps <= cfg.tpsThresholdLow) {
            return cfg.maxIntervalTicks;
        } else {
            double ratio = (cfg.tpsThresholdHigh - tps) / (cfg.tpsThresholdHigh - cfg.tpsThresholdLow);
            return (int) (cfg.uploadIntervalTicks + ratio * (cfg.maxIntervalTicks - cfg.uploadIntervalTicks));
        }
    }
}
