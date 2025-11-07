package net.redstone233.nsp.mixin;

import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.redstone233.nsp.config.ClientConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ScreenHandler.class)
public class ScreenHandlerMixin {

    @Redirect(
            method = "canInsertItemIntoSlot",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/item/ItemStack;getMaxCount()I"
            )
    )
    private static int redirectCanInsertGetMaxCount(ItemStack stack) {
        int vanillaMax = stack.getMaxCount();
        int configMax = ClientConfig.getMaxItemStackCount();
        return Math.max(vanillaMax, configMax);
    }

    @Redirect(
            method = "calculateStackSize",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/item/ItemStack;getMaxCount()I"
            )
    )
    private static int redirectCalculateStackSizeGetMaxCount(ItemStack stack) {
        int vanillaMax = stack.getMaxCount();
        int configMax = ClientConfig.getMaxItemStackCount();
        return Math.max(vanillaMax, configMax);
    }
}