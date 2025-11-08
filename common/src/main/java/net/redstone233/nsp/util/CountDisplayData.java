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

    /**
     * 将整数颜色值转换为十六进制颜色字符串
     * @param color 整数颜色值 (0xRRGGBB 格式)
     * @return 十六进制颜色字符串 (#RRGGBB 格式)
     */
    public static String colorToHexString(int color) {
        return String.format("#%06X", color & 0xFFFFFF);
    }

    /**
     * 将整数颜色值转换为十六进制颜色字符串（带Alpha通道）
     * @param color 整数颜色值 (0xAARRGGBB 格式)
     * @return 十六进制颜色字符串 (#AARRGGBB 格式)
     */
    public static String colorToHexStringWithAlpha(int color) {
        return String.format("#%08X", color);
    }

    /**
     * 获取当前颜色的十六进制字符串表示
     * @return 十六进制颜色字符串 (#RRGGBB 格式)
     */
    public String getColorHex() {
        return colorToHexString(this.color);
    }

    /**
     * 获取当前颜色的十六进制字符串表示（带Alpha通道）
     * @return 十六进制颜色字符串 (#AARRGGBB 格式)
     */
    public String getColorHexWithAlpha() {
        return colorToHexStringWithAlpha(this.color);
    }

    /**
     * 创建包含十六进制颜色字符串的显示数据
     * @param formattedText 格式化文本
     * @param hexColor 十六进制颜色字符串 (#RRGGBB 或 #AARRGGBB 格式)
     * @param tier 数量级
     * @return CountDisplayData 实例
     */
    public static CountDisplayData withHexColor(String formattedText, String hexColor, CountTier tier) {
        // 移除#号并解析十六进制字符串
        String cleanHex = hexColor.startsWith("#") ? hexColor.substring(1) : hexColor;
        int color;
        if (cleanHex.length() == 6) {
            color = Integer.parseInt(cleanHex, 16);
        } else if (cleanHex.length() == 8) {
            color = (int) Long.parseLong(cleanHex, 16);
        } else {
            throw new IllegalArgumentException("Invalid hex color format: " + hexColor);
        }
        return new CountDisplayData(formattedText, color, tier);
    }

    @Override
    public @NotNull String toString() {
        return String.format("CountDisplayData{text='%s', color=0x%06X, tier=%s}",
                formattedText, color, tier.name());
    }
}