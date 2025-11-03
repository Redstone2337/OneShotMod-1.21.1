package net.redstone233.nsp.mixin;

import net.minecraft.item.Item;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Item.class)
public interface ItemMaxCountAccessor {
    @Accessor("MAX_MAX_COUNT")
    static void setMaxCount(int count) {
        throw new UnsupportedOperationException("Cannot set max count to " + count);
    }
    @Accessor("MAX_MAX_COUNT")
    static int getMaxCount() {
        try {
            return Item.MAX_MAX_COUNT;
        } catch (Exception e) {
            throw new UnsupportedOperationException("Cannot get max count" + e);
        }
    }
}
