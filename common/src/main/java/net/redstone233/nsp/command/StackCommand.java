// StackCommand.java
package net.redstone233.nsp.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import dev.architectury.event.events.common.CommandRegistrationEvent;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.redstone233.nsp.OneShotMod;
import net.redstone233.nsp.util.StackSystemManager;

import java.util.Objects;

public class StackCommand {

    // 定义堆叠数量的最小和最大值
    private static final int MIN_STACK_COUNT = 1;
    private static final int MAX_STACK_COUNT = 32767;

    public static void register() {
        CommandRegistrationEvent.EVENT.register((dispatcher, registry, selection) -> {
            registerCommand(dispatcher);
            registerHelpCommand(dispatcher);
        });
    }

    private static void registerCommand(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("stack")
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
        );
    }

    private static int getStackCount(CommandContext<ServerCommandSource> context) {
        try {
            int currentCount = StackSystemManager.getCurrentStackCount();
            int maxMaxCount = StackSystemManager.getCurrentMaxMaxCount();

            Text message = Text.literal("§a当前配置堆叠数量: §e" + currentCount +
                    "§a, MAX_MAX_COUNT: §e" + maxMaxCount);
            context.getSource().sendFeedback(() -> message, false);

            OneShotMod.LOGGER.info("玩家 {} 查询了堆叠数量: {}, MAX_MAX_COUNT: {}",
                    Objects.requireNonNull(context.getSource().getPlayer()).getName(), currentCount, maxMaxCount);
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

            Text message = Text.literal(
                    "§6=== 堆叠系统信息 ===\n" +
                            "§a" + systemStatus + "\n" +
                            "§a支持范围: §e" + MIN_STACK_COUNT + " - " + MAX_STACK_COUNT + "\n" +
                            "§7使用 §e/stack force-update §7强制更新系统"
            );
            context.getSource().sendFeedback(() -> message, false);

            return Command.SINGLE_SUCCESS;
        } catch (Exception e) {
            Text errorMessage = Text.literal("§c获取堆叠信息时发生错误: " + e.getMessage());
            context.getSource().sendError(errorMessage);
            OneShotMod.LOGGER.error("获取堆叠信息失败", e);
            return 0;
        }
    }

    private static int forceUpdateStackSystem(CommandContext<ServerCommandSource> context) {
        try {
            int currentCount = StackSystemManager.getCurrentStackCount();

            // 强制更新堆叠系统
            boolean success = StackSystemManager.modifyStackSystem(currentCount);

            Text message;
            if (success) {
                message = Text.literal("§a已强制更新堆叠系统，当前值: §e" + currentCount);
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

            // 保存配置
            boolean saved = StackSystemManager.saveConfigToFile(defaultCount);

            Text message;
            if (success) {
                message = Text.literal("§a已重置最大物品堆叠数量为默认值: §e" + defaultCount);
                if (!saved) {
                    message = Text.literal("§e已重置堆叠数量，但配置保存失败");
                }
            } else {
                message = Text.literal("§c重置堆叠数量失败");
            }

            Text finalMessage = message;
            context.getSource().sendFeedback(() -> finalMessage, false);

            OneShotMod.LOGGER.info("玩家 {} 重置了最大堆叠数量为: {}, 保存配置: {}",
                    Objects.requireNonNull(context.getSource().getPlayer()).getName(), defaultCount, saved ? "成功" : "失败");
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

            // 使用 StackSystemManager 修改堆叠系统
            boolean success = StackSystemManager.modifyStackSystem(newCount);

            // 保存配置
            boolean saved = StackSystemManager.saveConfigToFile(newCount);

            Text message;
            if (success) {
                message = Text.literal("§a已设置最大物品堆叠数量为: §e" + newCount);
                if (!saved) {
                    message = Text.literal("§e已设置堆叠数量，但配置保存失败");
                }
            } else {
                message = Text.literal("§c设置堆叠数量失败");
            }

            Text finalMessage = message;
            context.getSource().sendFeedback(() -> finalMessage, false);

            OneShotMod.LOGGER.info("玩家 {} 设置了最大堆叠数量为: {}, 保存配置: {}",
                    Objects.requireNonNull(context.getSource().getPlayer()).getName(), newCount, saved ? "成功" : "失败");
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

            // 保存配置
            boolean saved = StackSystemManager.saveConfigToFile(newCount);

            Text message;
            if (success) {
                message = Text.literal("§a已减少物品堆叠数量 §c" + removeCount +
                        "§a，新的堆叠数量: §e" + newCount);
                if (!saved) {
                    message = Text.literal("§e已减少堆叠数量，但配置保存失败");
                }
            } else {
                message = Text.literal("§c减少堆叠数量失败");
            }

            Text finalMessage = message;
            context.getSource().sendFeedback(() -> finalMessage, false);

            OneShotMod.LOGGER.info("玩家 {} 减少了堆叠数量 {}，新数量: {}, 保存配置: {}",
                    Objects.requireNonNull(context.getSource().getPlayer()).getName(), removeCount, newCount, saved ? "成功" : "失败");
            return success ? Command.SINGLE_SUCCESS : 0;

        } catch (Exception e) {
            Text errorMessage = Text.literal("§c减少堆叠数量时发生错误: " + e.getMessage());
            context.getSource().sendError(errorMessage);
            OneShotMod.LOGGER.error("减少堆叠数量失败", e);
            return 0;
        }
    }

    // 帮助命令
    private static void registerHelpCommand(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("stackhelp")
                .executes(context -> {
                    Text helpMessage = Text.literal(
                            "§6=== 堆叠命令帮助 ===\n" +
                                    "§a/stack get §7- 查看当前堆叠数量\n" +
                                    "§a/stack info §7- 查看堆叠系统详细信息\n" +
                                    "§a/stack reset §7- 重置为默认堆叠数量(64)\n" +
                                    "§a/stack set <数量> §7- 设置最大堆叠数量 (1-32767)\n" +
                                    "§a/stack remove <数量> §7- 减少指定的堆叠数量 (1-32767)\n" +
                                    "§a/stack force-update §7- 强制更新堆叠系统\n" +
                                    "§e注意: 需要权限等级 2 才能使用这些命令"
                    );
                    context.getSource().sendFeedback(() -> helpMessage, false);
                    return Command.SINGLE_SUCCESS;
                })
        );
    }
}