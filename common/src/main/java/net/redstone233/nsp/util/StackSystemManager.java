package net.redstone233.nsp.util;

import net.redstone233.nsp.OneShotMod;
import net.redstone233.nsp.config.ClientConfig;

import java.lang.reflect.Method;

public class StackSystemManager {
    private static int currentStackCount = 64;
    private static boolean useConfigSystem = false;
    private static ConfigUpdater configUpdater;

    public static void initialize() {
        // 检查是否使用配置系统
        if (ClientConfig.getConfigProvider() != null) {
            useConfigSystem = true;
            currentStackCount = ClientConfig.getMaxItemStackCount();

            // 初始化配置更新器
            initConfigUpdater();

            OneShotMod.LOGGER.info("使用配置系统，当前堆叠大小: {}", currentStackCount);
        } else {
            // 如果没有配置系统，使用默认值
            currentStackCount = 64;
            OneShotMod.LOGGER.info("未使用配置系统，使用默认堆叠大小: {}", currentStackCount);
        }
    }

    public static int getCurrentStackCount() {
        if (useConfigSystem && ClientConfig.getConfigProvider() != null) {
            // 直接从配置系统获取最新值
            return ClientConfig.getMaxItemStackCount();
        }
        return currentStackCount;
    }

    public static boolean modifyStackSystem(int newCount) {
        if (newCount < 1 || newCount > OneShotMod.CUSTOM_MAX_ITEM_STACK_COUNT) {
            OneShotMod.LOGGER.error("无效的堆叠大小: {}", newCount);
            return false;
        }

        int oldCount = currentStackCount;
        currentStackCount = newCount;

        boolean success = true;

        if (useConfigSystem && configUpdater != null) {
            // 使用配置更新器
            success = configUpdater.updateConfig(newCount);

            if (success) {
                OneShotMod.LOGGER.info("堆叠大小已从 {} 更新为 {} (通过配置系统)", oldCount, newCount);
            }
        } else {
            // 不使用配置系统，直接更新内存值
            OneShotMod.LOGGER.info("堆叠大小已从 {} 更新为 {} (内存更新)", oldCount, newCount);
        }

        if (!success) {
            // 恢复原值
            currentStackCount = oldCount;
            OneShotMod.LOGGER.error("设置堆叠大小失败，恢复为 {}", oldCount);
        }

        return success;
    }

    public static String getSystemStatus() {
        if (useConfigSystem && ClientConfig.getConfigProvider() != null) {
            int configValue = ClientConfig.getMaxItemStackCount();
            String platform = getConfigPlatform();
            return String.format("当前堆叠大小: %d (来源: %s配置系统)", configValue, platform);
        } else {
            return String.format("当前堆叠大小: %d (来源: 内存值)", currentStackCount);
        }
    }

    public static int getCurrentMaxMaxCount() {
        return OneShotMod.CUSTOM_MAX_ITEM_STACK_COUNT;
    }

    // 配置重载时的回调
    public static void onConfigReloaded() {
        if (useConfigSystem && ClientConfig.getConfigProvider() != null) {
            int newCount = ClientConfig.getMaxItemStackCount();
            if (newCount != currentStackCount) {
                OneShotMod.LOGGER.info("配置重载检测到堆叠大小变化: {} -> {}", currentStackCount, newCount);
                currentStackCount = newCount;
            }
        }
    }

    // 强制更新方法
    public static boolean forceUpdate() {
        OneShotMod.LOGGER.info("强制更新堆叠系统，当前值: {}", getCurrentStackCount());
        return true;
    }

    // 检查是否使用配置系统
    public static boolean isUsingConfigSystem() {
        return useConfigSystem;
    }

    // 获取配置平台信息
    private static String getConfigPlatform() {
        if (ClientConfig.getConfigProvider() != null) {
            Object provider = ClientConfig.getConfigProvider();
            String className = provider.getClass().getName();
            if (className.contains("fabric")) {
                return "Fabric ";
            } else if (className.contains("neoforge")) {
                return "NeoForge ";
            }
        }
        return "";
    }

    // 获取配置同步状态
    public static String getConfigSyncStatus() {
        if (!useConfigSystem) {
            return "未使用配置系统";
        }

        int configValue = ClientConfig.getMaxItemStackCount();
        if (configValue == currentStackCount) {
            return "配置已同步";
        } else {
            return String.format("配置未同步 (配置值: %d, 内存值: %d)", configValue, currentStackCount);
        }
    }

    // 初始化配置更新器
    private static void initConfigUpdater() {
        if (ClientConfig.getConfigProvider() == null) {
            return;
        }

        Object provider = ClientConfig.getConfigProvider();
        String className = provider.getClass().getName();

        if (className.contains("fabric")) {
            configUpdater = new FabricConfigUpdater();
        } else if (className.contains("neoforge")) {
            configUpdater = new NeoForgeConfigUpdater();
        } else {
            configUpdater = new ReflectionConfigUpdater();
        }

        OneShotMod.LOGGER.info("初始化配置更新器: {}", configUpdater.getClass().getSimpleName());
    }

    // 配置更新器接口
    private interface ConfigUpdater {
        boolean updateConfig(int newValue);
    }

    // Fabric 配置更新器
    private static class FabricConfigUpdater implements ConfigUpdater {
        @Override
        public boolean updateConfig(int newValue) {
            try {
                // 使用反射调用 Fabric 配置类
                Class<?> fabricConfigClass = Class.forName("net.redstone233.nsp.fabric.config.FabricConfigImpl");
                Method setMethod = fabricConfigClass.getMethod("setMaxItemStackCount", int.class);
                setMethod.invoke(null, newValue);

                // 调用保存方法
                Method saveMethod = fabricConfigClass.getMethod("saveConfig");
                saveMethod.invoke(null);

                return true;
            } catch (Exception e) {
                OneShotMod.LOGGER.error("Fabric 配置更新失败", e);
                return false;
            }
        }
    }

    // NeoForge 配置更新器
    private static class NeoForgeConfigUpdater implements ConfigUpdater {
        @Override
        public boolean updateConfig(int newValue) {
            try {
                // 使用反射调用 NeoForge 配置类
                Class<?> neoForgeConfigClass = Class.forName("net.redstone233.nsp.neoforge.config.NeoForgeConfigImpl");
                Method setMethod = neoForgeConfigClass.getMethod("setMaxItemStackCount", int.class);
                setMethod.invoke(null, newValue);

                // NeoForge 配置会自动保存，不需要手动调用保存方法
                return true;
            } catch (Exception e) {
                OneShotMod.LOGGER.error("NeoForge 配置更新失败", e);
                return false;
            }
        }
    }

    // 反射配置更新器（备用）
    private static class ReflectionConfigUpdater implements ConfigUpdater {
        @Override
        public boolean updateConfig(int newValue) {
            try {
                Object provider = ClientConfig.getConfigProvider();
                Method setMethod = provider.getClass().getMethod("setMaxItemStackCount", int.class);
                setMethod.invoke(provider, newValue);
                return true;
            } catch (Exception e) {
                OneShotMod.LOGGER.error("反射配置更新失败", e);
                return false;
            }
        }
    }
}