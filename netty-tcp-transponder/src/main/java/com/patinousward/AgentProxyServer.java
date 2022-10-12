package com.patinousward;

import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

public class AgentProxyServer {
    Bootstrap bootstrap;
    ServerBootstrap server;

    NioEventLoopGroup bossGroup;
    NioEventLoopGroup workGroup;

    class DataHandler extends ChannelInboundHandlerAdapter {
        private Channel channel;

        public DataHandler(Channel channel) {
            this.channel = channel;
        }

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            ByteBuf readBuffer = (ByteBuf) msg;
            readBuffer.retain();
            channel.writeAndFlush(readBuffer);
            readBuffer.clear();//避免内存溢出
        }
    }

    void init() {
        // 初始化
        this.bossGroup = new NioEventLoopGroup();
        this.workGroup = new NioEventLoopGroup();
        this.server = new ServerBootstrap();
        this.bootstrap = new Bootstrap();
        bootstrap.channel(NioSocketChannel.class);
        bootstrap.group(bossGroup);
        this.server.group(bossGroup, workGroup);


        server.channel(NioServerSocketChannel.class).handler(new LoggingHandler(LogLevel.INFO))
                .childHandler(new ChannelInitializer<SocketChannel>(
                ) {
                    @Override
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        socketChannel.pipeline().addLast("serverHandler", new DataHandler(getClientChannel(socketChannel)));
                    }
                }).option(ChannelOption.SO_BACKLOG, 1024)
                .option(ChannelOption.SO_RCVBUF, 16 * 1024);

// 监听地址
        server.bind(50050).syncUninterruptibly().addListener((ChannelFutureListener) channelFuture -> {
            if (channelFuture.isSuccess()) {
                System.out.println("服务区启动成功");
            } else {
                System.out.println("服务器启动失败");
            }
        });
    }

    private Channel getClientChannel(SocketChannel ch) throws InterruptedException {
        this.bootstrap.handler(new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel socketChannel) throws Exception {
                socketChannel.pipeline().addLast("clientHandler", new DataHandler(ch));
            }
        });
//  转发地址
        ChannelFuture sync = bootstrap.connect("192.168.20.49", 22).sync();
        return sync.channel();
    }

    public static void main(String[] args) {
        AgentProxyServer agentProxyServer = new AgentProxyServer();
        agentProxyServer.init();
    }
}