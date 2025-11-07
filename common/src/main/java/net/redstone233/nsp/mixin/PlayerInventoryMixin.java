package net.redstone233.nsp.mixin;

import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.redstone233.nsp.config.ClientConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerInventory.class)
public class PlayerInventoryMixin {

    @Inject(
            method = "canStackAddMore",
            at = @At("HEAD"),
            cancellable = true
    )
    private static void onCanStackAddMore(ItemStack existingStack, ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        // 你的自定义逻辑
        int configMax = ClientConfig.getMaxItemStackCount();
        int vanillaMax = existingStack.getMaxCount();

        if (configMax > vanillaMax) {
            int combinedCount = existingStack.getCount() + stack.getCount();
            if (combinedCount <= configMax) {
                cir.setReturnValue(true);
            }
        }
    }
}