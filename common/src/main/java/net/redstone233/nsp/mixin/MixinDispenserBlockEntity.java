package net.redstone233.nsp.mixin;

import net.minecraft.block.entity.DispenserBlockEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.collection.DefaultedList;
import net.redstone233.nsp.util.IDispenserBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(DispenserBlockEntity.class)
public class MixinDispenserBlockEntity implements IDispenserBlockEntity {
    @Shadow
    private DefaultedList<ItemStack> inventory;

    @Override
    public boolean oneShotMod_1_21_1$tryInsertAndStackItem(ItemStack itemStack) {
        boolean inserted = false;
        for (ItemStack invStack : this.inventory) {
            if (invStack.getItem() == itemStack.getItem() && invStack.getCount() < invStack.getMaxCount()) {
                invStack.increment(1);
                inserted = true;
                break;
            }
        }
        return inserted;
    }
}