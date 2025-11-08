package net.redstone233.nsp.mixin;

import net.redstone233.nsp.util.CountTier;
import net.redstone233.nsp.util.CountDisplayData;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DrawContext.class)
public class MixinDrawContext {

    @Unique
    private CountDisplayData currentDisplayData;

    @Inject(
            method = "drawItemInSlot(Lnet/minecraft/client/font/TextRenderer;Lnet/minecraft/item/ItemStack;IILjava/lang/String;)V",
            at = @At("HEAD")
    )
    private void onDrawItemSlotHead(TextRenderer textRenderer, ItemStack stack, int x, int y, @Nullable String countOverride, CallbackInfo ci) {
        // 在方法开始时准备显示数据
        if (!stack.isEmpty() && (stack.getCount() != 1 || countOverride != null)) {
            int count = stack.getCount();
            CountTier tier = CountTier.fromCount(count);

            // 创建显示数据
            String displayText = countOverride == null ? String.valueOf(count) : countOverride;
            this.currentDisplayData = new CountDisplayData(displayText, tier);
        } else {
            this.currentDisplayData = null;
        }
    }

    @ModifyVariable(
            method = "drawItemInSlot(Lnet/minecraft/client/font/TextRenderer;Lnet/minecraft/item/ItemStack;IILjava/lang/String;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/DrawContext;drawText(Lnet/minecraft/client/font/TextRenderer;Ljava/lang/String;IIIZ)I"
            ),
            argsOnly = true,
            index = 6 // 假设颜色参数是第6个（从0开始计数）
    )
    private int modifyColorVariable(int value) {
        if (this.currentDisplayData != null) {
            return this.currentDisplayData.color();
        }
        return value;
    }

    @Inject(
            method = "drawItemInSlot(Lnet/minecraft/client/font/TextRenderer;Lnet/minecraft/item/ItemStack;IILjava/lang/String;)V",
            at = @At("RETURN")
    )
    private void onDrawItemSlotReturn(TextRenderer textRenderer, ItemStack stack, int x, int y, @Nullable String countOverride, CallbackInfo ci) {
        // 在方法结束时清理数据
        this.currentDisplayData = null;
    }
}