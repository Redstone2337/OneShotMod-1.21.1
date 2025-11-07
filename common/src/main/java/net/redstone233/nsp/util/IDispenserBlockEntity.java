package net.redstone233.nsp.util;

import net.minecraft.item.ItemStack;

public interface IDispenserBlockEntity {
    boolean tryInsertAndStackItem(ItemStack itemStack);
}
