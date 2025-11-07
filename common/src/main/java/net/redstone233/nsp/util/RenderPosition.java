package net.redstone233.nsp.util;

import org.jetbrains.annotations.NotNull;

/**
 * 渲染位置容器
 * 包含文本渲染的坐标和尺寸信息
 */
public record RenderPosition(int x, int y, int width) {

    /**
     * 创建基于原版物品栏位置计算的位置
     * 修复了垂直偏移问题
     */
    public static RenderPosition forItemSlot(int slotX, int slotY, int textWidth) {
        // 原版水平位置计算：槽位宽度(16) + 边距(2) - 文本宽度 - 右边距(2)
        int x = slotX + 16 + 2 - textWidth - 2;

        // 原版垂直位置计算：槽位高度(16) - 文本高度(8) + 上边距(6) - 基线调整(1)
        int y = slotY + 16 - 8 + 6 - 1;

        return new RenderPosition(x, y, textWidth);
    }

    /**
     * 替代方法：使用更精确的原版计算方式
     */
    public static RenderPosition forItemSlotExact(int slotX, int slotY, int textWidth) {
        // 精确复制原版计算逻辑
        int x = slotX + 19 - 2 - textWidth;  // 19 = 16(槽位) + 3(内边距)
        int y = slotY + 6 + 3;               // 6 = 上边距, 3 = 文本基线调整

        return new RenderPosition(x, y, textWidth);
    }

    /**
     * 调试方法：显示位置信息
     */
    public static RenderPosition forItemSlotDebug(int slotX, int slotY, int textWidth, String debugInfo) {
        int x = slotX + 19 - 2 - textWidth;
        int y = slotY + 6 + 3;

        System.out.println("RenderPosition Debug - " + debugInfo + ":");
        System.out.println("  Slot: (" + slotX + ", " + slotY + ")");
        System.out.println("  Text Width: " + textWidth);
        System.out.println("  Final: (" + x + ", " + y + ")");

        return new RenderPosition(x, y, textWidth);
    }

    @Override
    public @NotNull String toString() {
        return String.format("RenderPosition{x=%d, y=%d, width=%d}", x, y, width);
    }
}