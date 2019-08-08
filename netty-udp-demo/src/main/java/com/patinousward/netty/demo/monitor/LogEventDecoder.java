package com.patinousward.netty.demo.monitor;

import com.patinousward.netty.demo.LogEvent;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.DatagramPacket;
import io.netty.handler.codec.MessageToMessageDecoder;
import io.netty.util.CharsetUtil;

import java.util.List;

public class LogEventDecoder extends MessageToMessageDecoder<DatagramPacket> {
    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, DatagramPacket datagramPacket, List<Object> list) throws Exception {
        ByteBuf data = datagramPacket.content();
        int index = data.indexOf(0, data.readableBytes(), LogEvent.SEPARATOR);
        String filename = data.slice(0, index).toString(CharsetUtil.UTF_8);
        String logMsg = data.slice(index + 1, data.readableBytes()).toString(CharsetUtil.UTF_8);
        LogEvent event = new LogEvent(datagramPacket.sender(), System.currentTimeMillis(), filename, logMsg);
        list.add(event);
    }
}
