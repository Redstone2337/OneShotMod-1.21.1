package net.redstone233.nsp.mixin;

import net.minecraft.component.ComponentMap;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Item;
import net.redstone233.nsp.util.IItemMaxCount;
import net.redstone233.nsp.util.ItemsHelper;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Item.class)
public abstract class MixinItem implements IItemMaxCount {
    @Mutable
    @Final
    @Shadow private ComponentMap components;

    @Unique
    private int oneShotMod_1_21_1$vanillaMaxCount;

    @Override
    public int oneShotMod_1_21_1$getVanillaMaxCount() {
        return oneShotMod_1_21_1$vanillaMaxCount;
    }

    @Override
    public void oneShotMod_1_21_1$setVanillaMaxCount(int vanillaMaxCount) {
        this.oneShotMod_1_21_1$vanillaMaxCount = vanillaMaxCount;
    }

    @Inject(method = "<init>", at = @At("RETURN"))
    private void setVanillaMaxCount(Item.Settings settings, CallbackInfo ci) {
        oneShotMod_1_21_1$setVanillaMaxCount(this.components.getOrDefault(DataComponentTypes.MAX_STACK_SIZE, ItemsHelper.getItemMaxCount()));
    }

    @Inject(method = "getMaxCount", at = @At("HEAD"), cancellable = true)
    private void injectGetMaxCount(CallbackInfoReturnable<Integer> cir) {
        cir.setReturnValue(this.components.getOrDefault(DataComponentTypes.MAX_STACK_SIZE, ItemsHelper.getItemMaxCount()));
    }

    @Redirect(method = "isEnchantable", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;getMaxCount()I"))
    private int redirectGetMaxCount(ItemStack instance) {
        return ((IItemMaxCount) instance.getItem()).oneShotMod_1_21_1$getVanillaMaxCount();
    }
}
