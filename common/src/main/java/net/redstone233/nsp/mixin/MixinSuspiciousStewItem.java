package net.redstone233.nsp.mixin;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.SuspiciousStewItem;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SuspiciousStewItem.class)
public class MixinSuspiciousStewItem {

    @Inject(method = "finishUsing", at = @At(value = "HEAD"), cancellable = true)
    private void stackableStew(ItemStack stack, World world, LivingEntity user, CallbackInfoReturnable<ItemStack> cir) {
        // >= 1 because it is decreased by 1 before our code execution
        if (stack.getMaxCount() > 1 && stack.getCount() >= 1) {
            if (user instanceof PlayerEntity player) {
                // Insert bowl logic
                if (!player.getInventory().insertStack(new ItemStack(Items.BOWL))) {
                    player.dropItem(new ItemStack(Items.BOWL), false);
                }
            }
            cir.setReturnValue(stack);
        }
    }
}