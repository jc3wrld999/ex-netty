package com.ex.netty.echo;

import java.nio.charset.Charset;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

public class EchoClientHandler extends ChannelInboundHandlerAdapter{
    
    @Override
    public void channelActive(ChannelHandlerContext ctx) { // 소켓 채널이 최초 활성화되었을 때 실행됨
        String sendMessage = "Hello, Netty";
        
        ByteBuf messageBuffer = Unpooled.buffer();
        messageBuffer.writeBytes(sendMessage.getBytes());

        StringBuilder builder = new StringBuilder();
        builder.append("전송한 문자열 [");
        builder.append(sendMessage);
        builder.append("]");

        System.out.println(builder.toString());
        
        ctx.writeAndFlush(messageBuffer); // 데이터 기록, 전송 두가지 메서드를 호출/ 첫번째는 채널에 데이터를 기록하는 write, 두번째는 채널에 기록된 데이터를 서버로 전송하는 flush
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) { // 서버로 수신된 데이터가 있을 때 호출
        String readMessage = ((ByteBuf)msg).toString(Charset.defaultCharset()); // 서버로부터 수신된 데이터가 저장된 msg 객체에서 문자열 데이터를 추출

        StringBuilder builder = new StringBuilder();
        builder.append("수신한 문자열 [");
        builder.append(readMessage);
        builder.append("]");

        System.out.println(builder.toString());
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) { // 수신된 데이터를 모두 읽었을 때 호출/ ChannelRead가 완료된 후 자동으로 호출
        ctx.close();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}
