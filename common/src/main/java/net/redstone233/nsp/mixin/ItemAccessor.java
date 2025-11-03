package net.redstone233.nsp.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import net.minecraft.item.Item;

@Mixin(Item.class)
public interface ItemAccessor {
    // 为静态字段生成Accessor，方法也必须是静态的
    @Accessor("MAX_MAX_COUNT")
    static void setMaxMaxCount(int value) {
        throw new AssertionError();
    }

    @Accessor("MAX_MAX_COUNT")
    static int getMaxMaxCount() {
        throw new AssertionError();
    }
}