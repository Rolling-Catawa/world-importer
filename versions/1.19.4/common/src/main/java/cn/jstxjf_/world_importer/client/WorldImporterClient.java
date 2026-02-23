package cn.jstxjf_.world_importer.client;

import cn.jstxjf_.world_importer.network.WINetworking;
import cn.jstxjf_.world_importer.network.packet.ClientImportState;
import cn.jstxjf_.world_importer.network.packet.S2CRequestNextPacket;
import dev.architectury.event.events.client.ClientGuiEvent;
import dev.architectury.event.events.client.ClientTickEvent;
import dev.architectury.registry.client.keymappings.KeyMappingRegistry;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.GuiComponent;
import org.lwjgl.glfw.GLFW;

public class WorldImporterClient {
    public static final KeyMapping OPEN_CONFIG_KEY = new KeyMapping(
            "key.world_importer.open_config",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_K,
            "key.categories.world_importer"
    );

    public static void init() {
        WINetworking.initClient();
        KeyMappingRegistry.register(OPEN_CONFIG_KEY);

        ClientTickEvent.CLIENT_POST.register(mc -> {
            S2CRequestNextPacket.tick();
            if (mc.screen != null) return;
            if (OPEN_CONFIG_KEY.consumeClick()) {
                mc.setScreen(new WIConfigScreen(null));
            }
        });

        ClientGuiEvent.RENDER_HUD.register((poseStack, delta) -> renderProgressHud(poseStack));
    }

    private static void renderProgressHud(PoseStack poseStack) {
        String status = ClientImportState.getStatus();
        if ("idle".equals(status) || "done".equals(status)) return;

        Minecraft mc = Minecraft.getInstance();
        int completed = ClientImportState.getCompleted();
        int total = ClientImportState.getTotal();
        int percent = ClientImportState.getProgressPercent();
        double tps = ClientImportState.getServerTps();
        int screenW = mc.getWindow().getGuiScaledWidth();

        String label;
        int barColor;
        if ("relighting".equals(status)) {
            label = "§b光照更新: " + completed + "/" + total;
            barColor = 0xFF55FFFF;
        } else if ("syncing".equals(status)) {
            label = "§d同步区块: " + completed + "/" + total;
            barColor = 0xFFFF55FF;
        } else {
            label = "§e导入中: " + completed + "/" + total + " (" + percent + "%)";
            barColor = 0xFF55FF55;
        }

        int barWidth = 200;
        int barHeight = 5;
        int barX = (screenW - barWidth) / 2;
        int barY = 4;

        GuiComponent.fill(poseStack, barX - 1, barY - 1, barX + barWidth + 1, barY + barHeight + 1, 0x80000000);
        GuiComponent.fill(poseStack, barX, barY, barX + barWidth, barY + barHeight, 0xFF333333);
        int fillWidth = total > 0 ? (barWidth * Math.min(completed, total) / total) : 0;
        GuiComponent.fill(poseStack, barX, barY, barX + fillWidth, barY + barHeight, barColor);

        GuiComponent.drawCenteredString(poseStack, mc.font, label, screenW / 2, barY + barHeight + 3, 0xFFFFFF);

        int tpsColor = tps >= 18.0 ? 0xFF55FF55 : tps >= 15.0 ? 0xFFFFFF55 : 0xFFFF5555;
        String tpsText = String.format("TPS: %.1f", tps);
        GuiComponent.drawString(poseStack, mc.font, tpsText, barX + barWidth + 5, barY, tpsColor);
    }
}
