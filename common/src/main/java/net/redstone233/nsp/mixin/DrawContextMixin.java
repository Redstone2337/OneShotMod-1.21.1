package net.redstone233.nsp.mixin;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.item.ItemStack;
import net.redstone233.nsp.config.ClientConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DrawContext.class)
public class DrawContextMixin {

    @Unique
    private static final int MAX_DISPLAY_COUNT = 999999;

    @Unique
    private static final float MIN_SCALE = 0.5f;

    @Unique
    private static final int DEFAULT_COLOR = 0xFFFFFF;

    @Unique
    private static final int WARNING_COLOR = 0xFF5555;

    @Unique
    private static final int ALERT_COLOR = 0xFFAA00;

    @Unique
    private static final int NOTICE_COLOR = 0xFFFF55;

    /**
     * 修改物品槽中的物品数量渲染，突破99个显示限制
     * 在 Fabric 1.21.1 中，物品数量渲染在 DrawContext.drawItemInSlot 方法中
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
    private void modifyItemCountRender(TextRenderer textRenderer, ItemStack stack, int x, int y, String countOverride, CallbackInfo ci) {
        if (stack.isEmpty()) {
            return;
        }

        // 检查是否启用了自定义堆叠配置
        if (ClientConfig.getConfigProvider() != null) {
            int customMaxCount = ClientConfig.getMaxItemStackCount();
            if (customMaxCount > 99) {
                // 取消原版的数量渲染
                ci.cancel();

                // 获取实际数量
                int count = stack.getCount();
                if (count == 1 && countOverride == null) {
                    return; // 数量为1且没有自定义标签，不渲染
                }

                // 使用我们的自定义渲染
                renderCustomItemCount((DrawContext)(Object)this, textRenderer, stack, x, y, countOverride);
            }
        }
    }

    @Unique
    private void renderCustomItemCount(DrawContext context, TextRenderer textRenderer, ItemStack stack, int x, int y, String countOverride) {
        int count = stack.getCount();
        String countString = countOverride != null ? countOverride : formatCount(count);

        // 计算缩放和位置
        float scale = calculateScale(countString);
        int stringWidth = textRenderer.getWidth(countString);
        int adjustedX = x + 19 - 2 - (int)(stringWidth * scale);
        int adjustedY = y + 6 + 3 + getVerticalOffset(scale); // 原版位置是 y + 6 + 3，我们在此基础上调整垂直偏移

        // 选择颜色
        int color = getTextColor(count, ClientConfig.getMaxItemStackCount());

        // 保存当前的变换矩阵
        context.getMatrices().push();
        context.getMatrices().translate(adjustedX, adjustedY, 0);
        context.getMatrices().scale(scale, scale, 1.0f);

        // 绘制文本 - 使用 DrawContext 的 drawText 方法
        context.drawText(textRenderer, countString, 0, 0, color, false);

        // 恢复变换矩阵
        context.getMatrices().pop();
    }

    @Unique
    private String formatCount(int count) {
        if (count > MAX_DISPLAY_COUNT) {
            return "∞"; // 超过最大显示数量显示无穷大符号
        }

        if (count < 1000) {
            return String.valueOf(count);
        } else if (count < 10000) {
            // 1,000-9,999: 显示为 1.2k
            float kValue = count / 1000.0f;
            if (kValue == (int) kValue) {
                return String.format("%dk", (int) kValue);
            } else {
                return String.format("%.1fk", kValue);
            }
        } else if (count < 1000000) {
            // 10,000-999,999: 显示为 12k
            return String.format("%dk", count / 1000);
        } else {
            // 1,000,000+: 显示为 1.2M
            float mValue = count / 1000000.0f;
            if (mValue == (int) mValue) {
                return String.format("%dM", (int) mValue);
            } else {
                return String.format("%.1fM", mValue);
            }
        }
    }

    @Unique
    private float calculateScale(String countString) {
        int length = countString.length();

        return switch (length) {
            case 1, 2 -> 1.0f;
            case 3 -> 0.9f;
            case 4 -> 0.8f;
            case 5 -> 0.7f;
            case 6 -> 0.6f;
            default -> MIN_SCALE;
        };
    }

    @Unique
    private int getVerticalOffset(float scale) {
        // 根据缩放调整垂直位置，保持居中
        return (int) ((9 - 9 * scale) / 2);
    }

    @Unique
    private int getTextColor(int count, int maxCount) {
        // 默认白色
        if (maxCount <= 0) return DEFAULT_COLOR;

        float percentage = (float) count / maxCount;

        if (percentage > 0.9f) {
            return WARNING_COLOR; // 红色警告（接近最大值）
        } else if (percentage > 0.75f) {
            return ALERT_COLOR; // 橙色警告
        } else if (percentage > 0.5f) {
            return NOTICE_COLOR; // 黄色提示
        } else {
            return DEFAULT_COLOR; // 白色正常
        }
    }
}