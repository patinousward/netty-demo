package com.patinousward.netty.udp.demo.broadcaster;

import com.patinousward.netty.udp.demo.LogEvent;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioDatagramChannel;

import java.io.File;
import java.io.RandomAccessFile;
import java.net.InetSocketAddress;

/**
 * Created by Administrator on 2019/8/7.
 */
public class LogEventBroadcaster {

    private final EventLoopGroup group;
    private final Bootstrap bootstrap;
    private final File file;

    public LogEventBroadcaster(InetSocketAddress inetSocketAddress, File file) {
        group = new NioEventLoopGroup();
        bootstrap = new Bootstrap();
        bootstrap.group(group).channel(NioDatagramChannel.class)
                .option(ChannelOption.SO_BROADCAST, true)
                .handler(new LogEventEncoder(inetSocketAddress));
        this.file = file;
    }

    public void run() throws Exception {
        Channel ch = bootstrap.bind(0).sync().channel();
        long pointer = 0;
        for (; ; ) {
            long len = file.length();
            if (len < pointer) {
                pointer = len;
            } else if (len > pointer) {
                RandomAccessFile raf = new RandomAccessFile(this.file, "r");
                raf.seek(pointer);
                String line;
                while ((line = raf.readLine()) != null) {
                    //System.out.println(System.currentTimeMillis() + "send msg");
                    ch.writeAndFlush(new LogEvent(null, -1, file.getAbsolutePath(), line));
                }
                pointer = raf.getFilePointer();
                raf.close();
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.interrupted();
                break;
            }
        }
    }

    public void stop() {
        group.shutdownGracefully();
    }

    public static void main(String[] args) {
        if (args.length != 2) {
            throw new IllegalArgumentException();
        }
        LogEventBroadcaster broadcaster = new LogEventBroadcaster(new InetSocketAddress("255.255.255.255",Integer.parseInt(args[0])), new File(args[1]));
        try {
            broadcaster.run();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            broadcaster.stop();
        }
    }
}
