package net.redstone233.nsp.mixin;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.item.ItemStack;
import net.redstone233.nsp.config.ClientConfig;
import net.redstone233.nsp.util.CountDisplayData;
import net.redstone233.nsp.util.ItemCountFormatter;
import net.redstone233.nsp.util.RenderPosition;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 物品堆叠数量显示增强 Mixin
 * <p>
 * 功能特性：
 * 1. 突破99个物品的显示限制
 * 2. 根据数量级智能格式化数字（k, M, B）
 * 3. 颜色分级系统，便于视觉识别不同数量级
 * 4. 支持自定义计数文本
 * 5. 完整的配置兼容性
 */
@Mixin(DrawContext.class)
public class DrawContextMixin {

    // ==================== Mixin 注入点 ====================

    /**
     * 主注入方法 - 拦截物品数量渲染
     */
    @Inject(
            method = "drawItemInSlot(Lnet/minecraft/client/font/TextRenderer;Lnet/minecraft/item/ItemStack;IILjava/lang/String;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/DrawContext;drawText(Lnet/minecraft/client/font/TextRenderer;Ljava/lang/String;IIIZ)I",
                    ordinal = 0
            ),
            cancellable = true
    )
    private void onRenderItemCount(TextRenderer textRenderer, ItemStack stack, int x, int y, String countOverride, CallbackInfo ci) {
        // 检查前置条件
        if (!oneShotMod_1_21_1$shouldModifyRender(stack)) {
            return;
        }

        // 取消原版渲染，使用自定义渲染
        ci.cancel();
        oneShotMod_1_21_1$renderEnhancedItemCount((DrawContext)(Object)this, textRenderer, stack, x, y, countOverride);
    }

    // ==================== 核心渲染逻辑 ====================

    /**
     * 增强的物品数量渲染方法
     */
    @Unique
    private void oneShotMod_1_21_1$renderEnhancedItemCount(DrawContext context, TextRenderer textRenderer, ItemStack stack, int x, int y, String countOverride) {
        int count = stack.getCount();

        // 检查是否需要渲染（单个物品且无自定义文本时不渲染）
        if (oneShotMod_1_21_1$shouldSkipRendering(count, countOverride)) {
            return;
        }

        // 处理计数数据
        CountDisplayData displayData = ItemCountFormatter.processCountData(count, countOverride);

        // 计算渲染位置
        RenderPosition position = oneShotMod_1_21_1$calculateRenderPosition(textRenderer, displayData.formattedText(), x, y);

        // 执行渲染
        oneShotMod_1_21_1$renderCountText(context, textRenderer, displayData, position);
    }

    /**
     * 计算渲染位置
     */
    @Unique
    private RenderPosition oneShotMod_1_21_1$calculateRenderPosition(TextRenderer textRenderer, String text, int x, int y) {
        int textWidth = textRenderer.getWidth(text);
        return RenderPosition.forItemSlotExact(x, y, textWidth);
    }

    /**
     * 执行文本渲染
     */
    @Unique
    private void oneShotMod_1_21_1$renderCountText(DrawContext context, TextRenderer textRenderer, CountDisplayData data, RenderPosition position) {
        String text = data.formattedText();

        // 精确复制原版的渲染逻辑：
        // 1. 先绘制黑色阴影（向右下偏移1像素）
        context.drawText(textRenderer, text, position.x() + 1, position.y() + 1, 0x000000, false);

        // 2. 再绘制主文本（使用配置的颜色）
        context.drawText(textRenderer, text, position.x(), position.y(), data.color(), false);

        // 调试信息（可选）
        if (ClientConfig.isDebugMode()) {
            System.out.println("Rendered: '" + text + "' at (" + position.x() + ", " + position.y() +
                    ") with color: " + String.format("#%06X", data.color()));
        }
    }

    // ==================== 工具方法 ====================

    /**
     * 检查是否应该修改渲染
     */
    @Unique
    private boolean oneShotMod_1_21_1$shouldModifyRender(ItemStack stack) {
        return !stack.isEmpty() &&
                ClientConfig.getConfigProvider() != null &&
                ClientConfig.getMaxItemStackCount() > 99;
    }

    /**
     * 检查是否应该跳过渲染
     */
    @Unique
    private boolean oneShotMod_1_21_1$shouldSkipRendering(int count, String countOverride) {
        return count == 1 && countOverride == null;
    }
}