package net.redstone233.nsp.util;

import net.redstone233.nsp.OneShotMod;
import net.redstone233.nsp.mixin.ItemAccessor;
import net.redstone233.nsp.config.ConfigSyncHandler;

public class StackSystemManager {

    private static final int VANILLA_MAX_MAX_COUNT = 99;
    private static boolean isSystemModified = false;
    private static int currentMaxStackCount = 64;
    private static ConfigSyncHandler configSyncHandler = null;

    /**
     * 设置配置同步处理器
     */
    public static void setConfigSyncHandler(ConfigSyncHandler handler) {
        configSyncHandler = handler;
        OneShotMod.LOGGER.info("配置同步处理器已设置");
    }

    /**
     * 初始化堆叠系统
     */
    public static void initialize() {
        try {
            // 从配置系统获取当前配置值
            if (configSyncHandler != null) {
                currentMaxStackCount = configSyncHandler.getMaxItemStackCount();
            } else {
                // 备用方法：通过 ClientConfig 获取
                currentMaxStackCount = getStackCountFromClientConfig();
            }

            OneShotMod.LOGGER.info("初始化堆叠系统，配置值: {}", currentMaxStackCount);

            // 如果配置值超过原版限制，修改系统
            int currentMaxMaxCount = getCurrentMaxMaxCount();
            if (currentMaxStackCount > VANILLA_MAX_MAX_COUNT || currentMaxStackCount != currentMaxMaxCount) {
                boolean modifySuccess = modifyStackSystem(currentMaxStackCount);
                if (modifySuccess) {
                    OneShotMod.LOGGER.info("堆叠系统初始化同步成功 - 配置值: {}, MAX_MAX_COUNT: {}",
                            currentMaxStackCount, getCurrentMaxMaxCount());
                } else {
                    OneShotMod.LOGGER.warn("堆叠系统初始化同步失败，使用当前 MAX_MAX_COUNT: {}", currentMaxMaxCount);
                }
            }
            isSystemModified = (currentMaxStackCount > VANILLA_MAX_MAX_COUNT);

            OneShotMod.LOGGER.info("堆叠系统初始化完成 - 当前值: {}, 系统已修改: {}",
                    currentMaxStackCount, isSystemModified);

        } catch (Exception e) {
            OneShotMod.LOGGER.error("堆叠系统初始化失败: {}", e.getMessage());
            // 初始化失败时，尝试使用默认值
            resetToVanillaWithConfig();
        }
    }

