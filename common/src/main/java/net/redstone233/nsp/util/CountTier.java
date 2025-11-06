package net.redstone233.nsp.util;

/**
 * 物品数量级枚举
 * 用于根据物品数量确定显示颜色和格式化方式
 */
public enum CountTier {
    SINGLE(1, 0x888888, "单个物品"),
    SMALL(16, 0xFFFFFF, "小堆叠"),
    MEDIUM(64, 0x55FF55, "中等堆叠"),
    LARGE(256, 0x5555FF, "较大堆叠"),
    HUGE(1024, 0xFFFF55, "大堆叠"),
    GIANT(4096, 0xFFAA00, "非常大堆叠"),
    MASSIVE(16384, 0xFF5555, "巨大堆叠"),
    EXTREME(65536, 0xFF55FF, "超巨大堆叠"),
    ULTIMATE(Integer.MAX_VALUE, 0xAA00AA, "极限堆叠"),
    CUSTOM(-1, 0xFFFFFF, "自定义文本");

    private final int threshold;
    private final int color;
    private final String description;

    CountTier(int threshold, int color, String description) {
        this.threshold = threshold;
        this.color = color;
        this.description = description;
    }

    public int getThreshold() {
        return threshold;
    }

    public int getColor() {
        return color;
    }

    public String getDescription() {
        return description;
    }

    /**
     * 根据数量获取对应的数量级
     */
    public static CountTier fromCount(int count) {
        for (CountTier tier : values()) {
            if (tier == CUSTOM) continue; // 跳过自定义
            if (count <= tier.threshold) {
                return tier;
            }
        }
        return ULTIMATE;
    }

    /**
     * 获取下一个数量级的阈值
     */
    public static int getNextThreshold(int count) {
        CountTier currentTier = fromCount(count);
        for (CountTier tier : values()) {
            if (tier.threshold > count && tier != CUSTOM) {
                return tier.threshold;
            }
        }
        return Integer.MAX_VALUE;
    }
}