package com.patinousward.netty.ws;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.group.ChannelGroup;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;

/**
 * Created by Administrator on 2019/8/6.
 */
public class TextWebSocketFrameHandler extends SimpleChannelInboundHandler<WebSocketFrame> {

    private final ChannelGroup channelGroup;

    public TextWebSocketFrameHandler(ChannelGroup channelGroup) {
        this.channelGroup = channelGroup;
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt == WebSocketServerProtocolHandler.ServerHandshakeStateEvent.HANDSHAKE_COMPLETE) {
            ctx.pipeline().remove(HttpRequestHandler.class);
            channelGroup.writeAndFlush(new TextWebSocketFrame("Client" + ctx.channel() + "joined"));
            channelGroup.add(ctx.channel());
        }else {
            super.userEventTriggered(ctx,evt);
        }
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, WebSocketFrame webSocketFrame) throws Exception {
        channelGroup.writeAndFlush(webSocketFrame.retain());
    }
}
