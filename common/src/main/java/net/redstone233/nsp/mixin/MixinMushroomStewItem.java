package net.redstone233.nsp.mixin;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Item.class)
public class MixinMushroomStewItem {

    @Inject(method = "finishUsing", at = @At("HEAD"), cancellable = true)
    private void stackableStew(ItemStack stack, World world, LivingEntity user, CallbackInfoReturnable<ItemStack> cir) {
        // Check if the item is MUSHROOM_STEW
        if (stack.getItem() == Items.MUSHROOM_STEW) {
            // Check if the stack is modified and the stack size is greater than or equal to 1
            // >= 1 because it is decreased by 1 before our code execution
            if (stack.getMaxCount() > 1 && stack.getCount() >= 1) {
                if (user instanceof PlayerEntity player) {
                    // Insert a bowl into the player's inventory instead of replacing the stew with a bowl
                    if (!player.getInventory().insertStack(new ItemStack(Items.BOWL))) {
                        player.dropItem(new ItemStack(Items.BOWL), false);
                    }
                }
                // Cancel the original logic and return the current stack (to keep the stew stackable)
                cir.setReturnValue(stack);
            }
        }
    }
}