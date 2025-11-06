package net.redstone233.nsp.mixin;

import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.redstone233.nsp.config.ClientConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(ServerPlayNetworkHandler.class)
public class ServerPlayNetworkHandlerMixin {

    @ModifyArg(
            method = "onCreativeInventoryAction",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/item/ItemStack;getCount()I"
            )
    )
    private ItemStack redirectCountCheck(ItemStack stack) {
        int actualCount = stack.getCount();
        int configMax = ClientConfig.getMaxItemStackCount();
        int vanillaMax = stack.getMaxCount();

        if (actualCount > vanillaMax && actualCount <= configMax) {
            // 返回修改后的 ItemStack
            ItemStack modifiedStack = stack.copy();
            modifiedStack.setCount(vanillaMax);
            return modifiedStack;
        }
        return stack;
    }
}