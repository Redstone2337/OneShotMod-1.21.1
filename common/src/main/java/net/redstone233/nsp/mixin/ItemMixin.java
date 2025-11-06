package net.redstone233.nsp.mixin;

import net.minecraft.item.Item;
import net.redstone233.nsp.config.ClientConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Item.class)
public class ItemMixin {

    /**
     * 修改物品的最大堆叠数量
     */
    @Inject(method = "getMaxCount", at = @At("HEAD"), cancellable = true)
    private void modifyItemMaxCount(CallbackInfoReturnable<Integer> cir) {
        if (ClientConfig.getConfigProvider() != null) {
            int customMaxCount = ClientConfig.getMaxItemStackCount();
            if (customMaxCount > 64) {
                cir.setReturnValue(customMaxCount);
            }
        }
    }
}