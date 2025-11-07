package net.redstone233.nsp.mixin;

import net.minecraft.block.ShulkerBoxBlock;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.redstone233.nsp.util.ItemsHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemStack.class)
public class MixinItemStack {

    @Inject(method = "getMaxCount", at = @At("HEAD"), cancellable = true)
    private void preventItemSBoxStack(CallbackInfoReturnable<Integer> cir) {
            ItemStack self = (ItemStack) (Object) this;
            if ((self.getItem() instanceof BlockItem) && (((BlockItem) self.getItem()).getBlock() instanceof ShulkerBoxBlock)) {
                if (ItemsHelper.shulkerBoxHasItems(self)) {
                    cir.setReturnValue(1);
                }
            }
    }

    @Redirect(method = "damage(ILnet/minecraft/server/world/ServerWorld;Lnet/minecraft/server/network/ServerPlayerEntity;Ljava/util/function/Consumer;)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;setDamage(I)V"))
    private void splitStackedTools(ItemStack instance, int damage) {
        ItemStack rest = null;
        if (instance.getCount() > 1 && ItemsHelper.isModified(instance) && instance.getHolder() != null && instance.getHolder().isPlayer()) {
            rest = instance.copy();
            rest.decrement(1);
            instance.setCount(1);
        }
        instance.setDamage(damage);
        if (rest != null) {
            ItemsHelper.insertNewItem((ServerPlayerEntity) instance.getHolder(), rest);
        }
    }
}
