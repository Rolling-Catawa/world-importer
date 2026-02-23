package cn.jstxjf_.world_importer.network.packet;

import cn.jstxjf_.world_importer.data.ImportSession;
import cn.jstxjf_.world_importer.network.WINetworking;
import dev.architectury.networking.NetworkManager;
import io.netty.buffer.Unpooled;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;

public class C2SUploadMCAChunkPacket {
    public static void send(int chunkX, int chunkZ, int compressionType, int partIndex, int totalParts, byte[] data) {
        RegistryFriendlyByteBuf buf = new RegistryFriendlyByteBuf(Unpooled.buffer(), net.minecraft.client.Minecraft.getInstance().level.registryAccess());
        buf.writeInt(chunkX);
        buf.writeInt(chunkZ);
        buf.writeByte(compressionType);
        buf.writeInt(partIndex);
        buf.writeInt(totalParts);
        buf.writeByteArray(data);
        NetworkManager.sendToServer(WINetworking.C2S_UPLOAD_MCA_CHUNK, buf);
    }

    public static void handle(RegistryFriendlyByteBuf buf, NetworkManager.PacketContext ctx) {
        int chunkX = buf.readInt();
        int chunkZ = buf.readInt();
        int compressionType = buf.readByte();
        int partIndex = buf.readInt();
        int totalParts = buf.readInt();
        byte[] data = buf.readByteArray(1024 * 1024);
        ctx.queue(() -> {
            if (ctx.getPlayer() instanceof ServerPlayer player) {
                ImportSession session = ImportSession.getOrCreate(player.server);
                if (!session.isImporting() || session.isCancelled()) return;

                String key = chunkX + "," + chunkZ;

                if (totalParts == 1) {
                    ServerChunkPlacer.placeChunk(player, session, chunkX, chunkZ, compressionType, data);
                    session.incrementCompleted();
                    S2CImportStatusPacket.send(player, session.getCompletedMCA(), session.getTotalMCA(), "importing");
                    S2CRequestNextPacket.send(player);
                } else {
                    byte[] existing = session.getMCABuffer(key);
                    if (existing == null) existing = new byte[0];
                    byte[] combined = new byte[existing.length + data.length];
                    System.arraycopy(existing, 0, combined, 0, existing.length);
                    System.arraycopy(data, 0, combined, existing.length, data.length);
                    session.putMCABuffer(key, combined);

                    if (partIndex == totalParts - 1) {
                        byte[] fullData = session.getMCABuffer(key);
                        session.removeMCABuffer(key);
                        ServerChunkPlacer.placeChunk(player, session, chunkX, chunkZ, compressionType, fullData);
                        session.incrementCompleted();
                        S2CImportStatusPacket.send(player, session.getCompletedMCA(), session.getTotalMCA(), "importing");
                        S2CRequestNextPacket.send(player);
                    }
                }
            }
        });
    }
}
