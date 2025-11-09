package net.redstone233.nsp.mixin;

import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ScreenHandler.class)
public class MixinScreenHandler {

    @Inject(method = "insertItem", at = @At("HEAD"), cancellable = true)
    private void allowLargeStackInsertion(ItemStack stack, int startIndex, int endIndex, boolean fromLast, CallbackInfoReturnable<Boolean> cir) {
        // 允许插入大堆叠物品
        if (stack.getCount() > 64) {
            // 这里需要实现大堆叠物品的插入逻辑
            // 暂时返回false，让原版逻辑处理
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "canInsertItemIntoSlot", at = @At("HEAD"), cancellable = true)
    private static void allowLargeStackInsertionIntoSlot(Slot slot, ItemStack stack, boolean allowOverflow, CallbackInfoReturnable<Boolean> cir) {
        // 允许大堆叠物品插入槽位
        if (stack.getCount() > 64) {
            cir.setReturnValue(true);
        }
    }
}