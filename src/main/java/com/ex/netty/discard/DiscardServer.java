package com.ex.netty.discard;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class DiscardServer {

    public void start() throws InterruptedException {
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        try {
            ServerBootstrap b = new ServerBootstrap(); // ServerBootstrap 생성
            b.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class) // // NIO 전송 채널을 이용하도록 지정
                // .localAddress(new InetSocketAddress(8888)) // 지정된 포트를 이용해 소켓 주소를 설정
                .childHandler(new ChannelInitializer<SocketChannel>() { // // EchoServerHandler 하나를 채널의 Channel Pipeline으로 추가

                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ChannelPipeline p = ch.pipeline();
                        // 접속된 클라이언트로부터 수신된 데이터를 처리할 핸들러를 지정
                        p.addLast(new DiscardServerHandler());
                    }

                });
            // bootstrap 클래스의 bind 메서드로 접속할 포트를 지정
            ChannelFuture f = b.bind(8888).sync(); // 서버를 비동기식으로 바인딩, sync()는 바인딩이 완료되기를 대기
            f.channel().closeFuture().sync(); // 채널의 CloseFuture를 얻고 완료될 때까지 현재 스레드를 블로킹

        } finally {
            // EventLoopGroup를 종료하고 모든 리소스를 해제
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }
}
