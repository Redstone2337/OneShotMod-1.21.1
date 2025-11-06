package net.redstone233.nsp.mixin;

import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;
import net.redstone233.nsp.config.ClientConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Slot.class)
public class SlotMixin {

    @Inject(method = "getMaxItemCount*", at = @At("HEAD"), cancellable = true)
    private void onGetMaxItemCount(CallbackInfoReturnable<Integer> cir) {
        Slot self = (Slot)(Object)this;
        ItemStack stack = self.getStack();

        if (!stack.isEmpty()) {
            int configMax = ClientConfig.getMaxItemStackCount();
            int vanillaMax = stack.getMaxCount();

            if (configMax > vanillaMax) {
                cir.setReturnValue(configMax);
            }
        }
    }

    @Inject(method = "getMaxItemCount(Lnet/minecraft/item/ItemStack;)I", at = @At("HEAD"), cancellable = true)
    private void onGetMaxItemCountWithStack(ItemStack stack, CallbackInfoReturnable<Integer> cir) {
        if (!stack.isEmpty()) {
            int configMax = ClientConfig.getMaxItemStackCount();
            int vanillaMax = stack.getMaxCount();

            if (configMax > vanillaMax) {
                cir.setReturnValue(configMax);
            }
        }
    }
}