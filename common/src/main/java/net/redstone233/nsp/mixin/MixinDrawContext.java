package net.redstone233.nsp.mixin;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DrawContext.class)
public class MixinDrawContext {

    // 方法1：使用更安全的注入点
    @Inject(
            method = "drawItemInSlot(Lnet/minecraft/client/font/TextRenderer;Lnet/minecraft/item/ItemStack;IILjava/lang/String;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/util/math/MatrixStack;translate(FFF)V",
                    shift = At.Shift.AFTER
            )
    )
    private void captureItemCount(TextRenderer textRenderer, ItemStack stack, int x, int y, @Nullable String countOverride, CallbackInfo ci) {
        int oneShotMod_1_21_1$currentItemCount = stack.getCount();
    }

    // 方法2：修改颜色参数 - 使用正确的索引和类型
/*
    @ModifyArg(
            method = "drawItemInSlot(Lnet/minecraft/client/font/TextRenderer;Lnet/minecraft/item/ItemStack;IILjava/lang/String;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/DrawContext;drawText(Lnet/minecraft/client/font/TextRenderer;Ljava/lang/String;IIIZ)I"
            ),
            index = 4
    )
    private int modifyCountTextColor(int originalColor) {
        // 使用CountTier枚举根据数量获取对应的颜色
        CountTier tier = CountTier.fromCount(this.currentItemCount);
        return tier.getColor();
    }

 */
}