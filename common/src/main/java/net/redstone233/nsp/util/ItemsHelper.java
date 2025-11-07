package net.redstone233.nsp.util;

import net.minecraft.component.ComponentMap;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.redstone233.nsp.OneShotMod;
import net.redstone233.nsp.config.ClientConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;


public class ItemsHelper {
    public static final int ItemMaxCount = ClientConfig.getMaxItemStackCount();
    private static ItemsHelper itemsHelper;
    private static final Logger LOGGER = LoggerFactory.getLogger("ItemsHelper");

    private ItemsHelper() {
    }

    public static ItemsHelper getItemsHelper() {
        if (itemsHelper == null) {
            itemsHelper = new ItemsHelper();
        }
        return itemsHelper;
    }

    public void resetAll(boolean serverSide) {
        for (Map.Entry<RegistryKey<Item>, Item> itemEntry : getItemSet()) {
            Item item = itemEntry.getValue();
            ((IItemMaxCount) item).oneShotMod_1_21_1$revert();
        }
        if (serverSide) LOGGER.info("[All Stackable] Reset all items");
    }

    public void resetItem(Item item) {
        ((IItemMaxCount) item).oneShotMod_1_21_1$revert();
        LOGGER.info("[All Stackable] Reset {}", Registries.ITEM.getId(item).toString());
    }

    public void setCountByConfig(Set<Map.Entry<String, Integer>> configSet, boolean serverSide) {
        resetAll(serverSide);
        for (Map.Entry<String, Integer> entry : configSet) {
            Item item = Registries.ITEM.get(Identifier.of(entry.getKey()));
            int size = Integer.min(entry.getValue(), ItemsHelper.ItemMaxCount);
            if (serverSide)
                OneShotMod.LOGGER.info("[All Stackable] Set {} to {}", entry.getKey(), size);
            else
                LOGGER.info("[All Stackable] [Client] Set {} to {}", entry.getKey(), size);
            ((IItemMaxCount) item).oneShotMod_1_21_1$setMaxCount(size);

        }
    }

    public int getDefaultCount(Item item) {
        return ((IItemMaxCount) item).oneShotMod_1_21_1$getVanillaMaxCount();
    }

    public int getCurrentCount(Item item) {
        return item.getMaxCount();
    }

    public boolean isVanilla(Item item) {
        return getDefaultCount(item) == getCurrentCount(item);
    }

    public void setSingle(Item item, int count) {
        ((IItemMaxCount) item).oneShotMod_1_21_1$setMaxCount(count);
        LOGGER.info("[All Stackable] Set "+Registries.ITEM.getId(item).toString()+" to "+ count);
    }

    public LinkedList<Item> getAllModifiedItems() {
        LinkedList<Item> list = new LinkedList<>();
        for (Map.Entry<RegistryKey<Item>, Item> itemEntry : getItemSet()) {
            Item item = itemEntry.getValue();
            if (getDefaultCount(item) != getCurrentCount(item) && !list.contains(item)) {
                list.add(item);
            }
        }
        return list;
    }

    public int setMatchedItems(int originalSize, int newSize, String type) {
        int counter = 0;
        switch (type) {
            case "vanilla":
                for (Map.Entry<RegistryKey<Item>, Item> itemEntry : getItemSet()) {
                    Item item = itemEntry.getValue();
                    if (isVanilla(item) && getCurrentCount(item) == originalSize) {
                        setSingle(item, newSize);
                        counter++;
                    }
                }
                break;
            case "modified":
                for (Map.Entry<RegistryKey<Item>, Item> itemEntry : getItemSet()) {
                    Item item = itemEntry.getValue();
                    if (!isVanilla(item) && getCurrentCount(item) == originalSize) {
                        setSingle(item, newSize);
                        counter++;
                    }
                }
                break;
            case "all":
                for (Map.Entry<RegistryKey<Item>, Item> itemEntry : getItemSet()) {
                    Item item = itemEntry.getValue();
                    if (getCurrentCount(item) == originalSize) {
                        setSingle(item, newSize);
                        counter++;
                    }
                }
                break;
        }

        return counter;
    }

    public LinkedHashMap<String, Integer> getNewConfigMap() {
        LinkedHashMap<String, Integer> map = new LinkedHashMap<>();
        for (Map.Entry<RegistryKey<Item>, Item> itemEntry : getItemSet()) {
            Item item = itemEntry.getValue();
            String id = Registries.ITEM.getId(item).toString();
            if (getDefaultCount(item) != getCurrentCount(item) && !map.containsKey(id)) {
                map.put(id, item.getMaxCount());
            }
        }
        return map;
    }


    private Set<Map.Entry<RegistryKey<Item>, Item>> getItemSet() {
        return Registries.ITEM.getEntrySet();
    }


    // From nbt/Tag.java createTag()
    public static final int TAG_END = 0;
    public static final int TAG_BYTE = 1;
    public static final int TAG_SHORT = 2;
    public static final int TAG_INT = 3;
    public static final int TAG_LONG = 4;
    public static final int TAG_FLOAT = 5;
    public static final int TAG_DOUBLE = 6;
    public static final int TAG_BYTEARRAY = 7;
    public static final int TAG_STRING = 8;
    public static final int TAG_LIST = 9;
    public static final int TAG_COMPOUND = 10;
    public static final int TAG_INTARRAY = 11;
    public static final int TAG_LONGARRAY = 12;

    public static boolean shulkerBoxHasItems(ItemStack stack) {
        ComponentMap tag = stack.getComponents();

        if (tag == null || !tag.contains(DataComponentTypes.BLOCK_ENTITY_DATA))
            return false;

        NbtCompound bet = Objects.requireNonNull(tag.get(DataComponentTypes.BLOCK_ENTITY_DATA)).copyNbt();
        return bet.contains("Items", TAG_LIST) && !bet.getList("Items", TAG_COMPOUND).isEmpty();
    }

    public static void insertNewItem(PlayerEntity player, Hand hand, ItemStack stack1, ItemStack stack2) {
        if (stack1.isEmpty()) {
            player.setStackInHand(hand, stack2);
        } else if (!player.getInventory().insertStack(stack2)) {
            player.dropItem(stack2, false);
        }
//        if (player instanceof ServerPlayerEntity) {
//            ((ServerPlayerEntity) player).refreshScreenHandler((ScreenHandler) player.playerScreenHandler);
//        }
    }

    public static void insertNewItem(PlayerEntity player, ItemStack stack2) {
        if (!player.getInventory().insertStack(stack2)) {
            player.dropItem(stack2, false);
        }
//        if (player instanceof ServerPlayerEntity) {
//            ((ServerPlayerEntity) player).refreshScreenHandler((ScreenHandler) player.playerScreenHandler);
//        }
    }

    public static boolean isModified(ItemStack s) {
        if (s.isEmpty()) {
            return false;
        }
        Item i = s.getItem();
        return ((IItemMaxCount) i).oneShotMod_1_21_1$getVanillaMaxCount() != i.getMaxCount();
    }
}
