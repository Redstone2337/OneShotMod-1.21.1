// NeoForgeConfigSyncHandler.java - 在 neoforge 模块中
package net.redstone233.nsp.neoforge.config;

import net.redstone233.nsp.config.ConfigSyncHandler;
import net.redstone233.nsp.neoforge.OneShotModNeoForge;

public class NeoForgeConfigSyncHandler implements ConfigSyncHandler {

    @Override
    public void setMaxItemStackCount(int value) {
        NeoForgeConfigImpl.setMaxItemStackCount(value);
    }

    @Override
    public int getMaxItemStackCount() {
        return NeoForgeConfigImpl.MAX_ITEM_STACK_COUNT.get();
    }

    @Override
    public void saveConfig() {
        // NeoForge 配置会自动保存，这里记录日志即可
        OneShotModNeoForge.LOGGER.debug("NeoForge 配置已保存");
    }

    @Override
    public void validateConfig() {
        NeoForgeConfigImpl.validateConfig();
    }
}