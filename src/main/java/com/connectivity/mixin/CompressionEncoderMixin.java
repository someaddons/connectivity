package com.connectivity.mixin;

import com.connectivity.Connectivity;
import com.connectivity.logging.PacketLogging;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.network.CompressionEncoder;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(CompressionEncoder.class)
public class CompressionEncoderMixin
{
    @Shadow
    @Final
    @Mutable
    private static boolean DISABLE_PACKET_DEBUG;

    @ModifyConstant(method = "encode(Lio/netty/channel/ChannelHandlerContext;Lio/netty/buffer/ByteBuf;Lio/netty/buffer/ByteBuf;)V", constant = @Constant(intValue = 8388608), require = 0)
    private int modifyLimit(final int constant)
    {
        if (Connectivity.config.getCommonConfig().disablePacketLimits)
        {
            return Integer.MAX_VALUE;
        }

        return constant;
    }

    @Inject(method = "encode(Lio/netty/channel/ChannelHandlerContext;Lio/netty/buffer/ByteBuf;Lio/netty/buffer/ByteBuf;)V", at = @At("HEAD"))
    private void onEncodeCheckLimit(final ChannelHandlerContext p_129452_, final ByteBuf p_129453_, final ByteBuf p_129454_, final CallbackInfo ci)
    {
        if (Connectivity.config.getCommonConfig().disablePacketLimits)
        {
            DISABLE_PACKET_DEBUG = true;
        }
    }
}
