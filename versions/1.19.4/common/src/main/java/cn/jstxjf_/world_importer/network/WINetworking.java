package cn.jstxjf_.world_importer.network;

import cn.jstxjf_.world_importer.WorldImporter;
import cn.jstxjf_.world_importer.network.packet.*;
import dev.architectury.networking.NetworkManager;
import net.minecraft.resources.ResourceLocation;

public class WINetworking {
    public static final ResourceLocation C2S_UPLOAD_MCA_CHUNK = new ResourceLocation(WorldImporter.MOD_ID, "upload_mca_chunk");
    public static final ResourceLocation C2S_UPLOAD_START = new ResourceLocation(WorldImporter.MOD_ID, "upload_start");
    public static final ResourceLocation C2S_UPLOAD_FINISH = new ResourceLocation(WorldImporter.MOD_ID, "upload_finish");
    public static final ResourceLocation S2C_IMPORT_STATUS = new ResourceLocation(WorldImporter.MOD_ID, "import_status");
    public static final ResourceLocation S2C_REQUEST_NEXT = new ResourceLocation(WorldImporter.MOD_ID, "request_next");
    public static final ResourceLocation S2C_SELECT = new ResourceLocation(WorldImporter.MOD_ID, "select");
    public static final ResourceLocation S2C_START = new ResourceLocation(WorldImporter.MOD_ID, "start");

    public static void init() {
        NetworkManager.registerReceiver(NetworkManager.Side.C2S, C2S_UPLOAD_START, C2SUploadStartPacket::handle);
        NetworkManager.registerReceiver(NetworkManager.Side.C2S, C2S_UPLOAD_MCA_CHUNK, C2SUploadMCAChunkPacket::handle);
        NetworkManager.registerReceiver(NetworkManager.Side.C2S, C2S_UPLOAD_FINISH, C2SUploadFinishPacket::handle);
    }

    public static void initClient() {
        NetworkManager.registerReceiver(NetworkManager.Side.S2C, S2C_IMPORT_STATUS, S2CImportStatusPacket::handle);
        NetworkManager.registerReceiver(NetworkManager.Side.S2C, S2C_REQUEST_NEXT, S2CRequestNextPacket::handle);
        NetworkManager.registerReceiver(NetworkManager.Side.S2C, S2C_SELECT, S2CSelectPacket::handle);
        NetworkManager.registerReceiver(NetworkManager.Side.S2C, S2C_START, S2CStartPacket::handle);
    }
}
