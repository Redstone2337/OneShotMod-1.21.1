package net.redstone233.nsp.mixin;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.MilkBucketItem;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 这是一个Mixin类，用于修改MilkBucketItem类的行为
 * 通过@Mixin注解指定要修改的目标类为MilkBucketItem
 */
@Mixin(MilkBucketItem.class)
public class MixinMilkBucketItem {

    /**
     * 注入方法，用于修改finishUsing方法的执行逻辑
     * @param stack 当前使用的物品堆
     * @param world 当前所在的世界
     * @param user 使用物品的实体
     * @param cir 回调信息，用于控制方法的返回值
     */
    @Inject(method = "finishUsing", at = @At(value = "INVOKE", shift = At.Shift.AFTER, target = "Lnet/minecraft/item/ItemStack;decrementUnlessCreative(ILnet/minecraft/entity/LivingEntity;)V"), cancellable = true)
    private void stackableMilkBucket(ItemStack stack, World world, LivingEntity user, CallbackInfoReturnable<ItemStack> cir) {
        // 检查物品堆是否非空且可以堆叠，并且当前数量大于1
        if (!stack.isEmpty() && stack.getMaxCount() > 1 && stack.getCount() > 1) {
            // 如果不是客户端世界，清除实体的所有状态效果
            if (!world.isClient)
                user.clearStatusEffects();
            // 如果用户是玩家实体
            if (user instanceof PlayerEntity player) {
                // Insert bucket logic
                if (!player.getInventory().insertStack(new ItemStack(Items.BUCKET))) {
                    player.dropItem(new ItemStack(Items.BUCKET), false);
                }
                cir.setReturnValue(stack);
            }
        }
    }
}