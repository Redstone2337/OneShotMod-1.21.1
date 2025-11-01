package net.redstone233.nsp.fabric.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.redstone233.nsp.fabric.keys.ModKeys;
import net.redstone233.nsp.fabric.screen.FabricConfigScreen;

public final class OneShotModFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        // This entrypoint is suitable for setting up client-specific logic, such as rendering.
    }


    private static void registerKeys() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (ModKeys.isOpenConfigKey()) {
                if (client.player != null) {
                    // 打开配置屏幕[citation:5]
                    client.setScreen(FabricConfigScreen.createConfigScreen(client.currentScreen));
                }
            }
        });
    }
}
