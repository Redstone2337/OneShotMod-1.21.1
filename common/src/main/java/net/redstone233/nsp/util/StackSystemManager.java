// StackSystemManager.java
package net.redstone233.nsp.util;

import net.redstone233.nsp.OneShotMod;
import net.redstone233.nsp.mixin.ItemAccessor;

public class StackSystemManager {

    private static final int VANILLA_MAX_MAX_COUNT = 99;
    private static boolean isSystemModified = false;
    private static int currentMaxStackCount = 64;

    /**
     * 初始化堆叠系统
     */
    public static void initialize() {
        try {
            // 获取当前配置值
            currentMaxStackCount = getCurrentMaxStackCountFromConfig();

            // 如果配置值超过原版限制，修改系统
            if (currentMaxStackCount > VANILLA_MAX_MAX_COUNT) {
                modifyStackSystem(currentMaxStackCount);
            }

            OneShotMod.LOGGER.info("堆叠系统初始化完成 - 当前值: {}, 系统已修改: {}",
                    currentMaxStackCount, isSystemModified);

        } catch (Exception e) {
            OneShotMod.LOGGER.error("堆叠系统初始化失败: {}", e.getMessage());
        }
    }

    /**
     * 修改堆叠系统
     */
    public static boolean modifyStackSystem(int newValue) {
        try {
            OneShotMod.LOGGER.info("开始修改堆叠系统，目标值: {}", newValue);

            // 使用 ItemAccessor 修改 MAX_MAX_COUNT
            ItemAccessor.setMaxMaxCount(newValue);

            // 更新当前值
            currentMaxStackCount = newValue;
            isSystemModified = true;

            // 验证修改
            int actualValue = ItemAccessor.getMaxMaxCount();
            boolean success = (actualValue == newValue);

            OneShotMod.LOGGER.info("堆叠系统修改 {} - 目标: {}, 实际: {}",
                    success ? "成功" : "失败", newValue, actualValue);

            return success;

        } catch (Exception e) {
            OneShotMod.LOGGER.error("修改堆叠系统失败: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 获取当前堆叠数量配置
     */
    private static int getCurrentMaxStackCountFromConfig() {
        try {
            // 这里使用反射从配置提供者获取值
            Object configProvider = net.redstone233.nsp.config.ClientConfig.getConfigProvider();
            if (configProvider != null) {
                java.lang.reflect.Method getMethod = configProvider.getClass()
                        .getMethod("getMaxItemStackCount");
                return (int) getMethod.invoke(configProvider);
            }
        } catch (Exception e) {
            OneShotMod.LOGGER.warn("无法从配置获取堆叠数量，使用默认值: {}", e.getMessage());
        }
        return 64; // 默认值
    }

    /**
     * 保存配置到文件
     */
    public static boolean saveConfigToFile(int value) {
        try {
            Object configProvider = net.redstone233.nsp.config.ClientConfig.getConfigProvider();
            if (configProvider != null) {
                // 调用 setMaxItemStackCount 方法
                java.lang.reflect.Method setMethod = configProvider.getClass()
                        .getMethod("setMaxItemStackCount", int.class);
                setMethod.invoke(configProvider, value);

                // 调用保存方法
                try {
                    java.lang.reflect.Method saveMethod = configProvider.getClass()
                            .getMethod("saveConfig");
                    saveMethod.invoke(configProvider);
                    return true;
                } catch (NoSuchMethodException e) {
                    OneShotMod.LOGGER.debug("配置提供者不支持手动保存");
                    return true; // 某些平台自动保存
                }
            }
        } catch (Exception e) {
            OneShotMod.LOGGER.error("保存配置失败: {}", e.getMessage());
        }
        return false;
    }

    /**
     * 获取当前 MAX_MAX_COUNT 的值
     */
    public static int getCurrentMaxMaxCount() {
        try {
            return ItemAccessor.getMaxMaxCount();
        } catch (Exception e) {
            OneShotMod.LOGGER.error("获取 MAX_MAX_COUNT 失败: {}", e.getMessage());
            return VANILLA_MAX_MAX_COUNT;
        }
    }

    /**
     * 获取当前配置的堆叠数量
     */
    public static int getCurrentStackCount() {
        return currentMaxStackCount;
    }

    /**
     * 重置为原版值
     */
    public static boolean resetToVanilla() {
        return modifyStackSystem(VANILLA_MAX_MAX_COUNT);
    }

    /**
     * 检查系统是否已修改
     */
    public static boolean isSystemModified() {
        return isSystemModified;
    }

    /**
     * 获取系统状态信息
     */
    public static String getSystemStatus() {
        return String.format("堆叠系统 - 配置值: %d, MAX_MAX_COUNT: %d, 已修改: %s",
                currentMaxStackCount, getCurrentMaxMaxCount(), isSystemModified ? "是" : "否");
    }
}