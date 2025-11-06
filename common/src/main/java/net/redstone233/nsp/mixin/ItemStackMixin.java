package net.redstone233.nsp.mixin;

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
    private int count;

    /**
     * 修改物品堆叠的最大数量限制
     */
    @Inject(method = "getMaxCount", at = @At("HEAD"), cancellable = true)
    private void modifyMaxStackCount(CallbackInfoReturnable<Integer> cir) {
        if (ClientConfig.getConfigProvider() != null) {
            int customMaxCount = ClientConfig.getMaxItemStackCount();
            if (customMaxCount > 64) { // 只在自定义值大于默认值时修改
                cir.setReturnValue(customMaxCount);
            }
        }
    }

    /**
     * 修改物品是否可堆叠的判断
     */
    @Inject(method = "isStackable", at = @At("HEAD"), cancellable = true)
    private void modifyStackable(CallbackInfoReturnable<Boolean> cir) {
        if (ClientConfig.getConfigProvider() != null) {
            int customMaxCount = ClientConfig.getMaxItemStackCount();
            if (customMaxCount > 1) { // 如果最大堆叠数大于1，则认为可堆叠
                cir.setReturnValue(true);
            }
        }
    }
}