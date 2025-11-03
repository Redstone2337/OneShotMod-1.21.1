package net.redstone233.nsp.mixin;

import net.minecraft.item.Item;
import net.redstone233.nsp.config.ClientConfig;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

@Mixin(Item.class)
public class ItemStaticMixin {

    @Final
    @Shadow(remap = false)
    public static int MAX_MAX_COUNT;

    @Unique
    private static int oneShotMod_1_21_1$getMaxMaxCount() {
        return ClientConfig.getMaxItemStackCount();
    }

    // 静态初始化块 - 在类加载时执行
    static {
        try {
            // 直接修改静态字段
            MAX_MAX_COUNT = ClientConfig.getMaxItemStackCount();
            System.out.println("Static mixin initialized MAX_MAX_COUNT to: " + MAX_MAX_COUNT);
        } catch (Exception e) {
            System.err.println("Failed to initialize MAX_MAX_COUNT in static mixin: " + e.getMessage());
        }
    }
}