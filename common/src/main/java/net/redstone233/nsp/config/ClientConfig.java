package net.redstone233.nsp.config;

import net.minecraft.item.Item;
import net.redstone233.nsp.OneShotMod;

import java.lang.reflect.Method;
import java.util.List;

public class ClientConfig {
    // 配置值将通过平台特定实现获取
    private static ConfigProvider configProvider;

    public static void setConfigProvider(ConfigProvider provider) {
        configProvider = provider;
        // 验证配置
        if (configProvider != null) {
            validateConfiguration();
        }
    }

    public static boolean isEnabled() {
        return configProvider != null && configProvider.isEnabled();
    }

    public static int getChance() {
        return configProvider != null ? configProvider.getChance() : 100;
    }

    public static boolean requiresEmptyHand() {
        return configProvider != null && configProvider.requiresEmptyHand();
    }

    public static boolean affectsPlayers() {
        return configProvider != null && configProvider.affectsPlayers();
    }

    public static boolean affectsBosses() {
        return configProvider != null && configProvider.affectsBosses();
    }

    public static java.util.List<? extends String> getBlacklistedEntities() {
        return configProvider != null ? configProvider.getBlacklistedEntities() : java.util.List.of();
    }

    public static boolean shouldBroadcastMessage() {
        return configProvider != null && configProvider.shouldBroadcastMessage();
    }

    public static boolean shouldShowInActionbar() {
        return configProvider != null && configProvider.shouldShowInActionbar();
    }

    public static boolean isDebugMode() {
        return configProvider != null && configProvider.isDebugMode();
    }

    public static String getMessage(String playerName, String entityName) {
        if (configProvider == null) {
            return "[" + playerName + "] 触发一击必杀，已抹除 [" + entityName + "]";
        }
        return configProvider.getMessage(playerName, entityName);
    }

    public static int getMaxItemStackCount() {
        return configProvider != null ? configProvider.getMaxItemStackCount() : Item.MAX_MAX_COUNT;
    }

    public static ConfigProvider getConfigProvider() {
        return configProvider;
    }

    /**
     * 验证配置的完整性
     */
    public static boolean validateConfiguration() {
        if (configProvider == null) {
            OneShotMod.LOGGER.error("配置提供者未设置");
            return false;
        }

        try {
            // 测试获取所有配置值
            isEnabled();
            getChance();
            requiresEmptyHand();
            affectsPlayers();
            affectsBosses();
            getBlacklistedEntities();
            shouldBroadcastMessage();
            shouldShowInActionbar();
            isDebugMode();
            getMessage("TestPlayer", "TestEntity");

            OneShotMod.LOGGER.info("配置验证成功");
            return true;
        } catch (Exception e) {
            OneShotMod.LOGGER.error("配置验证失败", e);
            return false;
        }
    }

    /**
     * 配置重载时的回调
     */
    public static void onConfigReload() {
        OneShotMod.LOGGER.info("ClientConfig 配置已重新加载");

        // 通知堆叠系统配置已更新
        try {
            Class<?> stackSystemManager = Class.forName("net.redstone233.nsp.util.StackSystemManager");
            Method reloadMethod = stackSystemManager.getMethod("onConfigReloaded");
            reloadMethod.invoke(null);
        } catch (Exception e) {
            OneShotMod.LOGGER.warn("无法通知堆叠系统配置重载: {}", e.getMessage());
        }
    }

    /**
     * 获取配置摘要（包含堆叠信息）
     */
    public static String getFullConfigSummary() {

        return "=== 一击必杀配置 ===\n" +
                "启用: " + isEnabled() + "\n" +
                "概率: " + getChance() + "%\n" +
                "空手触发: " + requiresEmptyHand() + "\n" +
                "影响玩家: " + affectsPlayers() + "\n" +
                "影响Boss: " + affectsBosses() + "\n" +
                "广播消息: " + shouldBroadcastMessage() + "\n" +
                "动作栏显示: " + shouldShowInActionbar() + "\n" +
                "调试模式: " + isDebugMode() + "\n" +
                "最大堆叠: " + getMaxItemStackCount() + "\n";
    }

    public interface ConfigProvider {
        boolean isEnabled();
        int getChance();
        boolean requiresEmptyHand();
        boolean affectsPlayers();
        boolean affectsBosses();
        List<? extends String> getBlacklistedEntities();
        boolean shouldBroadcastMessage();
        boolean shouldShowInActionbar();
        boolean isDebugMode();
        String getMessage(String playerName, String entityName);
        int getMaxItemStackCount();
    }
}