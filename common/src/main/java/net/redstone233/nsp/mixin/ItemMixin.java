package net.redstone233.nsp.mixin;

import net.minecraft.item.Item;
import net.minecraft.component.DataComponentTypes;
import net.redstone233.nsp.config.ClientConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Item.class)
public class ItemMixin {

    @Inject(method = "getMaxCount", at = @At("HEAD"), cancellable = true)
    private void onGetMaxCount(CallbackInfoReturnable<Integer> cir) {
        Item self = (Item)(Object)this;

        // 获取原版的最大堆叠数
        int vanillaMax = self.getComponents().getOrDefault(DataComponentTypes.MAX_STACK_SIZE, 1);
        int configMax = ClientConfig.getMaxItemStackCount();

        // 如果配置的最大堆叠数大于原版，则使用配置值
        if (configMax > vanillaMax) {
            cir.setReturnValue(configMax);
        }
    }
}