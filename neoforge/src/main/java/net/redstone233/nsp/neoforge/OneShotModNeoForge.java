package net.redstone233.nsp.neoforge;

import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.redstone233.nsp.OneShotMod;
import net.neoforged.fml.common.Mod;
import net.redstone233.nsp.command.StackCommand;
import net.redstone233.nsp.config.ClientConfig;
import net.redstone233.nsp.neoforge.config.NeoForgeConfigImpl;
import org.slf4j.Logger;

@Mod(OneShotMod.MOD_ID)
public final class OneShotModNeoForge {

    public static final Logger LOGGER = LogUtils.getLogger();


    public OneShotModNeoForge(IEventBus modEventBus, ModContainer modContainer) {
        modEventBus.addListener(this::commonSetup);

        NeoForge.EVENT_BUS.register(this);

        modEventBus.addListener(this::addCreative);

        NeoForgeConfigImpl.init(modEventBus, modContainer);

        ClientConfig.setConfigProvider(new NeoForgeConfigImpl());

        // Register our mod's ModConfigSpec so that FML can create and load the config file for us
        modContainer.registerConfig(ModConfig.Type.COMMON, NeoForgeConfigImpl.SPEC);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        // Some common setup code
        OneShotMod.init();
        StackCommand.register();
        LOGGER.info("HELLO FROM COMMON SETUP");
    }

    // Add the example block item to the building blocks tab
    private void addCreative(BuildCreativeModeTabContentsEvent event) {

    }

    // You can use SubscribeEvent and let the Event Bus discover methods to call
    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        // Do something when the server starts
        LOGGER.info("HELLO from server starting");
    }
}
