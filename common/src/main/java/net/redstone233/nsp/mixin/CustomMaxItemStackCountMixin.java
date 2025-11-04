package net.redstone233.nsp.mixin;

import net.minecraft.item.ItemStack;
import net.redstone233.nsp.util.StackSystemManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemStack.class)
public class CustomMaxItemStackCountMixin {

    @Inject(method = "getMaxCount", at = @At("HEAD"), cancellable = true)
    private void getItemStackMaxCount(CallbackInfoReturnable<Integer> cir) {
        int customMaxCount = StackSystemManager.getCurrentStackCount();
        if (customMaxCount != 64) {
            cir.setReturnValue(customMaxCount);
        }
    }
}