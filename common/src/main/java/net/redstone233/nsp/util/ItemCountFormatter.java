package net.redstone233.nsp.util;

/**
 * 物品数量格式化工具
 * 提供智能的数字格式化和颜色计算功能
 */
public class ItemCountFormatter {
    // 格式化阈值
    private static final int FORMAT_THRESHOLD_K = 1000;    // 开始使用k格式
    private static final int FORMAT_THRESHOLD_M = 1000000; // 开始使用M格式
    private static final int FORMAT_THRESHOLD_B = 1000000000; // 开始使用B格式
    private static final int MAX_DISPLAY_COUNT = 999999999;

    private ItemCountFormatter() {
        // 工具类，防止实例化
    }

    /**
     * 智能数字格式化
     */
    public static String formatNumber(int count) {
        // 处理极限情况
        if (count > MAX_DISPLAY_COUNT) {
            return "∞";
        }

        // 根据数量级选择格式化策略
        CountTier tier = CountTier.fromCount(count);

        switch (tier) {
            case SINGLE:
            case SMALL:
            case MEDIUM:
            case LARGE:
                // 小到中等数量：直接显示完整数字
                return String.valueOf(count);

            case HUGE:
                // 大堆叠：使用小数格式（1.2k）
                return formatWithPrecision(count, 1000.0, "k");

            case GIANT:
            case MASSIVE:
                // 非常大到巨大堆叠：使用整数格式（12k）
                return formatWithoutPrecision(count, 1000, "k");

            case EXTREME:
            case ULTIMATE:
                // 超巨大到极限堆叠：使用百万格式
                if (count < FORMAT_THRESHOLD_M) {
                    return formatWithoutPrecision(count, 1000, "k");
                } else if (count < FORMAT_THRESHOLD_B) {
                    return formatWithPrecision(count, 1000000.0, "M");
                } else {
                    return formatWithPrecision(count, 1000000000.0, "B");
                }

            default:
                return String.valueOf(count);
        }
    }

    /**
     * 带精度的格式化（显示小数位）
     */
    private static String formatWithPrecision(int count, double divisor, String suffix) {
        double value = count / divisor;
        // 如果是整数值，不显示小数位
        if (value == (int) value) {
            return String.format("%d%s", (int) value, suffix);
        } else {
            return String.format("%.1f%s", value, suffix);
        }
    }

    /**
     * 不带精度的格式化（只显示整数）
     */
    private static String formatWithoutPrecision(int count, int divisor, String suffix) {
        return String.format("%d%s", count / divisor, suffix);
    }

    /**
     * 处理计数数据，包括格式化和颜色计算
     */
    public static CountDisplayData processCountData(int count, String countOverride) {
        // 处理自定义文本
        if (countOverride != null) {
            return CountDisplayData.custom(countOverride);
        }

        // 格式化数字
        String formattedText = formatNumber(count);

        // 确定数量级和颜色
        CountTier tier = CountTier.fromCount(count);

        return new CountDisplayData(formattedText, tier);
    }

    /**
     * 快速格式化方法，用于简单场景
     */
    public static String formatQuick(int count) {
        if (count > MAX_DISPLAY_COUNT) return "∞";
        if (count < 1000) return String.valueOf(count);
        if (count < 10000) return String.format("%.1fk", count / 1000.0);
        if (count < 1000000) return String.format("%dk", count / 1000);
        return String.format("%.1fM", count / 1000000.0);
    }
}