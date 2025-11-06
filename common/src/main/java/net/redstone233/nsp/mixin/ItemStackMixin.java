package net.redstone233.nsp.mixin;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.redstone233.nsp.config.ClientConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemStack.class)
public class ItemStackMixin {

    @Shadow
    public Item getItem() {
        throw new AssertionError();
    }

    @Inject(method = "getMaxCount", at = @At("HEAD"), cancellable = true)
    private void onGetMaxCount(CallbackInfoReturnable<Integer> cir) {
        Item item = this.getItem();
        int vanillaMax = item.getMaxCount();
        int configMax = ClientConfig.getMaxItemStackCount();

        if (configMax > vanillaMax) {
            cir.setReturnValue(configMax);
        }
    }
}