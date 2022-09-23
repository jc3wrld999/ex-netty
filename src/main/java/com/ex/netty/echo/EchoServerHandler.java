package com.ex.netty.echo;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.util.CharsetUtil;

@Sharable // ChannelHandler를 여러 채널간에 안전하게 공유할 수 있도록 하는 것
public class EchoServerHandler extends ChannelInboundHandlerAdapter { // 입력된 데이터를 처리하는 이벤트 핸들러인 ChannelInboundHandlerAdapter를 상속

    // 데이터 수신 이벤트처리
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) { 
        ByteBuf in = (ByteBuf) msg; 
        System.out.println("Server received: " + in.toString(CharsetUtil.UTF_8)); // 
        ctx.write(in); // 아웃바운드 메시지를 플러시할지 않은 채로 받은 메시지를 발신자로 출력함.
    }

    // 채널 파이프라인에 대한 이벤트를 처리
    // channelRead의 이벤트 처리 완료 후 자동으로 수행됨
    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) { 
        ctx.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE); // 대기 중인 메시지를 원격 피어로 플러시하고 채널을 닫음
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) { 
        cause.printStackTrace(); // 예외 stack trace를 출력
        ctx.close(); // 채널을 닫음
    } 
}
