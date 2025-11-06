package net.redstone233.nsp.mixin;

import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ScreenHandler.class)
public class ScreenHandlerMixin {

    /**
     * 修改物品堆叠合并逻辑，允许超过99个物品合并到同一格
     */
    @Inject(method = "insertItem", at = @At("HEAD"), cancellable = true)
    private void modifyInsertItem(ItemStack stack, int startIndex, int endIndex, boolean fromLast, CallbackInfoReturnable<Boolean> cir) {
        ScreenHandler handler = (ScreenHandler)(Object)this;

        if (stack.isEmpty()) {
            return;
        }

        boolean bl = false;
        int i = startIndex;
        if (fromLast) {
            i = endIndex - 1;
        }

        Slot slot;
        ItemStack itemStack;
        // 首先尝试合并到已有堆叠
        while (!stack.isEmpty() && (fromLast ? i >= startIndex : i < endIndex)) {
            slot = handler.slots.get(i);
            itemStack = slot.getStack();
            // 使用 ItemStack.areItemsAndComponentsEqual 替代不存在的 canCombine 方法
            if (ItemStack.areItemsAndComponentsEqual(stack, itemStack)) {
                int j = itemStack.getCount() + stack.getCount();
                // 使用 slot.getMaxItemCount(stack) 而不是 slot.getMaxItemCount()
                // 这样可以正确处理不同物品的最大堆叠数
                int maxCount = slot.getMaxItemCount(stack);
                if (j <= maxCount) {
                    stack.setCount(0);
                    itemStack.setCount(j);
                    slot.markDirty();
                    bl = true;
                } else if (itemStack.getCount() < maxCount) {
                    stack.decrement(maxCount - itemStack.getCount());
                    itemStack.setCount(maxCount);
                    slot.markDirty();
                    bl = true;
                }
            }
            if (fromLast) {
                --i;
            } else {
                ++i;
            }
        }

        // 如果还有剩余，尝试放入空槽位
        if (!stack.isEmpty()) {
            if (fromLast) {
                i = endIndex - 1;
            } else {
                i = startIndex;
            }
            while (!stack.isEmpty() && (fromLast ? i >= startIndex : i < endIndex)) {
                slot = handler.slots.get(i);
                itemStack = slot.getStack();
                if (itemStack.isEmpty() && slot.canInsert(stack)) {
                    // 使用 slot.getMaxItemCount(stack) 确保正确处理不同物品的最大堆叠数
                    int maxStackSize = slot.getMaxItemCount(stack);
                    if (stack.getCount() > maxStackSize) {
                        slot.setStack(stack.split(maxStackSize));
                    } else {
                        slot.setStack(stack.split(stack.getCount()));
                    }
                    slot.markDirty();
                    bl = true;
                    break;
                }
                if (fromLast) {
                    --i;
                } else {
                    ++i;
                }
            }
        }

        cir.setReturnValue(bl);
    }
}