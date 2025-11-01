package net.redstone233.nsp.neoforge;


import net.minecraft.client.MinecraftClient;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.redstone233.nsp.OneShotMod;

// This class will not load on dedicated servers. Accessing client side code from here is safe.
@Mod(value = OneShotMod.MOD_ID, dist = Dist.CLIENT)
// You can use EventBusSubscriber to automatically register all static methods in the class annotated with @SubscribeEvent
@EventBusSubscriber(modid = OneShotMod.MOD_ID, value = Dist.CLIENT)
public class OneShotModNeoForgeClient {

    public OneShotModNeoForgeClient(ModContainer container) {
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
    }


    @SubscribeEvent
    static void onClientSetup(FMLClientSetupEvent event) {
        // Some client setup code
        OneShotModNeoForge.LOGGER.info("HELLO FROM CLIENT SETUP");
        OneShotModNeoForge.LOGGER.info("MINECRAFT NAME >> {}", MinecraftClient.getInstance().getName());
    }
}
