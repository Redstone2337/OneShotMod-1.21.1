package net.redstone233.nsp.mixin;

import net.minecraft.component.ComponentHolder;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.ComponentType;
import net.redstone233.nsp.config.ClientConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ComponentHolder.class)
public interface ComponentHolderMixin {

    @Inject(method = "getOrDefault", at = @At("HEAD"), cancellable = true)
    @SuppressWarnings("unchecked")
    private <T> void onGetOrDefault(ComponentType<T> type, T defaultValue, CallbackInfoReturnable<T> cir) {
        // 特别处理 MAX_STACK_SIZE 组件
        if (type == DataComponentTypes.MAX_STACK_SIZE) {
            ComponentHolder self = (ComponentHolder) this;

            // 获取原版的最大堆叠数
            int vanillaMax = self.getComponents().getOrDefault(DataComponentTypes.MAX_STACK_SIZE, 1);
            int configMax = ClientConfig.getMaxItemStackCount();

            if (configMax > vanillaMax) {
                // 返回配置的最大堆叠数
                cir.setReturnValue((T) Integer.valueOf(configMax));
            }
        }
    }
}