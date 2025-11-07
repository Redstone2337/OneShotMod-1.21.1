package net.redstone233.nsp.mixin;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.MilkBucketItem;
import net.minecraft.world.World;
import net.redstone233.nsp.util.ItemsHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MilkBucketItem.class)
public class MixinMilkBucketItem {

    @Inject(method = "finishUsing", at = @At(value = "INVOKE", shift = At.Shift.AFTER, target = "Lnet/minecraft/item/ItemStack;decrementUnlessCreative(ILnet/minecraft/entity/LivingEntity;)V"), cancellable = true)
    private void stackableMilkBucket(ItemStack stack, World world, LivingEntity user, CallbackInfoReturnable<ItemStack> cir) {
        if (!stack.isEmpty() && ItemsHelper.isModified(stack) && stack.getCount() > 1) {
            if (!world.isClient)
                user.clearStatusEffects();
            if (user instanceof PlayerEntity) {
                ItemsHelper.insertNewItem((PlayerEntity) user, new ItemStack(Items.BUCKET));
                cir.setReturnValue(stack);
            }
        }
    }
}
