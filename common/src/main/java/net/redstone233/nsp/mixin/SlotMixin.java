package net.redstone233.nsp.mixin;

import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;
import net.redstone233.nsp.config.ClientConfig;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Slot.class)
public class SlotMixin {

    @Shadow
    @Final
    public Inventory inventory;

    @Shadow
    @Final
    private int index;

    /**
     * 修改槽位的最大物品数量限制
     */
    @Inject(method = "getMaxItemCount*", at = @At("HEAD"), cancellable = true)
    private void modifySlotMaxItemCount(CallbackInfoReturnable<Integer> cir) {
        if (ClientConfig.getConfigProvider() != null) {
            ItemStack stack = this.inventory.getStack(this.index);
            if (!stack.isEmpty()) {
                int customMaxCount = ClientConfig.getMaxItemStackCount();
                if (customMaxCount > 64) {
                    cir.setReturnValue(customMaxCount);
                }
            }
        }
    }

    /**
     * 修改槽位最大堆叠数量
     */
    @Inject(method = "getMaxItemCount(Lnet/minecraft/item/ItemStack;)I", at = @At("HEAD"), cancellable = true)
    private void modifySlotMaxItemCountForStack(ItemStack stack, CallbackInfoReturnable<Integer> cir) {
        if (ClientConfig.getConfigProvider() != null) {
            int customMaxCount = ClientConfig.getMaxItemStackCount();
            if (customMaxCount > 64) {
                cir.setReturnValue(customMaxCount);
            }
        }
    }
}