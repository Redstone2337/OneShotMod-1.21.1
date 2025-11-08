package net.redstone233.nsp.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import dev.architectury.event.events.common.CommandRegistrationEvent;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.redstone233.nsp.OneShotMod;
import net.redstone233.nsp.config.ClientConfig;
import net.redstone233.nsp.util.ItemsHelper;
import net.redstone233.nsp.util.StackSystemManager;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
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
                // 新增的 ItemsHelper 管理命令
                .then(CommandManager.literal("items")
                        .then(CommandManager.literal("list-modified")
                                .executes(StackCommand::listModifiedItems))
                        .then(CommandManager.literal("reset-all")
                                .executes(StackCommand::resetAllItems))
                        .then(CommandManager.literal("reset-item")
                                .then(CommandManager.argument("item_id", StringArgumentType.string())
                                        .executes(StackCommand::resetSingleItem)))
                        .then(CommandManager.literal("set-item")
                                .then(CommandManager.argument("item_id", StringArgumentType.string())
                                        .then(CommandManager.argument("count", IntegerArgumentType.integer(MIN_STACK_COUNT, MAX_STACK_COUNT))
                                                .executes(StackCommand::setSingleItem))))
                        .then(CommandManager.literal("get-config")
                                .executes(StackCommand::getItemsConfig))
                        .then(CommandManager.literal("batch-set")
                                .then(CommandManager.argument("original_size", IntegerArgumentType.integer(MIN_STACK_COUNT, MAX_STACK_COUNT))
                                        .then(CommandManager.argument("new_size", IntegerArgumentType.integer(MIN_STACK_COUNT, MAX_STACK_COUNT))
                                                .then(CommandManager.argument("type", StringArgumentType.string())
                                                        .executes(StackCommand::batchSetItems)))))
                        .then(CommandManager.literal("stats")
                                .executes(StackCommand::getItemsStats)));
    }

    // ==================== 新增的 ItemsHelper 管理命令实现 ====================

    private static int listModifiedItems(CommandContext<ServerCommandSource> context) {
        try {
            ItemsHelper helper = ItemsHelper.getItemsHelper();
            LinkedList<Item> modifiedItems = helper.getAllModifiedItems();

            if (modifiedItems.isEmpty()) {
                context.getSource().sendFeedback(() -> Text.literal("§a没有已修改的物品"), false);
                return Command.SINGLE_SUCCESS;
            }

            StringBuilder message = new StringBuilder("§6=== 已修改的物品列表 ===\n");
            int count = 0;
            for (Item item : modifiedItems) {
                if (count >= 20) { // 限制显示数量
                    message.append("§7... 还有更多物品未显示\n");
                    break;
                }
                String itemId = Registries.ITEM.getId(item).toString();
                int currentCount = helper.getCurrentCount(item);
                int defaultCount = helper.getDefaultCount(item);
                message.append("§a- ").append(itemId)
                        .append(" §7(默认: ").append(defaultCount)
                        .append(" → 当前: ").append(currentCount).append(")\n");
                count++;
            }
            message.append("§a总计: ").append(modifiedItems.size()).append(" 个物品被修改");

            context.getSource().sendFeedback(() -> Text.literal(message.toString()), false);
            return Command.SINGLE_SUCCESS;
        } catch (Exception e) {
            Text errorMessage = Text.literal("§c获取修改物品列表时发生错误: " + e.getMessage());
            context.getSource().sendError(errorMessage);
            OneShotMod.LOGGER.error("获取修改物品列表失败", e);
            return 0;
        }
    }

    private static int resetAllItems(CommandContext<ServerCommandSource> context) {
        try {
            ItemsHelper helper = ItemsHelper.getItemsHelper();
            helper.resetAll(true);

            context.getSource().sendFeedback(() -> Text.literal("§a已重置所有物品的堆叠数量为默认值"), false);
            OneShotMod.LOGGER.info("玩家 {} 重置了所有物品的堆叠数量",
                    Objects.requireNonNull(context.getSource().getPlayer()).getName());
            return Command.SINGLE_SUCCESS;
        } catch (Exception e) {
            Text errorMessage = Text.literal("§c重置所有物品时发生错误: " + e.getMessage());
            context.getSource().sendError(errorMessage);
            OneShotMod.LOGGER.error("重置所有物品失败", e);
            return 0;
        }
    }

    private static int resetSingleItem(CommandContext<ServerCommandSource> context) {
        try {
            String itemId = StringArgumentType.getString(context, "item_id");
            Item item = Registries.ITEM.get(Identifier.of(itemId));

            if (item == null) {
                context.getSource().sendError(Text.literal("§c找不到物品: " + itemId));
                return 0;
            }

            ItemsHelper helper = ItemsHelper.getItemsHelper();
            helper.resetItem(item);

            context.getSource().sendFeedback(() -> Text.literal("§a已重置物品 " + itemId + " 的堆叠数量为默认值"), false);
            OneShotMod.LOGGER.info("玩家 {} 重置了物品 {} 的堆叠数量",
                    Objects.requireNonNull(context.getSource().getPlayer()).getName(), itemId);
            return Command.SINGLE_SUCCESS;
        } catch (Exception e) {
            Text errorMessage = Text.literal("§c重置物品时发生错误: " + e.getMessage());
            context.getSource().sendError(errorMessage);
            OneShotMod.LOGGER.error("重置物品失败", e);
            return 0;
        }
    }

    private static int setSingleItem(CommandContext<ServerCommandSource> context) {
        try {
            String itemId = StringArgumentType.getString(context, "item_id");
            int count = IntegerArgumentType.getInteger(context, "count");

            Item item = Registries.ITEM.get(Identifier.of(itemId));
            if (item == null) {
                context.getSource().sendError(Text.literal("§c找不到物品: " + itemId));
                return 0;
            }

            ItemsHelper helper = ItemsHelper.getItemsHelper();
            helper.setSingle(item, count);

            context.getSource().sendFeedback(() -> Text.literal("§a已设置物品 " + itemId + " 的堆叠数量为: " + count), false);
            OneShotMod.LOGGER.info("玩家 {} 设置了物品 {} 的堆叠数量为 {}",
                    Objects.requireNonNull(context.getSource().getPlayer()).getName(), itemId, count);
            return Command.SINGLE_SUCCESS;
        } catch (Exception e) {
            Text errorMessage = Text.literal("§c设置物品堆叠数量时发生错误: " + e.getMessage());
            context.getSource().sendError(errorMessage);
            OneShotMod.LOGGER.error("设置物品堆叠数量失败", e);
            return 0;
        }
    }

    private static int getItemsConfig(CommandContext<ServerCommandSource> context) {
        try {
            ItemsHelper helper = ItemsHelper.getItemsHelper();
            LinkedHashMap<String, Integer> configMap = helper.getNewConfigMap();

            if (configMap.isEmpty()) {
                context.getSource().sendFeedback(() -> Text.literal("§a没有自定义的物品堆叠配置"), false);
                return Command.SINGLE_SUCCESS;
            }

            StringBuilder message = new StringBuilder("§6=== 当前物品堆叠配置 ===\n");
            int count = 0;
            for (Map.Entry<String, Integer> entry : configMap.entrySet()) {
                if (count >= 15) { // 限制显示数量
                    message.append("§7... 还有更多配置未显示\n");
                    break;
                }
                message.append("§a- ").append(entry.getKey()).append(": ").append(entry.getValue()).append("\n");
                count++;
            }
            message.append("§a总计: ").append(configMap.size()).append(" 个自定义配置");

            context.getSource().sendFeedback(() -> Text.literal(message.toString()), false);
            return Command.SINGLE_SUCCESS;
        } catch (Exception e) {
            Text errorMessage = Text.literal("§c获取物品配置时发生错误: " + e.getMessage());
            context.getSource().sendError(errorMessage);
            OneShotMod.LOGGER.error("获取物品配置失败", e);
            return 0;
        }
    }

    private static int batchSetItems(CommandContext<ServerCommandSource> context) {
        try {
            int originalSize = IntegerArgumentType.getInteger(context, "original_size");
            int newSize = IntegerArgumentType.getInteger(context, "new_size");
            String type = StringArgumentType.getString(context, "type");

            // 验证类型参数
            if (!type.equals("vanilla") && !type.equals("modified") && !type.equals("all")) {
                context.getSource().sendError(Text.literal("§c类型参数必须是: vanilla, modified 或 all"));
                return 0;
            }

            ItemsHelper helper = ItemsHelper.getItemsHelper();
            int affectedCount = helper.setMatchedItems(originalSize, newSize, type);

            String typeName = switch (type) {
                case "vanilla" -> "原版";
                case "modified" -> "已修改";
                case "all" -> "所有";
                default -> type;
            };

            context.getSource().sendFeedback(() ->
                    Text.literal("§a批量设置完成: 将" + typeName + "堆叠数量为 " + originalSize +
                            " 的物品设置为 " + newSize + "，影响了 " + affectedCount + " 个物品"), false);

            OneShotMod.LOGGER.info("玩家 {} 批量设置了物品堆叠: {} {} -> {} ({} 个物品)",
                    Objects.requireNonNull(context.getSource().getPlayer()).getName(),
                    typeName, originalSize, newSize, affectedCount);
            return Command.SINGLE_SUCCESS;
        } catch (Exception e) {
            Text errorMessage = Text.literal("§c批量设置物品时发生错误: " + e.getMessage());
            context.getSource().sendError(errorMessage);
            OneShotMod.LOGGER.error("批量设置物品失败", e);
            return 0;
        }
    }

    private static int getItemsStats(CommandContext<ServerCommandSource> context) {
        try {
            ItemsHelper helper = ItemsHelper.getItemsHelper();
            LinkedList<Item> modifiedItems = helper.getAllModifiedItems();
            Text message = getMessage(modifiedItems);

            context.getSource().sendFeedback(() -> message, false);
            return Command.SINGLE_SUCCESS;
        } catch (Exception e) {
            Text errorMessage = Text.literal("§c获取物品统计时发生错误: " + e.getMessage());
            context.getSource().sendError(errorMessage);
            OneShotMod.LOGGER.error("获取物品统计失败", e);
            return 0;
        }
    }

    private static @NotNull Text getMessage(LinkedList<Item> modifiedItems) {
        int totalItems = Registries.ITEM.getEntrySet().size();

        int maxAllowed = ItemsHelper.getItemMaxCount();
        int currentConfigCount = ClientConfig.getMaxItemStackCount();

        return Text.literal(
                "§6=== 物品堆叠统计 ===\n" +
                        "§a总物品数量: §e" + totalItems + "\n" +
                        "§a已修改物品: §e" + modifiedItems.size() + "\n" +
                        "§a修改比例: §e" + String.format("%.1f", (modifiedItems.size() * 100.0 / totalItems)) + "%\n" +
                        "§a配置最大堆叠: §e" + currentConfigCount + "\n" +
                        "§a实际最大堆叠: §e" + maxAllowed + "\n" +
                        "§a使用 §e/stack items list-modified §a查看详细列表"
        );
    }

    // ==================== 原有命令保持不变 ====================

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
            Text message = getMessage(syncStatus);
            context.getSource().sendFeedback(() -> message, false);
            return Command.SINGLE_SUCCESS;
        } catch (Exception e) {
            Text errorMessage = Text.literal("§c获取配置状态时发生错误: " + e.getMessage());
            context.getSource().sendError(errorMessage);
            OneShotMod.LOGGER.error("获取配置状态失败", e);
            return 0;
        }
    }

    private static @NotNull Text getMessage(String syncStatus) {
        boolean usingConfig = StackSystemManager.isUsingConfigSystem();
        String platform = getPlatformName();

        return Text.literal(
                "§6=== 配置同步状态 ===\n" +
                        "§a平台: §e" + platform + "\n" +
                        "§a配置系统: §e" + (usingConfig ? "已启用" : "未启用") + "\n" +
                        "§a同步状态: §e" + syncStatus + "\n" +
                        "§a当前堆叠大小: §e" + StackSystemManager.getCurrentStackCount() + "\n" +
                        "§7使用 §e/stack sync-config §7手动同步配置"
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
                                    
                                    §a物品管理命令:
                                    §a/stack items list-modified §7- 列出所有已修改的物品
                                    §a/stack items reset-all §7- 重置所有物品的堆叠数量
                                    §a/stack items reset-item <物品ID> §7- 重置单个物品的堆叠数量
                                    §a/stack items set-item <物品ID> <数量> §7- 设置单个物品的堆叠数量
                                    §a/stack items get-config §7- 查看当前物品堆叠配置
                                    §a/stack items batch-set <原数量> <新数量> <类型> §7- 批量设置物品堆叠
                                    §7  (类型: vanilla-原版, modified-已修改, all-所有)
                                    §a/stack items stats §7- 查看物品堆叠统计
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
            String syncStatus = StackSystemManager.getConfigSyncStatus();

            Text message = Text.literal(
                    "§6=== 堆叠系统信息 ===\n" +
                            "§a" + systemStatus + "\n" +
                            "§a配置同步: §e" + syncStatus + "\n" +
                            "§a支持范围: §e1 - " + MAX_STACK_COUNT + "\n" +
                            "§a配置来源: §e" + (usingConfig ? "主配置文件" : "独立配置文件") + "\n" +
                            "§c注意: 此设置将影响所有原版物品，包括工具、武器和盔甲\n" +
                            "§7使用 §e/stack force-update §7强制刷新系统\n" +
                            "§7使用 §e/stack sync-config §7同步配置文件"
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