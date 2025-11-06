package net.redstone233.nsp.mixin;

import net.minecraft.item.ItemStack;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.c2s.play.CreativeInventoryActionC2SPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(CreativeInventoryActionC2SPacket.class)
public class CreativeInventoryActionC2SPacketMixin {

    @Redirect(
            method = "<clinit>",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/item/ItemStack;createExtraValidatingPacketCodec(Lnet/minecraft/network/codec/PacketCodec;)Lnet/minecraft/network/codec/PacketCodec;"
            )
    )
    private static PacketCodec<RegistryByteBuf, ItemStack> replaceValidatingCodec(PacketCodec<RegistryByteBuf, ItemStack> original) {
        // 使用不验证的编解码器替代验证编解码器
        return ItemStack.OPTIONAL_PACKET_CODEC;
    }
}