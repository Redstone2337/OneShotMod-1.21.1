package net.redstone233.nsp.mixin;

import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.redstone233.nsp.config.ClientConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Inventory.class)
public interface InventoryMixin {

    @Inject(method = "isValid", at = @At("HEAD"), cancellable = true)
    private void onIsValid(int slot, ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        if (!stack.isEmpty()) {
            int actualCount = stack.getCount();
            int configMax = ClientConfig.getMaxItemStackCount();
            int vanillaMax = stack.getMaxCount();

            // 允许超过原版限制但不超过配置限制的物品
            if (actualCount > vanillaMax && actualCount <= configMax) {
                cir.setReturnValue(true);
            }
        }
    }
}