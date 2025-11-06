package net.redstone233.nsp.mixin;

import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.redstone233.nsp.config.ClientConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ServerPlayNetworkHandler.class)
public class ServerPlayNetworkHandlerMixin {

    /**
     * 重定向创意模式物品栏操作的堆叠数量检查
     */
    @Redirect(
            method = "onCreativeInventoryAction",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/item/ItemStack;getCount()I"
            )
    )
    private int redirectCreativeActionCountCheck(ItemStack stack) {
        int actualCount = stack.getCount();
        int configMax = ClientConfig.getMaxItemStackCount();
        int vanillaMax = stack.getItem().getMaxCount();

        // 如果堆叠数量在配置上限内但超过原版上限，返回原版上限以通过验证
        if (actualCount > vanillaMax && actualCount <= configMax) {
            return vanillaMax;
        }

        return actualCount;
    }
}