package net.redstone233.nsp.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import dev.architectury.event.events.common.CommandRegistrationEvent;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.redstone233.nsp.OneShotMod;
import net.redstone233.nsp.config.ClientConfig;
import net.redstone233.nsp.util.StackSystemManager;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class StackCommand {

    // 定义堆叠数量的最小和最大值
    private static final int MIN_STACK_COUNT = 1;
    private static final int MAX_STACK_COUNT = OneShotMod.CUSTOM_MAX_ITEM_STACK_COUNT;

    public static void register() {
        // 使用 Architectury 的跨平台命令注册
        CommandRegistrationEvent.EVENT.register((dispatcher, registry, environment) -> {
            // 注册主命令
            dispatcher.register(buildStackCommand());

            // 注册帮助命令
            dispatcher.register(buildStackHelpCommand());

            OneShotMod.LOGGER.info("堆叠命令已在 {} 端成功注册", getPlatformName());
        });
    }

    private static LiteralArgumentBuilder<ServerCommandSource> buildStackCommand() {
        return CommandManager.literal("stack")
                .requires(source -> source.hasPermissionLevel(2))
                .then(CommandManager.literal("get")
                        .executes(StackCommand::getStackCount))
                .then(CommandManager.literal("reset")
                        .executes(StackCommand::resetStackCount))
                .then(CommandManager.literal("set")
                        .then(CommandManager.argument("maxCountValue", IntegerArgumentType.integer(MIN_STACK_COUNT, MAX_STACK_COUNT))
                                .executes(StackCommand::setStackCount)))
                .then(CommandManager.literal("remove")
                        .then(CommandManager.argument("count", IntegerArgumentType.integer(MIN_STACK_COUNT, MAX_STACK_COUNT))
                                .executes(StackCommand::removeStackCount)))
                .then(CommandManager.literal("info")
                        .executes(StackCommand::getStackInfo))
                .then(CommandManager.literal("force-update")
                        .executes(StackCommand::forceUpdateStackSystem))
                .then(CommandManager.literal("sync-config")
                        .executes(StackCommand::syncWithConfig))
                .then(CommandManager.literal("config-status")
                        .executes(StackCommand::getConfigStatus))
                // 新增的配置管理命令
                .then(CommandManager.literal("config")
                        .then(CommandManager.literal("get")
                                .executes(StackCommand::getConfigValue))
                        .then(CommandManager.literal("set")
                                .then(CommandManager.argument("maxStack", IntegerArgumentType.integer(MIN_STACK_COUNT, MAX_STACK_COUNT))
                                        .executes(StackCommand::setConfigValue)))
                        .then(CommandManager.literal("reload")
                                .executes(StackCommand::reloadConfig))
                        .then(CommandManager.literal("reset-defaults")
                                .executes(StackCommand::resetConfigDefaults)));
    }

    // ==================== 新增的配置管理命令实现 ====================

    private static int getConfigValue(CommandContext<ServerCommandSource> context) {
        try {
            int maxStack = ClientConfig.getMaxItemStackCount();
            boolean isEnabled = ClientConfig.isEnabled();

            Text message = Text.literal(
                    "§6=== 当前配置 ===\n" +
                            "§a模组启用: §e" + (isEnabled ? "是" : "否") + "\n" +
                            "§a最大堆叠数: §e" + maxStack + "\n" +
                            "§a触发概率: §e" + ClientConfig.getChance() + "%\n" +
                            "§a影响玩家: §e" + (ClientConfig.affectsPlayers() ? "是" : "否") + "\n" +
                            "§a影响Boss: §e" + (ClientConfig.affectsBosses() ? "是" : "否")
            );

            context.getSource().sendFeedback(() -> message, false);
            return Command.SINGLE_SUCCESS;
        } catch (Exception e) {
            Text errorMessage = Text.literal("§c获取配置值时发生错误: " + e.getMessage());
            context.getSource().sendError(errorMessage);
            OneShotMod.LOGGER.error("获取配置值失败", e);
            return 0;
        }
    }

    private static int setConfigValue(CommandContext<ServerCommandSource> context) {
        try {
            int maxStack = IntegerArgumentType.getInteger(context, "maxStack");

            // 这里需要调用配置系统的方法来设置值
            // 注意：由于配置系统可能是只读的，这里只是示例
            // 实际实现需要根据具体的配置系统来调整
            if (ClientConfig.getConfigProvider() != null) {
                // 尝试通过反射调用设置方法（如果存在）
                try {
                    java.lang.reflect.Method setMethod = ClientConfig.getConfigProvider().getClass()
                            .getMethod("setMaxItemStackCount", int.class);
                    setMethod.invoke(ClientConfig.getConfigProvider(), maxStack);

                    // 同步堆叠系统
                    StackSystemManager.modifyStackSystem(maxStack);

                    context.getSource().sendFeedback(() ->
                            Text.literal("§a已设置最大堆叠数为: §e" + maxStack + " §a并同步堆叠系统"), false);
                } catch (Exception e) {
                    context.getSource().sendFeedback(() ->
                            Text.literal("§6配置系统可能不支持动态修改，请在配置文件中修改后使用 §e/stack config reload"), false);
                }
            } else {
                context.getSource().sendError(Text.literal("§c配置系统未启用"));
            }

            return Command.SINGLE_SUCCESS;
        } catch (Exception e) {
            Text errorMessage = Text.literal("§c设置配置值时发生错误: " + e.getMessage());
            context.getSource().sendError(errorMessage);
            OneShotMod.LOGGER.error("设置配置值失败", e);
            return 0;
        }
    }

    private static int reloadConfig(CommandContext<ServerCommandSource> context) {
        try {
            // 调用配置重载
            ClientConfig.onConfigReload();

            // 同步堆叠系统
            int maxStack = ClientConfig.getMaxItemStackCount();
            StackSystemManager.modifyStackSystem(maxStack);

            context.getSource().sendFeedback(() ->
                    Text.literal("§a已重新加载配置，当前最大堆叠数: §e" + maxStack), false);
            return Command.SINGLE_SUCCESS;
        } catch (Exception e) {
            Text errorMessage = Text.literal("§c重新加载配置时发生错误: " + e.getMessage());
            context.getSource().sendError(errorMessage);
            OneShotMod.LOGGER.error("重新加载配置失败", e);
            return 0;
        }
    }

    private static int resetConfigDefaults(CommandContext<ServerCommandSource> context) {
        try {
            // 这里需要调用配置系统的重置方法
            // 实际实现需要根据具体的配置系统来调整
            context.getSource().sendFeedback(() ->
                    Text.literal("§6请通过配置文件手动重置为默认值，然后使用 §e/stack config reload"), false);
            return Command.SINGLE_SUCCESS;
        } catch (Exception e) {
            Text errorMessage = Text.literal("§c重置配置时发生错误: " + e.getMessage());
            context.getSource().sendError(errorMessage);
            OneShotMod.LOGGER.error("重置配置失败", e);
            return 0;
        }
    }

    // ==================== 原有命令保持不变但已清理 ItemsHelper 引用 ====================

    private static int syncWithConfig(CommandContext<ServerCommandSource> context) {
        try {
            if (ClientConfig.getConfigProvider() != null) {
                int configValue = ClientConfig.getMaxItemStackCount();
                boolean success = StackSystemManager.modifyStackSystem(configValue);

                Text message;
                if (success) {
                    message = Text.literal("§a已同步配置，堆叠大小: §e" + configValue);
                } else {
                    message = Text.literal("§c同步配置失败");
                }

                context.getSource().sendFeedback(() -> message, false);
                return success ? Command.SINGLE_SUCCESS : 0;
            } else {
                Text message = Text.literal("§c配置系统未启用，无法同步");
                context.getSource().sendError(message);
                return 0;
            }
        } catch (Exception e) {
            Text errorMessage = Text.literal("§c同步配置时发生错误: " + e.getMessage());
            context.getSource().sendError(errorMessage);
            OneShotMod.LOGGER.error("同步配置失败", e);
            return 0;
        }
    }

    private static int getConfigStatus(CommandContext<ServerCommandSource> context) {
        try {
            String syncStatus = StackSystemManager.getConfigSyncStatus();
            Text message = getConfigStatusMessage(syncStatus);
            context.getSource().sendFeedback(() -> message, false);
            return Command.SINGLE_SUCCESS;
        } catch (Exception e) {
            Text errorMessage = Text.literal("§c获取配置状态时发生错误: " + e.getMessage());
            context.getSource().sendError(errorMessage);
            OneShotMod.LOGGER.error("获取配置状态失败", e);
            return 0;
        }
    }

    // 获取配置状态信息
    private static @NotNull Text getConfigStatusMessage(String syncStatus) {
        boolean usingConfig = StackSystemManager.isUsingConfigSystem();
        String platform = getPlatformName();

        return Text.literal(
                "§6=== 配置同步状态 ===\n" +
                        "§a平台: §e" + platform + "\n" +
                        "§a配置系统: §e" + (usingConfig ? "已启用" : "未启用") + "\n" +
                        "§a同步状态: §e" + syncStatus + "\n" +
                        "§a当前堆叠大小: §e" + StackSystemManager.getCurrentStackCount() + "\n" +
                        "§a配置最大堆叠: §e" + ClientConfig.getMaxItemStackCount() + "\n" +
                        "§7使用 §e/stack sync-config §7手动同步配置\n" +
                        "§7使用 §e/stack config get §7查看完整配置"
        );
    }

    private static LiteralArgumentBuilder<ServerCommandSource> buildStackHelpCommand() {
        return CommandManager.literal("stackhelp")
                .executes(context -> {
                    Text helpMessage = Text.literal(
                            """
                                    §6=== 堆叠命令帮助 ===
                                    §a基础命令:
                                    §a/stack get §7- 查看当前堆叠数量
                                    §a/stack info §7- 查看堆叠系统详细信息
                                    §a/stack reset §7- 重置为默认堆叠数量(64)
                                    §a/stack set <数量> §7- 设置最大堆叠数量 (1-32767)
                                    §a/stack remove <数量> §7- 减少指定的堆叠数量 (1-32767)
                                    §a/stack force-update §7- 强制更新堆叠系统
                                    §a/stack sync-config §7- 同步配置文件中的堆叠设置
                                    §a/stack config-status §7- 查看配置同步状态
                                    
                                    §a配置管理命令:
                                    §a/stack config get §7- 查看当前配置值
                                    §a/stack config set <数量> §7- 设置最大堆叠数（如果支持）
                                    §a/stack config reload §7- 重新加载配置文件
                                    §a/stack config reset-defaults §7- 重置配置为默认值
                                    §e注意: 需要权限等级 2 才能使用这些命令"""
                    );
                    context.getSource().sendFeedback(() -> helpMessage, false);
                    return Command.SINGLE_SUCCESS;
                });
    }

    // 获取平台名称（用于日志）
    private static String getPlatformName() {
        try {
            // 检查是否是 NeoForge
            Class.forName("net.neoforged.fml.common.Mod");
            return "NeoForge";
        } catch (ClassNotFoundException e1) {
            try {
                // 检查是否是 Fabric
                Class.forName("net.fabricmc.api.EnvType");
                return "Fabric";
            } catch (ClassNotFoundException e2) {
                return "Unknown";
            }
        }
    }

    private static int getStackCount(CommandContext<ServerCommandSource> context) {
        try {
            int currentCount = StackSystemManager.getCurrentStackCount();
            int maxMaxCount = StackSystemManager.getCurrentMaxMaxCount();

            Text message = Text.literal("§a当前配置堆叠数量: §e" + currentCount +
                    "§a (应用于所有原版物品), 最大支持: §e" + maxMaxCount);
            context.getSource().sendFeedback(() -> message, false);

            OneShotMod.LOGGER.info("玩家 {} 查询了堆叠数量: {} (应用于所有物品)",
                    Objects.requireNonNull(context.getSource().getPlayer()).getName(), currentCount);
            return Command.SINGLE_SUCCESS;
        } catch (Exception e) {
            Text errorMessage = Text.literal("§c获取堆叠数量时发生错误: " + e.getMessage());
            context.getSource().sendError(errorMessage);
            OneShotMod.LOGGER.error("获取堆叠数量失败", e);
            return 0;
        }
    }

    private static int getStackInfo(CommandContext<ServerCommandSource> context) {
        try {
            String systemStatus = StackSystemManager.getSystemStatus();
            boolean usingConfig = StackSystemManager.isUsingConfigSystem();
            Text message = getStackInfoMessage(systemStatus, usingConfig);
            context.getSource().sendFeedback(() -> message, false);

            return Command.SINGLE_SUCCESS;
        } catch (Exception e) {
            Text errorMessage = Text.literal("§c获取堆叠信息时发生错误: " + e.getMessage());
            context.getSource().sendError(errorMessage);
            OneShotMod.LOGGER.error("获取堆叠信息失败", e);
            return 0;
        }
    }

    // 获取堆叠系统信息
    private static @NotNull Text getStackInfoMessage(String systemStatus, boolean usingConfig) {
        String syncStatus = StackSystemManager.getConfigSyncStatus();

        return Text.literal(
                "§6=== 堆叠系统信息 ===\n" +
                        "§a" + systemStatus + "\n" +
                        "§a配置同步: §e" + syncStatus + "\n" +
                        "§a支持范围: §e1 - " + MAX_STACK_COUNT + "\n" +
                        "§a配置来源: §e" + (usingConfig ? "主配置文件" : "独立配置文件") + "\n" +
                        "§a当前配置: §e" + ClientConfig.getMaxItemStackCount() + "\n" +
                        "§c注意: 此设置将影响所有原版物品，包括工具、武器和盔甲\n" +
                        "§7使用 §e/stack force-update §7强制刷新系统\n" +
                        "§7使用 §e/stack sync-config §7同步配置文件\n" +
                        "§7使用 §e/stack config get §7查看完整配置"
        );
    }

    private static int forceUpdateStackSystem(CommandContext<ServerCommandSource> context) {
        try {
            boolean success = StackSystemManager.forceUpdate();

            Text message;
            if (success) {
                int currentCount = StackSystemManager.getCurrentStackCount();
                message = Text.literal("§a已强制更新堆叠系统，当前值: §e" + currentCount + " (应用于所有物品)");
            } else {
                message = Text.literal("§c强制更新堆叠系统失败");
            }

            context.getSource().sendFeedback(() -> message, false);
            return success ? Command.SINGLE_SUCCESS : 0;

        } catch (Exception e) {
            Text errorMessage = Text.literal("§c强制更新堆叠系统时发生错误: " + e.getMessage());
            context.getSource().sendError(errorMessage);
            OneShotMod.LOGGER.error("强制更新堆叠系统失败", e);
            return 0;
        }
    }

    private static int resetStackCount(CommandContext<ServerCommandSource> context) {
        try {
            int defaultCount = 64;

            // 使用 StackSystemManager 修改堆叠系统
            boolean success = StackSystemManager.modifyStackSystem(defaultCount);

            Text message;
            if (success) {
                message = Text.literal("§a已重置最大物品堆叠数量为默认值: §e" + defaultCount);
            } else {
                message = Text.literal("§c重置堆叠数量失败");
            }

            Text finalMessage = message;
            context.getSource().sendFeedback(() -> finalMessage, false);

            OneShotMod.LOGGER.info("玩家 {} 重置了最大堆叠数量为: {}",
                    Objects.requireNonNull(context.getSource().getPlayer()).getName(), defaultCount);
            return success ? Command.SINGLE_SUCCESS : 0;

        } catch (Exception e) {
            Text errorMessage = Text.literal("§c重置堆叠数量时发生错误: " + e.getMessage());
            context.getSource().sendError(errorMessage);
            OneShotMod.LOGGER.error("重置堆叠数量失败", e);
            return 0;
        }
    }

    private static int setStackCount(CommandContext<ServerCommandSource> context) {
        try {
            int newCount = IntegerArgumentType.getInteger(context, "maxCountValue");

            // 验证数值范围
            if (newCount < MIN_STACK_COUNT || newCount > MAX_STACK_COUNT) {
                Text errorMessage = Text.literal("§c堆叠数量必须在 " + MIN_STACK_COUNT + " 到 " + MAX_STACK_COUNT + " 之间");
                context.getSource().sendError(errorMessage);
                return 0;
            }

            // 警告信息
            if (newCount > 1000) {
                Text warningMessage = Text.literal("§6警告: 设置极高的堆叠数量 (" + newCount + ") 可能会影响游戏性能!");
                context.getSource().sendFeedback(() -> warningMessage, true);
            }

            // 使用 StackSystemManager 修改堆叠系统
            boolean success = StackSystemManager.modifyStackSystem(newCount);

            Text message;
            if (success) {
                message = Text.literal("§a已设置所有物品的最大堆叠数量为: §e" + newCount);
            } else {
                message = Text.literal("§c设置堆叠数量失败");
            }

            Text finalMessage = message;
            context.getSource().sendFeedback(() -> finalMessage, false);

            OneShotMod.LOGGER.info("玩家 {} 设置了所有物品的最大堆叠数量为: {}",
                    Objects.requireNonNull(context.getSource().getPlayer()).getName(), newCount);
            return success ? Command.SINGLE_SUCCESS : 0;

        } catch (Exception e) {
            Text errorMessage = Text.literal("§c设置堆叠数量时发生错误: " + e.getMessage());
            context.getSource().sendError(errorMessage);
            OneShotMod.LOGGER.error("设置堆叠数量失败", e);
            return 0;
        }
    }

    private static int removeStackCount(CommandContext<ServerCommandSource> context) {
        try {
            int removeCount = IntegerArgumentType.getInteger(context, "count");
            int currentCount = StackSystemManager.getCurrentStackCount();
            int newCount = Math.max(MIN_STACK_COUNT, currentCount - removeCount);

            // 验证减少后的数值范围
            if (newCount < MIN_STACK_COUNT) {
                Text errorMessage = Text.literal("§c堆叠数量不能低于 " + MIN_STACK_COUNT);
                context.getSource().sendError(errorMessage);
                return 0;
            }

            // 使用 StackSystemManager 修改堆叠系统
            boolean success = StackSystemManager.modifyStackSystem(newCount);

            Text message;
            if (success) {
                message = Text.literal("§a已减少物品堆叠数量 §c" + removeCount +
                        "§a，新的堆叠数量: §e" + newCount);
            } else {
                message = Text.literal("§c减少堆叠数量失败");
            }

            Text finalMessage = message;
            context.getSource().sendFeedback(() -> finalMessage, false);

            OneShotMod.LOGGER.info("玩家 {} 减少了堆叠数量 {}，新数量: {}",
                    Objects.requireNonNull(context.getSource().getPlayer()).getName(), removeCount, newCount);
            return success ? Command.SINGLE_SUCCESS : 0;

        } catch (Exception e) {
            Text errorMessage = Text.literal("§c减少堆叠数量时发生错误: " + e.getMessage());
            context.getSource().sendError(errorMessage);
            OneShotMod.LOGGER.error("减少堆叠数量失败", e);
            return 0;
        }
    }
}