package net.redstone233.nsp.mixin;

import com.mojang.serialization.DataResult;
import net.minecraft.item.ItemStack;
import net.minecraft.network.RegistryByteBuf;
import net.redstone233.nsp.config.ClientConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "net.minecraft.item.ItemStack$2") // 这是匿名编解码器类
public class ItemStackCodecMixin {

    @Inject(
            method = "decode(Lnet/minecraft/network/RegistryByteBuf;)Lnet/minecraft/item/ItemStack;",
            at = @At("RETURN"),
            cancellable = true
    )
    private void modifyDecodeValidation(RegistryByteBuf buf, CallbackInfoReturnable<ItemStack> cir) {
        ItemStack stack = cir.getReturnValue();
        if (stack != null && !stack.isEmpty()) {
            int actualCount = stack.getCount();
            int configMax = ClientConfig.getMaxItemStackCount();
            int vanillaMax = stack.getMaxCount();

            // 如果堆叠数量超过原版上限但在配置上限内，不抛出异常
            if (actualCount > vanillaMax && actualCount <= configMax) {
                // 允许通过，不抛出DecoderException
                cir.setReturnValue(stack);
            }
        }
    }
}