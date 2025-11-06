package net.redstone233.nsp.util;

import org.jetbrains.annotations.NotNull;

/**
 * 计数显示数据容器
 * 包含格式化后的文本、颜色和数量级信息
 */
public record CountDisplayData(String formattedText, int color, CountTier tier) {

    public CountDisplayData(String formattedText, CountTier tier) {
        this(formattedText, tier.getColor(), tier);
    }

    /**
     * 创建自定义文本的显示数据
     */
    public static CountDisplayData custom(String text) {
        return new CountDisplayData(text, CountTier.CUSTOM);
    }

    @Override
    public @NotNull String toString() {
        return String.format("CountDisplayData{text='%s', color=0x%06X, tier=%s}",
                formattedText, color, tier.name());
    }
}