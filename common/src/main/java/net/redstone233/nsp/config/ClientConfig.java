package net.redstone233.nsp.config;

import net.redstone233.nsp.OneShotMod;

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

    public interface ConfigProvider {
        boolean isEnabled();
        int getChance();
        boolean requiresEmptyHand();
        boolean affectsPlayers();
        boolean affectsBosses();
        java.util.List<? extends String> getBlacklistedEntities();
        boolean shouldBroadcastMessage();
        boolean shouldShowInActionbar();
        boolean isDebugMode();
        String getMessage(String playerName, String entityName);
    }
}