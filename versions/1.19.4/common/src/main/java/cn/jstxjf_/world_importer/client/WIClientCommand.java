package cn.jstxjf_.world_importer.client;

import cn.jstxjf_.world_importer.network.packet.ClientUploadManager;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

import java.io.File;

public class WIClientCommand {

    public static void handleSelect(String path) {
        File dir = new File(path);
        if (!dir.exists()) {
            File withRegion = new File(path, "region");
            if (withRegion.exists()) {
                dir = withRegion;
            } else {
                sendMsg("§c路径不存在: " + path);
                return;
            }
        }
        if (!dir.isDirectory()) {
            sendMsg("§c路径不是文件夹: " + path);
            return;
        }

        File[] mcaFiles = dir.listFiles((d, name) -> name.endsWith(".mca"));
        if (mcaFiles == null || mcaFiles.length == 0) {
            sendMsg("§c未找到 .mca 文件: " + dir.getAbsolutePath());
            return;
        }

        ClientUploadManager.setSelectedPath(dir.getAbsolutePath());
        sendMsg("§a已选择 region 文件夹: " + dir.getAbsolutePath() + " (" + mcaFiles.length + " 个MCA文件)");
    }

    public static void handleStart() {
        String path = ClientUploadManager.getSelectedPath();
        if (path == null || path.isEmpty()) {
            sendMsg("§c请先使用 /wi select <路径> 选择 region 文件夹");
            return;
        }
        File regionDir = new File(path);
        if (!regionDir.exists()) {
            sendMsg("§c路径不存在: " + path);
            return;
        }
        ClientUploadManager.startUpload(regionDir);
        sendMsg("§a开始上传...");
    }

    private static void sendMsg(String msg) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null) {
            mc.player.sendSystemMessage(Component.literal(msg));
        }
    }
}
