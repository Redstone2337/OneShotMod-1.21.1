package net.redstone233.nsp.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import net.minecraft.item.Item;

@Mixin(Item.class)
public interface ItemAccessor {

    // 获取 MAX_MAX_COUNT 字段的值
    @Accessor("MAX_MAX_COUNT")
    static int getMaxMaxCount() {
        throw new AssertionError("Mixin accessor not implemented!");
    }

    // 设置 MAX_MAX_COUNT 字段的值
    @Accessor("MAX_MAX_COUNT")
    static void setMaxMaxCount(int value) {
        throw new AssertionError("Mixin accessor not implemented!");
    }
}