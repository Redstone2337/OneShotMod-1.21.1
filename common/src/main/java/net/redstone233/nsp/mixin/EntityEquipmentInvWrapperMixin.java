package net.redstone233.nsp.mixin;

import net.neoforged.neoforge.items.wrapper.EntityEquipmentInvWrapper;
import net.redstone233.nsp.OneShotMod;
import net.redstone233.nsp.util.StackSystemManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EntityEquipmentInvWrapper.class)
public class EntityEquipmentInvWrapperMixin {
    @Inject(method = "getSlotLimit", at = @At("HEAD"), cancellable = true)
    private void getSlotLimit(int slot, CallbackInfoReturnable<Integer> cir) {
        int customMaxCount = StackSystemManager.getCurrentStackCount();

        // 确保数值在有效范围内
        if (customMaxCount >= 1 && customMaxCount <= OneShotMod.CUSTOM_MAX_ITEM_STACK_COUNT) {
            cir.setReturnValue(customMaxCount);
        }
    }
}
