package net.redstone233.nsp;

import net.minecraft.item.Item;
import net.redstone233.nsp.config.ClientConfig;
import net.redstone233.nsp.event.AttackEventHandler;
import net.redstone233.nsp.mixin.ItemMaxCountAccessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class OneShotMod {
    public static final String MOD_ID = "nsp";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    public static final int CUSTOM_MAX_ITEM_STACK_COUNT = 32767;

    public static void init() {
        // Write common init code here.
        AttackEventHandler.register();
        LOGGER.info("Mod {} initialized", MOD_ID);
        applyGlobalStackSizeOverride();
    }

    private static void applyGlobalStackSizeOverride() {
        try {
            // 记录原始值（可选，用于调试或回滚）
            int originalMaxCount = ItemMaxCountAccessor.getMaxCount();
            LOGGER.info("Original MAX_COUNT: {}", originalMaxCount);

            // 通过ClientConfig获取新的最大堆叠数量
            int newMaxCount = ClientConfig.getMaxItemStackCount();

            // 使用Accessor设置新的最大值
            ItemMaxCountAccessor.setMaxCount(newMaxCount);

            // 验证并记录修改结果
            LOGGER.info("Successfully modified MAX_COUNT to: {}", ItemMaxCountAccessor.getMaxCount());
        } catch (Exception e) {
            LOGGER.error("Failed to modify MAX_COUNT via Mixin Accessor", e);
        }
    }
}
