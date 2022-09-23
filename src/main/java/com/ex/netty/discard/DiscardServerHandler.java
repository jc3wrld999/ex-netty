package com.ex.netty.discard;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

public class DiscardServerHandler extends SimpleChannelInboundHandler<Object> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception { // channelRead() 메서드가 자동으로 실행된다.
        // 데이터 입력 시 아무것도 하지않음
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) { // 지정한 포트로 접속한 클라이언트가 데이터를 전송하면
        cause.printStackTrace();
        ctx.close();
    }
    
}
