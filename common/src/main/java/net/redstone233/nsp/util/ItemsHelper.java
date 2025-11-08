package net.redstone233.nsp.util;

import net.minecraft.component.type.NbtComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.block.ShulkerBoxBlock;
import net.minecraft.item.BlockItem;
import net.minecraft.server.network.ServerPlayerEntity;
import net.redstone233.nsp.config.ClientConfig;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;

public class ItemsHelper {

    public static boolean isModified(ItemStack stack) {
        if (stack.isEmpty()) return false;

        // 使用配置系统中的最大堆叠数进行检查
        int configMaxStack = ClientConfig.getMaxItemStackCount();
        int currentCount = stack.getCount();

        // 如果当前堆叠数超过原版限制但不超过配置限制，则认为被修改
        return currentCount > stack.getMaxCount() && currentCount <= configMaxStack;
    }

    public static void insertNewItem(PlayerEntity player, ItemStack stack) {
        if (player == null || stack.isEmpty()) return;

        // 尝试将物品插入玩家库存
        if (!player.getInventory().insertStack(stack)) {
            // 如果库存已满，在玩家位置掉落物品
            if (player instanceof ServerPlayerEntity serverPlayer) {
                serverPlayer.dropItem(stack, false);
            }
        }
    }

    /**
     * 检查潜影盒是否有物品 - 适配 Minecraft 1.21 Data Components 系统
     */
    public static boolean shulkerBoxHasItems(ItemStack stack) {
        if (!(stack.getItem() instanceof BlockItem blockItem) ||
                !(blockItem.getBlock() instanceof ShulkerBoxBlock)) {
            return false;
        }

        // 新的 Data Components 系统替代了旧的 NBT 系统
        // 检查 BlockEntityData 组件
        NbtComponent blockEntityData = stack.get(DataComponentTypes.BLOCK_ENTITY_DATA);
        if (blockEntityData != null) {
            NbtCompound nbt = blockEntityData.copyNbt();
            if (nbt != null && nbt.contains("Items", 9)) {
                NbtList items = nbt.getList("Items", 10);
                return !items.isEmpty();
            }
        }

        return false;
    }

    /**
     * 获取配置的最大堆叠数
     */
    public static int getMaxStackCount() {
        return ClientConfig.getMaxItemStackCount();
    }

    /**
     * 检查物品是否可以堆叠到配置的最大值
     */
    public static boolean canStackToMax(ItemStack stack) {
        return stack.getCount() < getMaxStackCount();
    }

    /**
     * 安全地增加物品堆叠数，不超过配置的最大值
     */
    public static ItemStack safeIncrement(ItemStack stack, int amount) {
        int newCount = Math.min(stack.getCount() + amount, getMaxStackCount());
        stack.setCount(newCount);
        return stack;
    }

    /**
     * 检查物品是否达到配置的最大堆叠数
     */
    public static boolean isAtMaxStack(ItemStack stack) {
        return stack.getCount() >= getMaxStackCount();
    }
}