    /**
     * 修改堆叠系统并确保配置持久化
     */
    public static boolean modifyStackSystem(int newValue) {
        try {
            OneShotMod.LOGGER.info("开始修改堆叠系统，目标值: {}", newValue);

            // 1. 使用 ItemAccessor 修改 MAX_MAX_COUNT 字段
            ItemAccessor.setMaxMaxCount(newValue);

            // 2. 更新配置系统中的值
            updateConfigValue(newValue);

            // 3. 更新当前内存中的值
            currentMaxStackCount = newValue;

            // 4. 保存配置到文件
            boolean saveSuccess = saveConfigToFile();

            // 5. 验证修改
            int actualMaxCount = getCurrentMaxMaxCount();
            boolean modifySuccess = (actualMaxCount == newValue);

            isSystemModified = modifySuccess && (newValue > VANILLA_MAX_MAX_COUNT);

            OneShotMod.LOGGER.info("堆叠系统修改结果 - 请求: {}, 实际MAX_MAX_COUNT: {}, 配置文件保存: {}, 最终状态: {}",
                    newValue, actualMaxCount, saveSuccess ? "成功" : "失败", isSystemModified ? "已修改" : "未修改");

            return modifySuccess && saveSuccess;

        } catch (Exception e) {
            OneShotMod.LOGGER.error("修改堆叠系统失败: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 更新配置值
     */
    private static void updateConfigValue(int newValue) {
        if (configSyncHandler != null) {
            // 使用配置同步处理器
            configSyncHandler.setMaxItemStackCount(newValue);
        } else {
            // 备用方法：通过反射调用 ClientConfig
            setStackCountViaReflection(newValue);
        }
    }

    /**
     * 通过反射设置堆叠数量
     */
    private static void setStackCountViaReflection(int value) {
        try {
            // 使用反射调用 ClientConfig.setMaxItemStackCount
            Class<?> clientConfig = Class.forName("net.redstone233.nsp.config.ClientConfig");
            java.lang.reflect.Method setMethod = clientConfig.getMethod("setMaxItemStackCount", int.class);
            setMethod.invoke(null, value);
        } catch (Exception e) {
            OneShotMod.LOGGER.warn("通过反射设置堆叠数量失败: {}", e.getMessage());
        }
    }

    /**
     * 通过 ClientConfig 获取堆叠数量
     */
    private static int getStackCountFromClientConfig() {
        try {
            Class<?> clientConfig = Class.forName("net.redstone233.nsp.config.ClientConfig");
            java.lang.reflect.Method getMethod = clientConfig.getMethod("getMaxItemStackCount");
            return (int) getMethod.invoke(null);
        } catch (Exception e) {
            OneShotMod.LOGGER.warn("通过反射获取堆叠数量失败: {}", e.getMessage());
            return 64; // 默认值
        }
    }

    /**
     * 保存配置到文件
     */
    public static boolean saveConfigToFile() {
        try {
            if (configSyncHandler != null) {
                // 使用配置同步处理器
                configSyncHandler.saveConfig();
                OneShotMod.LOGGER.debug("通过配置同步处理器保存配置");
                return true;
            } else {
                // 备用方法：通过反射保存配置
                return saveConfigViaReflection();
            }
        } catch (Exception e) {
            OneShotMod.LOGGER.error("保存配置文件失败: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 通过反射保存配置
     */
    private static boolean saveConfigViaReflection() {
        try {
            // 尝试调用配置提供者的保存方法
            Class<?> clientConfig = Class.forName("net.redstone233.nsp.config.ClientConfig");
            java.lang.reflect.Method getProviderMethod = clientConfig.getMethod("getConfigProvider");
            Object configProvider = getProviderMethod.invoke(null);

            if (configProvider != null) {
                java.lang.reflect.Method saveMethod = configProvider.getClass().getMethod("saveConfig");
                saveMethod.invoke(configProvider);
                OneShotMod.LOGGER.debug("通过反射保存配置成功");
                return true;
            }
        } catch (Exception e) {
            OneShotMod.LOGGER.debug("通过反射保存配置失败: {}", e.getMessage());
        }
        return false;
    }

    /**
     * 专门处理从配置屏幕的修改
     */
    public static void onConfigScreenUpdate(int newValue) {
        OneShotMod.LOGGER.info("配置屏幕更新通知: {}", newValue);

        // 直接调用 modifyStackSystem 确保同步修改和保存
        boolean success = modifyStackSystem(newValue);

        if (success) {
            OneShotMod.LOGGER.info("配置屏幕更新处理成功");
        } else {
            OneShotMod.LOGGER.error("配置屏幕更新处理失败！");
        }
    }

    /**
     * 重置为原版值（包括配置文件）
     */
    public static boolean resetToVanilla() {
        boolean success = modifyStackSystem(VANILLA_MAX_MAX_COUNT);
        if (success) {
            OneShotMod.LOGGER.info("已重置堆叠系统为原版值");
        }
        return success;
    }

    /**
     * 带配置重置的重置方法
     */
    private static void resetToVanillaWithConfig() {
        try {
            ItemAccessor.setMaxMaxCount(VANILLA_MAX_MAX_COUNT);
            updateConfigValue(VANILLA_MAX_MAX_COUNT);
            saveConfigToFile();
            currentMaxStackCount = VANILLA_MAX_MAX_COUNT;
            isSystemModified = false;
            OneShotMod.LOGGER.warn("已强制重置堆叠系统为原版默认值");
        } catch (Exception e) {
            OneShotMod.LOGGER.error("强制重置堆叠系统失败: {}", e.getMessage());
        }
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
     * 检查系统是否已修改
     */
    public static boolean isSystemModified() {
        return isSystemModified;
    }

    /**
     * 获取系统状态信息
     */
    public static String getSystemStatus() {
        int memoryValue = getCurrentStackCount();
        int maxMaxCount = getCurrentMaxMaxCount();
        int configValue = getStackCountFromClientConfig();

        return String.format("堆叠系统状态 - 内存值: %d, 配置值: %d, MAX_MAX_COUNT: %d, 同步: %s",
                memoryValue, configValue, maxMaxCount,
                (memoryValue == maxMaxCount && memoryValue == configValue) ? "已同步" : "不同步");
    }

    /**
     * 验证系统同步状态
     */
    public static boolean validateSystemSync() {
        int memoryValue = getCurrentStackCount();
        int maxMaxCount = getCurrentMaxMaxCount();
        int configValue = getStackCountFromClientConfig();

        boolean isSynced = (memoryValue == maxMaxCount && memoryValue == configValue);

        if (!isSynced) {
            OneShotMod.LOGGER.warn("系统同步验证失败 - 内存值: {}, 配置值: {}, MAX_MAX_COUNT: {}",
                    memoryValue, configValue, maxMaxCount);
        }

        return isSynced;
    }

    /**
     * 强制同步系统状态
     */
    public static boolean forceSyncSystem() {
        try {
            int configValue = getStackCountFromClientConfig();
            OneShotMod.LOGGER.info("强制同步系统状态，使用配置值: {}", configValue);
            return modifyStackSystem(configValue);
        } catch (Exception e) {
            OneShotMod.LOGGER.error("强制同步系统状态失败: {}", e.getMessage());
            return false;
        }
    }
}