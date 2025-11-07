package net.redstone233.nsp.mixin;

import net.minecraft.block.dispenser.ItemDispenserBehavior;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.math.BlockPointer;
import net.redstone233.nsp.util.IDispenserBlockEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "net/minecraft/block/dispenser/DispenserBehavior$15")
public class MixinDispenserBehavior20 {

    @Shadow
    @Final
    private ItemDispenserBehavior fallbackBehavior;

    @Inject(
            method = "dispenseSilently",
            at = @At("RETURN"),
            cancellable = true
    )
    private void decreaseOne(BlockPointer pointer, ItemStack stack, CallbackInfoReturnable<ItemStack> cir) {
        // dispense empty bottle if dispenser is full
//        if (((DispenserBlockEntity) pointer.getBlockEntity()).addToFirstFreeSlot(new ItemStack(Items.GLASS_BOTTLE)) < 0) {
        if (!((IDispenserBlockEntity)pointer.blockEntity()).tryInsertAndStackItem(new ItemStack(Items.GLASS_BOTTLE))) {
            this.fallbackBehavior.dispense(pointer, new ItemStack(Items.GLASS_BOTTLE));
        }
//        }
        stack.decrement(1);
        cir.setReturnValue(stack);
    }
}
