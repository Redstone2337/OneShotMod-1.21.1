package net.redstone233.nsp.mixin;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.item.ItemStack;
import net.redstone233.nsp.util.CountTier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DrawContext.class)
public class MixinDrawContext {

    @Unique
    private int oneShotMod_1_21_1$currentItemCount = 1;

    /**
     * 捕获当前物品数量
     */
    @Inject(
            method = "drawItemInSlot(Lnet/minecraft/client/font/TextRenderer;Lnet/minecraft/item/ItemStack;IILjava/lang/String;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/util/math/MatrixStack;translate(FFF)V",
                    shift = At.Shift.AFTER
            )
    )
    private void captureItemCount(TextRenderer textRenderer, ItemStack stack, int x, int y, String countOverride, CallbackInfo ci) {
        this.oneShotMod_1_21_1$currentItemCount = stack.getCount();
    }

    /**
     * 只修改颜色参数，不改变其他渲染逻辑
     * 这样可以避免Tooltip偏移问题
     */
    @ModifyArg(
            method = "drawItemInSlot(Lnet/minecraft/client/font/TextRenderer;Lnet/minecraft/item/ItemStack;IILjava/lang/String;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/DrawContext;drawText(Lnet/minecraft/client/font/TextRenderer;Ljava/lang/String;IIIZ)I"
            ),
            index = 4 // 颜色参数的索引
    )
    private int modifyCountTextColor(int color) {
        // 使用CountTier枚举根据数量获取对应的颜色
        CountTier tier = CountTier.fromCount(this.oneShotMod_1_21_1$currentItemCount);
        return tier.getColor();
    }
}