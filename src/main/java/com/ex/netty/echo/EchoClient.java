package com.ex.netty.echo;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

public class EchoClient {

    public static void main(String[] args) throws Exception {
        connect();
    }
    
    public static void connect() throws InterruptedException {
        EventLoopGroup group = new NioEventLoopGroup();

        try {
            Bootstrap b = new Bootstrap();
            b.group(group) // 클라이언트는 서버와 달리 서버에 연결된 채널 하나만 존재하기 때문에 이벤트 루프 그룹이 하나다.
                .channel(NioSocketChannel.class) // 클라이언트가 생성하는 채널의 종류를 설정. NIO 소켓 채널로 설정
                .handler(new ChannelInitializer<SocketChannel>() { // 클라이언트이므로 채널 파이프라인의 설정에 일반 소켓 채널 클래스인 SocketChannel을 설정
    
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ChannelPipeline p = ch.pipeline();
                        p.addLast(new EchoClientHandler());
                    }
                    
                });
            // 비동기 입출력 메서드인 connect 호출. ChannelFuture 객체를 리턴/ 이 객체를 통해 비동기 처리 결과를 확인
            // sync는 ChannelFuture 객체의 요청이 완료될 때까지 대기/ 실패하면 예외 던짐
            ChannelFuture f = b.connect("localhost", 8888).sync(); 
            
            f.channel().closeFuture().sync();
        } finally {
            group.shutdownGracefully();
        }
    }
}
