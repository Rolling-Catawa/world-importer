package cn.jstxjf_.world_importer.client;

import cn.jstxjf_.world_importer.config.WIConfig;
import cn.jstxjf_.world_importer.network.packet.ClientImportState;
import cn.jstxjf_.world_importer.network.packet.ClientUploadManager;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.io.File;

public class WIConfigScreen extends Screen {
    private final Screen parent;
    private EditBox worldPathBox;
    private EditBox uploadIntervalBox;
    private EditBox maxPacketSizeBox;
    private EditBox relightSpeedBox;
    private Button autoThrottleBtn;

    public WIConfigScreen(Screen parent) {
        super(Component.literal("World Importer"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int cx = this.width / 2;
        int y = 35;

        worldPathBox = new EditBox(this.font, cx - 150, y, 300, 20, Component.literal("Path"));
        worldPathBox.setMaxLength(512);
        worldPathBox.setValue(ClientUploadManager.getSelectedPath());
        worldPathBox.setHint(Component.literal("region文件夹路径 或 /wi select"));
        this.addRenderableWidget(worldPathBox);
        y += 26;

        uploadIntervalBox = new EditBox(this.font, cx - 150, y, 70, 20, Component.literal("Interval"));
        uploadIntervalBox.setMaxLength(4);
        uploadIntervalBox.setValue(String.valueOf(WIConfig.get().uploadIntervalTicks));
        this.addRenderableWidget(uploadIntervalBox);

        maxPacketSizeBox = new EditBox(this.font, cx - 70, y, 70, 20, Component.literal("PacketKB"));
        maxPacketSizeBox.setMaxLength(4);
        maxPacketSizeBox.setValue(String.valueOf(WIConfig.get().maxPacketSizeKB));
        this.addRenderableWidget(maxPacketSizeBox);

        relightSpeedBox = new EditBox(this.font, cx + 10, y, 60, 20, Component.literal("Relight"));
        relightSpeedBox.setMaxLength(3);
        relightSpeedBox.setValue(String.valueOf(WIConfig.get().relightChunksPerTick));
        this.addRenderableWidget(relightSpeedBox);

        autoThrottleBtn = Button.builder(
                Component.literal(WIConfig.get().autoThrottle ? "§a自动调速" : "§c自动调速"),
                btn -> {
                    WIConfig.get().autoThrottle = !WIConfig.get().autoThrottle;
                    btn.setMessage(Component.literal(WIConfig.get().autoThrottle ? "§a自动调速" : "§c自动调速"));
                }
        ).bounds(cx + 78, y, 72, 20).build();
        this.addRenderableWidget(autoThrottleBtn);
        y += 26;

        this.addRenderableWidget(Button.builder(Component.literal("开始上传"), btn -> {
            applyConfig();
            String path = worldPathBox.getValue().trim();
            if (path.isEmpty()) return;
            File regionDir = new File(path);
            if (!regionDir.exists()) regionDir = new File(path, "region");
            ClientUploadManager.startUpload(regionDir);
        }).bounds(cx - 152, y, 100, 20).build());

        this.addRenderableWidget(Button.builder(Component.literal("取消上传"), btn -> {
            ClientUploadManager.cancel();
            ClientImportState.reset();
        }).bounds(cx - 48, y, 100, 20).build());

        this.addRenderableWidget(Button.builder(Component.literal("保存关闭"), btn -> {
            applyConfig();
            WIConfig.save();
            this.onClose();
        }).bounds(cx + 56, y, 96, 20).build());
    }

    private void applyConfig() {
        try { WIConfig.get().uploadIntervalTicks = Integer.parseInt(uploadIntervalBox.getValue()); } catch (NumberFormatException ignored) {}
        try { WIConfig.get().maxPacketSizeKB = Integer.parseInt(maxPacketSizeBox.getValue()); } catch (NumberFormatException ignored) {}
        try { WIConfig.get().relightChunksPerTick = Math.max(1, Integer.parseInt(relightSpeedBox.getValue())); } catch (NumberFormatException ignored) {}
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float delta) {
        this.renderBackground(g);
        g.drawCenteredString(this.font, this.title, this.width / 2, 12, 0xFFFFFF);

        int cx = this.width / 2;
        g.drawString(this.font, "Region路径:", cx - 150, 25, 0xAAAAAA);
        g.drawString(this.font, "间隔(t)", cx - 150, 53, 0xAAAAAA);
        g.drawString(this.font, "包(KB)", cx - 70, 53, 0xAAAAAA);
        g.drawString(this.font, "光照/t", cx + 10, 53, 0xAAAAAA);

        String status = ClientImportState.getStatus();
        if (!"idle".equals(status) && !"done".equals(status)) {
            int completed = ClientImportState.getCompleted();
            int total = ClientImportState.getTotal();
            int percent = ClientImportState.getProgressPercent();
            double tps = ClientImportState.getServerTps();

            String info;
            if ("relighting".equals(status)) info = "§b光照更新: " + completed + "/" + total;
            else if ("syncing".equals(status)) info = "§d同步: " + completed + "/" + total;
            else info = "§e导入: " + completed + "/" + total + " (" + percent + "%)";

            g.drawCenteredString(this.font, info, cx, this.height - 38, 0xFFFFFF);

            int tpsColor = tps >= 18 ? 0x55FF55 : tps >= 15 ? 0xFFFF55 : 0xFF5555;
            g.drawCenteredString(this.font, String.format("TPS: %.1f", tps), cx, this.height - 26, tpsColor);

            int bw = 300, bx = cx - bw / 2, by = this.height - 14;
            g.fill(bx, by, bx + bw, by + 6, 0xFF333333);
            g.fill(bx, by, bx + (total > 0 ? bw * Math.min(completed, total) / total : 0), by + 6, 0xFF55FF55);
        }

        super.render(g, mouseX, mouseY, delta);
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(parent);
    }
}
