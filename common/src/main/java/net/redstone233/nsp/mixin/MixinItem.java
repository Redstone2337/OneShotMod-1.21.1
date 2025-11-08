package net.redstone233.nsp.mixin;

import net.minecraft.component.ComponentMap;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Item;
import net.redstone233.nsp.config.ClientConfig;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Item.class)
public abstract class MixinItem {
    @Mutable
    @Final
    @Shadow private ComponentMap components;

    @Unique
    private int oneShotMod_1_21_1$vanillaMaxCount;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void setVanillaMaxCount(Item.Settings settings, CallbackInfo ci) {
        oneShotMod_1_21_1$vanillaMaxCount = (Integer) this.components.getOrDefault(DataComponentTypes.MAX_STACK_SIZE, 64);
        // Apply config max count
        oneShotMod_1_21_1$applyConfigMaxCount();
    }

    @Unique
    private void oneShotMod_1_21_1$applyConfigMaxCount() {
        int configMaxCount = ClientConfig.getMaxItemStackCount();
        if (configMaxCount != 64) { // Only modify if different from default
            ComponentMap.Builder builder = ComponentMap.builder().addAll(this.components);
            builder.add(DataComponentTypes.MAX_STACK_SIZE, configMaxCount);
            this.components = builder.build();
        }
    }

    @Inject(method = "getMaxCount", at = @At("HEAD"), cancellable = true)
    private void injectGetMaxCount(CallbackInfoReturnable<Integer> cir) {
        cir.setReturnValue((Integer) this.components.getOrDefault(DataComponentTypes.MAX_STACK_SIZE, 64));
    }

    @Redirect(method = "isEnchantable", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;getMaxCount()I"))
    private int redirectGetMaxCount(ItemStack instance) {
        // Use vanilla max count for enchantability check
        return oneShotMod_1_21_1$vanillaMaxCount;
    }
}