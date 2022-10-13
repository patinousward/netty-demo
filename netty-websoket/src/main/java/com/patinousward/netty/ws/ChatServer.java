package com.patinousward.netty.ws;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandler;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.concurrent.ImmediateEventExecutor;

import java.net.InetSocketAddress;

/**
 * Created by Administrator on 2019/8/6.
 */
public class ChatServer {
    private final ChannelGroup channelGroup = new DefaultChannelGroup(ImmediateEventExecutor.INSTANCE);
    private final EventLoopGroup group = new NioEventLoopGroup();
    private Channel channel;

    public ChannelFuture start(InetSocketAddress address){
        ServerBootstrap serverBootstrap = new ServerBootstrap();
        serverBootstrap.group(group).channel(NioServerSocketChannel.class).childHandler(createInitializer(channelGroup));
        ChannelFuture future = serverBootstrap.bind(address);
        future.syncUninterruptibly();  //wait绑定完成后
        channel = future.channel();
        return  future;
    }

    private ChannelHandler createInitializer(ChannelGroup channelGroup) {
        return new ChatServerInitializer(channelGroup);
    }

    public void destory(){
        if(channel != null) channel.close();
        channelGroup.close();
        group.shutdownGracefully();
    }

    public static void main(String[] args) {
        if(args.length != 1){
            System.err.println("please give port");
            System.exit(1);
        }
        int port = Integer.parseInt(args[0]);
        final ChatServer endpoint = new ChatServer();
        ChannelFuture future = endpoint.start(new InetSocketAddress(port));
        Runtime.getRuntime().addShutdownHook(new Thread(){
            @Override
            public void run() {
                endpoint.destory();
            }
        });
        //future.channel() 和endpoint.channel 是同一个对象
        future.channel().closeFuture().syncUninterruptibly();
    }
}
