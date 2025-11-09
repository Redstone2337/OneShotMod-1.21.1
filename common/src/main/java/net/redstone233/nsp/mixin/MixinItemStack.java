package net.redstone233.nsp.mixin;

import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.redstone233.nsp.config.ClientConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemStack.class)
public class MixinItemStack {

    @Inject(method = "getMaxCount", at = @At("HEAD"), cancellable = true)
    private void injectGetMaxCount(CallbackInfoReturnable<Integer> cir) {
        // 直接返回配置的最大堆叠数量
        int configMaxCount = ClientConfig.getMaxItemStackCount();
        cir.setReturnValue(configMaxCount);
    }

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