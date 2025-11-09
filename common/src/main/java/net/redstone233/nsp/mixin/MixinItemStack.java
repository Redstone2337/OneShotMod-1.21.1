package net.redstone233.nsp.mixin;

import com.mojang.serialization.DataResult;
import net.minecraft.item.ItemStack;
import net.redstone233.nsp.config.ClientConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemStack.class)
public class MixinItemStack {

    @Inject(method = "validate", at = @At("HEAD"), cancellable = true)
    private static void bypassStackCountValidation(ItemStack stack, CallbackInfoReturnable<DataResult<ItemStack>> cir) {
        // 绕过堆叠数量验证，允许超过原版限制
        int configMaxCount = ClientConfig.getMaxItemStackCount();
        if (stack.getCount() > configMaxCount) {
            // 如果超过配置的最大值，仍然报错
            cir.setReturnValue(DataResult.error(() -> "Item stack with stack size of " + stack.getCount() + " was larger than maximum: " + configMaxCount));
        } else {
            // 否则直接通过验证
            cir.setReturnValue(DataResult.success(stack));
        }
    }

    @Inject(method = "areItemsAndComponentsEqual", at = @At("HEAD"), cancellable = true)
    private static void allowStacking(ItemStack left, ItemStack right, CallbackInfoReturnable<Boolean> cir) {
        if (left.getItem() == right.getItem() && left.getComponents().equals(right.getComponents())) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "isStackable", at = @At("HEAD"), cancellable = true)
    private void allowLargeStacks(CallbackInfoReturnable<Boolean> cir) {
        ItemStack self = (ItemStack) (Object) this;
        // 修正：使用正确的可堆叠检查方法
        // 原版逻辑：可堆叠的条件是当前数量小于最大堆叠数量，并且如果物品可损坏，则必须没有损坏
        boolean isStackable = self.getCount() < self.getMaxCount() && (!self.isDamageable() || !self.isDamaged());
        cir.setReturnValue(isStackable);
    }

    @Inject(method = "getMaxCount", at = @At("HEAD"), cancellable = true)
    private void getActualMaxCount(CallbackInfoReturnable<Integer> cir) {
        int configMaxCount = ClientConfig.getMaxItemStackCount();
        cir.setReturnValue(configMaxCount);
    }
}