// FabricConfigSyncHandler.java - 在 fabric 模块中
package net.redstone233.nsp.fabric.config;

import net.redstone233.nsp.config.ConfigSyncHandler;

public class FabricConfigSyncHandler implements ConfigSyncHandler {

    @Override
    public void setMaxItemStackCount(int value) {
        FabricConfigImpl.setMaxItemStackCount(value);
    }

    @Override
    public int getMaxItemStackCount() {
        return FabricConfigImpl.MAX_ITEM_STACK_COUNT.get();
    }

    @Override
    public void saveConfig() {
        FabricConfigImpl.saveConfig();
    }

    @Override
    public void validateConfig() {
        FabricConfigImpl.validateConfig();
    }
}