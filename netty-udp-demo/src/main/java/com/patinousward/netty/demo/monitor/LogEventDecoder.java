package com.patinousward.netty.demo.monitor;

import com.patinousward.netty.demo.LogEvent;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.SwappedByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import io.netty.util.CharsetUtil;

import java.net.DatagramPacket;
import java.net.InetSocketAddress;
import java.util.List;

public class LogEventDecoder extends MessageToMessageDecoder<DatagramPacket> {
    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, DatagramPacket datagramPacket, List<Object> list) throws Exception {
        byte[] data = datagramPacket.getData();
        ByteBuf buffer = Unpooled.buffer();
        ByteBuf byteBuf = buffer.setBytes(0, data);
        int index = byteBuf.indexOf(0, buffer.readableBytes(), LogEvent.SEPARATOR);
        String filename = buffer.slice(0, index).toString(CharsetUtil.UTF_8);
        String logMsg = buffer.slice(index + 1, byteBuf.readableBytes()).toString(CharsetUtil.UTF_8);
        LogEvent event = new LogEvent((InetSocketAddress) datagramPacket.getSocketAddress(), System.currentTimeMillis(), filename, logMsg);
        list.add(event);
    }
}
