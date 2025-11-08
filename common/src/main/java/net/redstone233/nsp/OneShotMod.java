package net.redstone233.nsp;

import net.redstone233.nsp.command.StackCommand;
import net.redstone233.nsp.event.AttackEventHandler;
import net.redstone233.nsp.util.ItemsHelper;
import net.redstone233.nsp.util.StackSystemManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class OneShotMod {
    public static final String MOD_ID = "nsp";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    public static final int CUSTOM_MAX_ITEM_STACK_COUNT = 100000;

    public static void init() {
        // Write common init code here.
        AttackEventHandler.register();
        LOGGER.info("Mod {} initialized", MOD_ID);
        StackCommand.register();
        StackSystemManager.initialize();

        if (StackSystemManager.isUsingConfigSystem()) {
            LOGGER.info("堆叠系统已集成到主配置系统中");
        } else {
            LOGGER.info("堆叠系统使用独立配置文件");
        }
    }
}
