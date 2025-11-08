package net.redstone233.nsp.mixin;

import net.minecraft.item.ItemStack;
import net.redstone233.nsp.util.IDispenserBlockEntity;
import net.minecraft.util.math.BlockPointer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "net/minecraft/block/dispenser/DispenserBehavior$9")
public class MixinDispenserBehavior9 {

    @Inject(method = "dispenseSilently", at = @At("HEAD"), cancellable = true)
    public void tryStack(BlockPointer pointer, ItemStack stack, CallbackInfoReturnable<ItemStack> cir) {
        // Add the item to the first free slot and get the remaining stack
        ItemStack remainingStack = pointer.blockEntity().addToFirstFreeSlot(stack);

        // Check if the item is modified and if there's any remaining item stack after the operation
        if (stack.getMaxCount() > 1 && !remainingStack.isEmpty()) {
            // Attempt custom logic to stack the remaining item
            boolean success = ((IDispenserBlockEntity) pointer.blockEntity()).oneShotMod_1_21_1$tryInsertAndStackItem(remainingStack);

            // If custom stacking succeeds, cancel further execution and return an empty stack
            if (success) {
                cir.setReturnValue(ItemStack.EMPTY);
                cir.cancel();
            }
        }
    }
}