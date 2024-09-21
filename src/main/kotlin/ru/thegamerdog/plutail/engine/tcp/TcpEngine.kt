package ru.thegamerdog.plutail.engine.tcp

import io.netty.bootstrap.ServerBootstrap
import io.netty.buffer.Unpooled
import io.netty.channel.ChannelInitializer
import io.netty.channel.ChannelOption
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.SocketChannel
import io.netty.channel.socket.nio.NioServerSocketChannel
import io.netty.handler.codec.DelimiterBasedFrameDecoder
import io.netty.handler.codec.string.StringDecoder
import io.netty.handler.codec.string.StringEncoder
import kotlinx.coroutines.CoroutineScope
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.slf4j.Logger
import ru.thegamerdog.plutail.config.IConfiguration
import ru.thegamerdog.plutail.user.TcpUser

interface ITcpEngine {
    suspend fun run(scope: CoroutineScope)
    suspend fun stop()
}

class TcpEngine : ITcpEngine, KoinComponent {
    private val config by inject<IConfiguration>()
    private val logger by inject<Logger>()

    private val bossGroup = NioEventLoopGroup()
    private val workerGroup = NioEventLoopGroup()

    override suspend fun run(scope: CoroutineScope) {
        logger.info("Started TCP server")

        val serverBootstrap = ServerBootstrap()
        serverBootstrap.group(bossGroup, workerGroup)
            .channel(NioServerSocketChannel::class.java)
            .option(ChannelOption.SO_BACKLOG, 128)
            .childOption(ChannelOption.SO_KEEPALIVE, true)
            .childHandler(object : ChannelInitializer<SocketChannel>() {
                override fun initChannel(socketChannel: SocketChannel) {
                    val delimiter = Unpooled.wrappedBuffer(byteArrayOf(0x0.toByte()))
                    val pipeline = socketChannel.pipeline()

                    pipeline.addLast("frameDecoder", DelimiterBasedFrameDecoder(1024, delimiter))
                    pipeline.addLast("decoder", StringDecoder())

                    pipeline.addLast("encoder", StringEncoder())
                    pipeline.addLast("frameEncoder", DelimiterBasedFrameEncoder())

                    pipeline.addLast("handler", TcpHandler())
                }
            })

        val channelFuture = serverBootstrap.bind(config.configData.tcpPort).sync()

        channelFuture.channel().closeFuture().sync()
    }

    override suspend fun stop() {
        bossGroup.shutdownGracefully();
        workerGroup.shutdownGracefully();

        logger.info("Stopped TCP server")
    }
}