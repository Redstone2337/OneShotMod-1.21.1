package net.redstone233.nsp.fabric.screen;

import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import net.redstone233.nsp.fabric.config.FabricConfigImpl;

import java.util.Arrays;

public class FabricConfigScreen {

    public static Screen createConfigScreen(Screen parent) {
        // 初始化ConfigBuilder，设置父屏幕和标题（使用硬编码文本）
        // 当用户点击保存时执行
        ConfigBuilder builder = ConfigBuilder.create()
                .setParentScreen(parent)
                .setTitle(Text.literal("一击必杀模组配置"))
                .setSavingRunnable(FabricConfigImpl::saveConfig);

        // 获取EntryBuilder用于创建配置项
        ConfigEntryBuilder entryBuilder = builder.entryBuilder();

        // ==================== 功能开关分类 ====================
        ConfigCategory features = builder.getOrCreateCategory(Text.literal("功能开关"));

        features.addEntry(entryBuilder.startBooleanToggle(Text.literal("启用模组"), FabricConfigImpl.ENABLED.get())
                .setDefaultValue(true)
                .setTooltip(Text.literal("是否启用一击必杀模组"))
                .setSaveConsumer(FabricConfigImpl.ENABLED::set)
                .build());

        features.addEntry(entryBuilder.startBooleanToggle(Text.literal("调试模式"), FabricConfigImpl.DEBUG_MODE.get())
                .setDefaultValue(false)
                .setTooltip(Text.literal("启用调试模式，在控制台输出详细信息"))
                .setSaveConsumer(FabricConfigImpl.DEBUG_MODE::set)
                .build());

        // ==================== 触发条件分类 ====================
        ConfigCategory trigger = builder.getOrCreateCategory(Text.literal("触发条件"));

        trigger.addEntry(entryBuilder.startIntSlider(Text.literal("触发概率"), FabricConfigImpl.CHANCE.get(), 0, 100)
                .setDefaultValue(100)
                .setTooltip(Text.literal("一击必杀触发概率 (0-100)\n0 = 从不触发, 100 = 每次攻击都触发"))
                .setSaveConsumer(FabricConfigImpl.CHANCE::set)
                .build());

        trigger.addEntry(entryBuilder.startBooleanToggle(Text.literal("要求空手"), FabricConfigImpl.REQUIRE_EMPTY_HAND.get())
                .setDefaultValue(false)
                .setTooltip(Text.literal("是否要求空手才能触发一击必杀"))
                .setSaveConsumer(FabricConfigImpl.REQUIRE_EMPTY_HAND::set)
                .build());

        trigger.addEntry(entryBuilder.startBooleanToggle(Text.literal("影响玩家"), FabricConfigImpl.AFFECT_PLAYERS.get())
                .setDefaultValue(false)
                .setTooltip(Text.literal("一击必杀是否对玩家生效\n警告：启用此选项可能导致PvP不平衡"))
                .setSaveConsumer(FabricConfigImpl.AFFECT_PLAYERS::set)
                .build());

        trigger.addEntry(entryBuilder.startBooleanToggle(Text.literal("影响Boss"), FabricConfigImpl.AFFECT_BOSSES.get())
                .setDefaultValue(false)
                .setTooltip(Text.literal("一击必杀是否对Boss生物生效\n如末影龙、凋灵等"))
                .setSaveConsumer(FabricConfigImpl.AFFECT_BOSSES::set)
                .build());

        trigger.addEntry(entryBuilder.startStrList(Text.literal("实体黑名单"),
                        Arrays.asList(FabricConfigImpl.BLACKLISTED_ENTITIES.get().toArray(new String[0])))
                .setDefaultValue(Arrays.asList("minecraft:wither", "minecraft:ender_dragon"))
                .setTooltip(Text.literal("黑名单实体ID列表\n这些实体不会被一击必杀影响"))
                .setSaveConsumer(list -> FabricConfigImpl.BLACKLISTED_ENTITIES.set(list))
                .build());

        // ==================== 消息设置分类 ====================
        ConfigCategory message = builder.getOrCreateCategory(Text.literal("消息设置"));

        message.addEntry(entryBuilder.startTextField(Text.literal("消息格式"), FabricConfigImpl.MESSAGE_FORMAT.get())
                .setDefaultValue("§6[§e%player%§6] §a触发一击必杀，已抹除 §c[§4%entity%§c]")
                .setTooltip(Text.literal("一击必杀触发时显示的消息格式\n可用占位符:\n  %player% - 玩家名称\n  %entity% - 实体名称\n支持颜色代码，使用§符号"))
                .setSaveConsumer(FabricConfigImpl.MESSAGE_FORMAT::set)
                .build());

        message.addEntry(entryBuilder.startBooleanToggle(Text.literal("广播消息"), FabricConfigImpl.BROADCAST_MESSAGE.get())
                .setDefaultValue(true)
                .setTooltip(Text.literal("是否向服务器所有玩家广播消息\n如果禁用，只有触发玩家能看到消息"))
                .setSaveConsumer(FabricConfigImpl.BROADCAST_MESSAGE::set)
                .build());

        message.addEntry(entryBuilder.startBooleanToggle(Text.literal("动作栏提示"), FabricConfigImpl.SHOW_IN_ACTIONBAR.get())
                .setDefaultValue(false)
                .setTooltip(Text.literal("是否在动作栏显示简短提示\n如果启用，会在玩家动作栏显示简化的提示信息"))
                .setSaveConsumer(FabricConfigImpl.SHOW_IN_ACTIONBAR::set)
                .build());

        return builder.build();
    }
}
