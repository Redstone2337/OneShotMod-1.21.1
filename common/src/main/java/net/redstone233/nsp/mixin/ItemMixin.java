package net.redstone233.nsp.mixin;

import net.minecraft.item.Item;
import net.redstone233.nsp.OneShotMod;
import net.redstone233.nsp.config.ClientConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

@Mixin(Item.class)
public class ItemMixin {

    @Inject(method = "<clinit>", at = @At("TAIL"))
    private static void overrideMaxMaxCount(CallbackInfo ci) {
        try {
            // 使用反射在静态初始化阶段修改字段
            Field field = Item.class.getDeclaredField("MAX_MAX_COUNT");

            // 移除final修饰符
            Field modifiersField = Field.class.getDeclaredField("modifiers");
            modifiersField.setAccessible(true);
            modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);

            // 设置新值
            int newMaxCount = ClientConfig.getMaxItemStackCount();
            field.set(null, newMaxCount);

            OneShotMod.LOGGER.info("Successfully set MAX_MAX_COUNT to: {}", newMaxCount);

        } catch (Exception e) {
            OneShotMod.LOGGER.error("Failed to modify MAX_MAX_COUNT: {}", e.getMessage());
        }
    }
}