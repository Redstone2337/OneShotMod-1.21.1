package net.redstone233.nsp.mixin;

import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Inventory.class)
public interface MixinInventory {

    @Inject(method = "isValid", at = @At("HEAD"), cancellable = true)
    private void allowLargeStacks(int slot, ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        // 允许大堆叠物品放入槽位
        if (stack.getCount() > 64) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "getMaxCountPerStack", at = @At("HEAD"), cancellable = true)
    private void increaseMaxStackSize(CallbackInfoReturnable<Integer> cir) {
        // 提高每个槽位的最大堆叠数量
        cir.setReturnValue(999); // 设置一个足够大的值
    }
}