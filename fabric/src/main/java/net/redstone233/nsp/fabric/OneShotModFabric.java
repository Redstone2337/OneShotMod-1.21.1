package net.redstone233.nsp.fabric;

import net.redstone233.nsp.OneShotMod;
import net.fabricmc.api.ModInitializer;
import net.redstone233.nsp.command.StackCommand;
import net.redstone233.nsp.config.ClientConfig;
import net.redstone233.nsp.fabric.config.FabricConfigImpl;
import net.redstone233.nsp.fabric.keys.ModKeys;

public final class OneShotModFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        // This code runs as soon as Minecraft is in a mod-load-ready state.
        // However, some things (like resources) may still be uninitialized.
        // Proceed with mild caution.

        FabricConfigImpl.register();
        ClientConfig.setConfigProvider(new FabricConfigImpl());
        // Run our common setup.
        OneShotMod.init();
        ModKeys.register();
        StackCommand.register();
    }
}
