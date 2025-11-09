package net.redstone233.nsp.mixin;

import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerInventory.class)
public abstract class MixinPlayerInventory {

    @Shadow
    public abstract ItemStack getStack(int slot);

    @Shadow
    public abstract void setStack(int slot, ItemStack stack);

    @Inject(method = "canStackAddMore", at = @At("HEAD"), cancellable = true)
    private static void allowMoreStacking(ItemStack existingStack, ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        // 允许堆叠更多物品，即使超过原版限制
        if (!existingStack.isEmpty() && ItemStack.areItemsAndComponentsEqual(existingStack, stack)) {
            int totalCount = existingStack.getCount() + stack.getCount();
            cir.setReturnValue(totalCount <= existingStack.getMaxCount());
        }
    }

    @Inject(method = "addStack(ILnet/minecraft/item/ItemStack;)I", at = @At("HEAD"), cancellable = true)
    private void addLargeStack(int slot, ItemStack stack, CallbackInfoReturnable<Integer> cir) {
        // 处理大堆叠物品的添加
        if (stack.getCount() > 64) {
            ItemStack itemStack = this.getStack(slot);
            if (itemStack.isEmpty()) {
                this.setStack(slot, stack.copy());
                stack.setCount(0);
                cir.setReturnValue(0);
            } else if (ItemStack.areItemsAndComponentsEqual(itemStack, stack)) {
                int totalCount = itemStack.getCount() + stack.getCount();
                int maxCount = itemStack.getMaxCount();

                if (totalCount <= maxCount) {
                    itemStack.setCount(totalCount);
                    stack.setCount(0);
                    cir.setReturnValue(0);
                } else {
                    int remaining = totalCount - maxCount;
                    itemStack.setCount(maxCount);
                    stack.setCount(remaining);
                    cir.setReturnValue(remaining);
                }
            }
        }
    }
}