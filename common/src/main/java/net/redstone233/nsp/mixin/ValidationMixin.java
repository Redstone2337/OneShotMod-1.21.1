package net.redstone233.nsp.mixin;

import com.mojang.serialization.DataResult;
import net.minecraft.item.ItemStack;
import net.redstone233.nsp.config.ClientConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemStack.class)
public class ValidationMixin {

    @Inject(method = "validate", at = @At("HEAD"), cancellable = true)
    private static void onValidate(ItemStack stack, CallbackInfoReturnable<DataResult<ItemStack>> cir) {
        if (!stack.isEmpty()) {
            int actualCount = stack.getCount();
            int configMax = ClientConfig.getMaxItemStackCount();
            int vanillaMax = stack.getMaxCount();

            // 如果堆叠数量超过原版上限但在配置上限内，通过验证
            if (actualCount > vanillaMax && actualCount <= configMax) {
                cir.setReturnValue(DataResult.success(stack));
            }
        }
    }
}