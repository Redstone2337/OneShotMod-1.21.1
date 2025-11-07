package net.redstone233.nsp.mixin;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.redstone233.nsp.config.ClientConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
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

    @Inject(method = "isStackable", at = @At("HEAD"), cancellable = true)
    private void onIsStackable(CallbackInfoReturnable<Boolean> cir) {
        ItemStack self = (ItemStack)(Object)this;
        int configMax = ClientConfig.getMaxItemStackCount();

        // 如果配置的最大堆叠数大于1，则认为物品可堆叠
        if (configMax > 1) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "capCount", at = @At("HEAD"), cancellable = true)
    private void onCapCount(int maxCount, CallbackInfo ci) {
        ItemStack self = (ItemStack)(Object)this;
        if (!self.isEmpty()) {
            int actualCount = self.getCount();
            int configMax = ClientConfig.getMaxItemStackCount();

            // 只有当超过配置的最大堆叠数时才需要限制
            if (actualCount > configMax) {
                self.setCount(configMax);
            }
            // 取消原方法的执行，因为我们已经在需要时处理了
            ci.cancel();
        }
    }
}