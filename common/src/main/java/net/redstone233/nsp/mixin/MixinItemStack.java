package net.redstone233.nsp.mixin;

import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ItemStack.class)
public class MixinItemStack {

    @Redirect(method = "damage(ILnet/minecraft/server/world/ServerWorld;Lnet/minecraft/server/network/ServerPlayerEntity;Ljava/util/function/Consumer;)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;setDamage(I)V"))
    private void splitStackedTools(ItemStack instance, int damage) {
        ItemStack rest = null;
        if (instance.getCount() > 1 && instance.getMaxCount() > 1 && instance.getHolder() != null && instance.getHolder().isPlayer()) {
            rest = instance.copy();
            rest.decrement(1);
            instance.setCount(1);
        }
        instance.setDamage(damage);
        if (rest != null) {
            // Simple item insertion logic
            ServerPlayerEntity player = (ServerPlayerEntity) instance.getHolder();
            if (!player.getInventory().insertStack(rest)) {
                player.dropItem(rest, false);
            }
        }
    }
}