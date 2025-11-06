package net.redstone233.nsp.util;

import org.jetbrains.annotations.NotNull;

/**
 * 渲染位置容器
 * 包含文本渲染的坐标和尺寸信息
 */
public record RenderPosition(int x, int y, int width) {

    /**
     * 创建基于原版物品栏位置计算的位置
     */
    public static RenderPosition forItemSlot(int slotX, int slotY, int textWidth) {
        int x = slotX + 19 - 2 - textWidth;  // 原版水平位置计算
        int y = slotY + 6 + 3;               // 原版垂直位置
        return new RenderPosition(x, y, textWidth);
    }

    @Override
    public @NotNull String toString() {
        return String.format("RenderPosition{x=%d, y=%d, width=%d}", x, y, width);
    }
}