package ru.thegamerdog.plutail.engine.tcp

import io.netty.buffer.ByteBuf
import io.netty.buffer.Unpooled
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.MessageToByteEncoder

class DelimiterBasedFrameEncoder : MessageToByteEncoder<String>() {
    override fun encode(ctx: ChannelHandlerContext, message: String, byteBuf: ByteBuf) {
        ctx.writeAndFlush(Unpooled.wrappedBuffer((message + 0x0.toChar()).encodeToByteArray()))
    }
}