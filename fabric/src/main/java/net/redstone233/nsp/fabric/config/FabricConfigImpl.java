package net.redstone233.nsp.fabric.config;

import fuzs.forgeconfigapiport.fabric.api.neoforge.v4.NeoForgeConfigRegistry;
import fuzs.forgeconfigapiport.fabric.api.neoforge.v4.NeoForgeModConfigEvents;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.*;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.ModConfigSpec;
import net.redstone233.nsp.OneShotMod;
import net.redstone233.nsp.config.ClientConfig;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class FabricConfigImpl implements ClientConfig.ConfigProvider {

    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    // ==================== 功能开关设置 ====================
    private static void setupFeatureSettings() {
        BUILDER.push("features");

        ENABLED = BUILDER
                .comment("是否启用一击必杀模组")
                .define("enabled", true);

        DEBUG_MODE = BUILDER
                .comment("启用调试模式，在控制台输出详细信息")
                .define("debugMode", false);

        BUILDER.pop();
    }

    // ==================== 触发条件设置 ====================
    private static void setupTriggerSettings() {
        BUILDER.push("trigger");

        CHANCE = BUILDER
                .comment("一击必杀触发概率 (0-100)", "0 = 从不触发, 100 = 每次攻击都触发")
                .defineInRange("chance", 100, 0, 100);

        REQUIRE_EMPTY_HAND = BUILDER
                .comment("是否要求空手才能触发一击必杀")
                .define("requireEmptyHand", false);

        AFFECT_PLAYERS = BUILDER
                .comment("一击必杀是否对玩家生效", "警告：启用此选项可能导致PvP不平衡")
                .define("affectPlayers", false);

        AFFECT_BOSSES = BUILDER
                .comment("一击必杀是否对Boss生物生效", "如末影龙、凋灵等")
                .define("affectBosses", false);

        BLACKLISTED_ENTITIES = BUILDER
                .comment("黑名单实体ID列表", "这些实体不会被一击必杀影响")
                .defineList("blacklistedEntities",
                        Arrays.asList(
                                "minecraft:wither",
                                "minecraft:ender_dragon"
                        ),
                        FabricConfigImpl::validateString);

        BUILDER.pop();
    }

    // ==================== 消息显示设置 ====================
    private static void setupMessageSettings() {
        BUILDER.push("message");

        MESSAGE_FORMAT = BUILDER
                .comment(
                        "一击必杀触发时显示的消息格式",
                        "可用占位符:",
                        "  %player% - 玩家名称",
                        "  %entity% - 实体名称",
                        "支持颜色代码，使用§符号，如 §c红色文本"
                )
                .define("format", "§6[§e%player%§6] §a触发一击必杀，已抹除 §c[§4%entity%§c]");

        BROADCAST_MESSAGE = BUILDER
                .comment("是否向服务器所有玩家广播消息", "如果禁用，只有触发玩家能看到消息")
                .define("broadcast", true);

        SHOW_IN_ACTIONBAR = BUILDER
                .comment("是否在动作栏显示简短提示", "如果启用，会在玩家动作栏显示简化的提示信息")
                .define("showInActionbar", false);

        BUILDER.pop();
    }

    // 配置字段声明
    public static ModConfigSpec.BooleanValue ENABLED;
    public static ModConfigSpec.BooleanValue DEBUG_MODE;
    public static ModConfigSpec.IntValue CHANCE;
    public static ModConfigSpec.BooleanValue REQUIRE_EMPTY_HAND;
    public static ModConfigSpec.BooleanValue AFFECT_PLAYERS;
    public static ModConfigSpec.BooleanValue AFFECT_BOSSES;
    public static ModConfigSpec.ConfigValue<List<? extends String>> BLACKLISTED_ENTITIES;
    public static ModConfigSpec.ConfigValue<String> MESSAGE_FORMAT;
    public static ModConfigSpec.BooleanValue BROADCAST_MESSAGE;
    public static ModConfigSpec.BooleanValue SHOW_IN_ACTIONBAR;

    static {
        // 按顺序初始化各个配置节
        setupFeatureSettings();
        setupTriggerSettings();
        setupMessageSettings();
    }

    public static final ModConfigSpec SPEC = BUILDER.build();

    // ==================== 配置提供者接口实现 ====================

    @Override
    public boolean isEnabled() {
        return ENABLED.get();
    }

    @Override
    public int getChance() {
        return CHANCE.get();
    }

    @Override
    public boolean requiresEmptyHand() {
        return REQUIRE_EMPTY_HAND.get();
    }

    @Override
    public boolean affectsPlayers() {
        return AFFECT_PLAYERS.get();
    }

    @Override
    public boolean affectsBosses() {
        return AFFECT_BOSSES.get();
    }

    @Override
    public List<? extends String> getBlacklistedEntities() {
        return BLACKLISTED_ENTITIES.get();
    }

    @Override
    public boolean shouldBroadcastMessage() {
        return BROADCAST_MESSAGE.get();
    }

    @Override
    public boolean shouldShowInActionbar() {
        return SHOW_IN_ACTIONBAR.get();
    }

    @Override
    public boolean isDebugMode() {
        return DEBUG_MODE.get();
    }

    @Override
    public String getMessage(String playerName, String entityName) {
        return MESSAGE_FORMAT.get().replace("%player%", playerName).replace("%entity%", entityName);
    }

    // ==================== 配置管理方法 ====================

    // ==================== 功能开关 ====================
    public static boolean shouldEnable() {
        return ENABLED.get();
    }

    public static boolean isInDebugMode() {
        return DEBUG_MODE.get();
    }

    // ==================== 触发设置 ====================
    public static int getTriggerChance() {
        return CHANCE.get();
    }

//    public static boolean requiresEmptyHand() {
//        return REQUIRE_EMPTY_HAND.get();
//    }

    public static boolean shouldAffectPlayers() {
        return AFFECT_PLAYERS.get();
    }

    public static boolean shouldAffectBosses() {
        return AFFECT_BOSSES.get();
    }

    public static List<String> getEntityBlacklist() {
        try {
            // 安全地转换类型
            List<?> rawList = BLACKLISTED_ENTITIES.get();
            List<String> result = new ArrayList<>();

            for (Object item : rawList) {
                if (item instanceof String) {
                    result.add((String) item);
                }
            }

            return result;
        } catch (Exception e) {
            OneShotMod.LOGGER.warn("获取黑名单实体时出错，使用默认值", e);
            return getDefaultBlacklistedEntities();
        }
    }

    private static List<String> getDefaultBlacklistedEntities() {
        return List.of("minecraft:wither", "minecraft:ender_dragon");
    }

    // ==================== 消息设置 ====================
    public static String getMessageFormat() {
        return MESSAGE_FORMAT.get();
    }

    public static boolean shouldBroadcast() {
        return BROADCAST_MESSAGE.get();
    }

    public static boolean shouldShowActionbar() {
        return SHOW_IN_ACTIONBAR.get();
    }

    // ==================== 辅助方法 ====================

    /**
     * 获取格式化的消息
     */
    public static String getFormattedMessage(String playerName, String entityName) {
        return MESSAGE_FORMAT.get().replace("%player%", playerName).replace("%entity%", entityName);
    }

    /**
     * 检查是否应该触发一击必杀
     */
    public static boolean shouldTriggerInstakill() {
        if (!ENABLED.get()) {
            return false;
        }

        // 检查概率
        int chance = CHANCE.get();
        if (chance <= 0) {
            return false;
        }

        if (chance >= 100) {
            return true;
        }

        // 根据概率计算
        return new Random().nextInt(100) < chance;
    }

    /**
     * 检查实体是否在黑名单中
     */
    public static boolean isEntityBlacklisted(String entityId) {
        if (entityId == null) {
            return false;
        }
        return getEntityBlacklist().contains(entityId);
    }

    /**
     * 获取配置摘要信息（用于调试）
     */
    public static String getConfigSummary() {
        return String.format(
                "一击必杀配置: 启用=%s, 概率=%d%%, 空手=%s, 影响玩家=%s, 影响Boss=%s, 广播=%s, 动作栏=%s",
                ENABLED.get(),
                CHANCE.get(),
                REQUIRE_EMPTY_HAND.get(),
                AFFECT_PLAYERS.get(),
                AFFECT_BOSSES.get(),
                BROADCAST_MESSAGE.get(),
                SHOW_IN_ACTIONBAR.get()
        );
    }

    // ==================== 配置设置方法 ====================

    public static void setEnabled(boolean value) {
        ENABLED.set(value);
    }

    public static void setDebugMode(boolean value) {
        DEBUG_MODE.set(value);
    }

    public static void setChance(int value) {
        CHANCE.set(value);
    }

    public static void setRequireEmptyHand(boolean value) {
        REQUIRE_EMPTY_HAND.set(value);
    }

    public static void setAffectPlayers(boolean value) {
        AFFECT_PLAYERS.set(value);
    }

    public static void setAffectBosses(boolean value) {
        AFFECT_BOSSES.set(value);
    }

    public static void setBlacklistedEntities(List<String> entities) {
        BLACKLISTED_ENTITIES.set(entities);
    }

    public static void setMessageFormat(String format) {
        MESSAGE_FORMAT.set(format);
    }

    public static void setBroadcastMessage(boolean value) {
        BROADCAST_MESSAGE.set(value);
    }

    public static void setShowInActionbar(boolean value) {
        SHOW_IN_ACTIONBAR.set(value);
    }

    // ==================== 配置初始化和事件处理 ====================

    // 验证方法
    private static boolean validateString(final Object obj) {
        return obj instanceof String;
    }

    // 初始化配置
    public static void register() {
        NeoForgeConfigRegistry.INSTANCE.register(OneShotMod.MOD_ID, ModConfig.Type.COMMON, SPEC, "one_shot_common.toml");

        // 注册配置事件监听器
        NeoForgeModConfigEvents.loading(OneShotMod.MOD_ID).register(FabricConfigImpl::onConfigLoad);
        NeoForgeModConfigEvents.reloading(OneShotMod.MOD_ID).register(FabricConfigImpl::onConfigReload);

        OneShotMod.LOGGER.info("一击必杀模组配置已注册");
    }

    private static void onConfigReload(ModConfig modConfig) {
        validateConfig();
        OneShotMod.LOGGER.info("一击必杀模组配置已重新加载");
    }

    private static void onConfigLoad(ModConfig modConfig) {
        validateConfig();
        OneShotMod.LOGGER.info("一击必杀模组配置已加载");
    }

    // 配置加载事件处理

    // 配置验证
    private static void validateConfig() {
        // 验证概率值在合理范围内
        if (CHANCE.get() < 0 || CHANCE.get() > 100) {
            OneShotMod.LOGGER.error("一击必杀概率值超出范围 (0-100)，当前值: {}", CHANCE.get());
        }

        // 验证消息格式包含必要的占位符
        String message = MESSAGE_FORMAT.get();
        if (!message.contains("%player%") || !message.contains("%entity%")) {
            OneShotMod.LOGGER.warn("消息格式缺少必要的占位符 %player% 或 %entity%");
        }

        OneShotMod.LOGGER.info("配置验证完成: {}", getConfigSummary());
    }

    // ==================== 工具方法 ====================

    public static ModConfigSpec getConfigSpec() {
        return SPEC;
    }

    /**
     * 保存配置到文件
     */
    public static void saveConfig() {
        // 在 Forge Config API Port 中，配置会自动保存
        // 这里可以添加额外的保存逻辑
        if (SPEC.isLoaded()) {
            OneShotMod.LOGGER.debug("配置已保存");
        }
    }

    /**
     * 重置为默认值
     */
    public static void resetToDefaults() {
        setEnabled(true);
        setDebugMode(false);
        setChance(100);
        setRequireEmptyHand(false);
        setAffectPlayers(false);
        setAffectBosses(false);
        setBlacklistedEntities(Arrays.asList("minecraft:wither", "minecraft:ender_dragon"));
        setMessageFormat("§6[§e%player%§6] §a触发一击必杀，已抹除 §c[§4%entity%§c]");
        setBroadcastMessage(true);
        setShowInActionbar(false);

        OneShotMod.LOGGER.info("一击必杀配置已重置为默认值");
    }

    /**
     * 验证配置的完整性
     */
    public static boolean validateConfiguration() {
        try {
            // 测试获取所有配置值
            ENABLED.get();
            DEBUG_MODE.get();
            CHANCE.get();
            REQUIRE_EMPTY_HAND.get();
            AFFECT_PLAYERS.get();
            AFFECT_BOSSES.get();
            BLACKLISTED_ENTITIES.get();
            MESSAGE_FORMAT.get();
            BROADCAST_MESSAGE.get();
            SHOW_IN_ACTIONBAR.get();

            OneShotMod.LOGGER.info("配置验证成功: {}", getConfigSummary());
            return true;
        } catch (Exception e) {
            OneShotMod.LOGGER.error("配置验证失败", e);
            return false;
        }
    }

    /**
     * 重新加载配置
     */
    public static void reloadConfig() {
        // 在 Forge Config API Port 中，配置会自动重新加载
        // 这里主要是为了提供统一的重新加载接口
        OneShotMod.LOGGER.info("重新加载一击必杀配置");
        validateConfiguration();
    }
}