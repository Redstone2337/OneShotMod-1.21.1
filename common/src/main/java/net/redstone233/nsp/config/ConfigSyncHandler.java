// ConfigSyncHandler.java - 在 common 模块中
package net.redstone233.nsp.config;

public interface ConfigSyncHandler {
    /**
     * 设置最大堆叠数量
     */
    void setMaxItemStackCount(int value);

    /**
     * 获取最大堆叠数量
     */
    int getMaxItemStackCount();

    /**
     * 保存配置到文件
     */
    void saveConfig();

    /**
     * 验证配置
     */
    void validateConfig();
}