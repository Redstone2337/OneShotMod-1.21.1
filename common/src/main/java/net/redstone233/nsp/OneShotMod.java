package net.redstone233.nsp;

import net.redstone233.nsp.event.AttackEventHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class OneShotMod {
    public static final String MOD_ID = "nsp";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static void init() {
        // Write common init code here.
        AttackEventHandler.register();
        LOGGER.info("Mod {} initialized", MOD_ID);
    }
}
