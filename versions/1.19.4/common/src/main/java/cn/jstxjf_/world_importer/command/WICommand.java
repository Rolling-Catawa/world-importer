package cn.jstxjf_.world_importer.command;

import cn.jstxjf_.world_importer.data.ImportSession;
import cn.jstxjf_.world_importer.network.packet.RelightTask;
import cn.jstxjf_.world_importer.network.packet.S2CImportStatusPacket;
import cn.jstxjf_.world_importer.network.packet.S2CSelectPacket;
import cn.jstxjf_.world_importer.network.packet.S2CStartPacket;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public class WICommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("wi")
                .requires(src -> src.hasPermission(2))
                .then(Commands.literal("setpos")
                        .executes(ctx -> {
                            ServerPlayer player = ctx.getSource().getPlayerOrException();
                            return setPos(ctx.getSource(), player);
                        })
                )
                .then(Commands.literal("select")
                        .then(Commands.argument("path", StringArgumentType.greedyString())
                                .executes(ctx -> {
                                    ServerPlayer player = ctx.getSource().getPlayerOrException();
                                    String path = StringArgumentType.getString(ctx, "path");
                                    S2CSelectPacket.send(player, path);
                                    return 1;
                                })
                        )
                )
                .then(Commands.literal("start")
                        .executes(ctx -> {
                            ServerPlayer player = ctx.getSource().getPlayerOrException();
                            S2CStartPacket.send(player);
                            return 1;
                        })
                )
                .then(Commands.literal("status")
                        .executes(ctx -> showStatus(ctx.getSource()))
                )
                .then(Commands.literal("cancel")
                        .executes(ctx -> cancelImport(ctx.getSource()))
                )
        );
    }

    private static int setPos(CommandSourceStack source, ServerPlayer player) {
        int x = player.getBlockX();
        int z = player.getBlockZ();
        ImportSession session = ImportSession.getOrCreate(source.getServer());
        session.setOrigin(x, z);
        source.sendSuccess(Component.literal("§a已设置粘贴中心点: (" + x + ", " + z + ")"), true);
        return 1;
    }

    private static int showStatus(CommandSourceStack source) {
        ImportSession session = ImportSession.getOrCreate(source.getServer());
        if (session.isImporting()) {
            source.sendSuccess(Component.literal("§e导入中... " + session.getProgress() + "% (" + session.getCompletedMCA() + "/" + session.getTotalMCA() + ")"), false);
        } else {
            source.sendSuccess(Component.literal(session.getStatusDisplay()), false);
        }
        return 1;
    }

    private static int cancelImport(CommandSourceStack source) {
        ImportSession session = ImportSession.getOrCreate(source.getServer());
        if (session.isImporting() || RelightTask.isActive()) {
            session.cancel();
            RelightTask.stop();
            ServerPlayer player = source.getPlayer();
            if (player != null) {
                S2CImportStatusPacket.send(player, 0, 0, "done");
            }
            source.sendSuccess(Component.literal("§c已取消导入"), true);
        } else {
            source.sendFailure(Component.literal("§c当前没有进行中的导入"));
        }
        return 1;
    }
}
