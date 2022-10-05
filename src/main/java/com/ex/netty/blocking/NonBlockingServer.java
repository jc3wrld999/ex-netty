package com.ex.netty.blocking;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class NonBlockingServer {
    private Map<SocketChannel, List<byte[]>> keepDataTrack = new HashMap<>();
    private ByteBuffer buffer = ByteBuffer.allocate(2*1024);
    
    public void startEchoServer() {
        // finally에서 자원해제하는 걸 이렇게 할 수 있음
        try(
            Selector selector = Selector.open(); // 자신에게 등록된 채널에 변경 사항이 발생했는지 검사하고 변경 사항이 발생한 채널에 대한 접근을 가능하게 해준다.
            ServerSocketChannel serverSocketChannel = ServerSocketChannel.open() // 논블로킹 서버 소켓 채널을 생성/ 블로킹 소켓과 다르게 소켓 채널을 먼저 생성하고 사용할 포트를 바인딩한다.
        ) {
            if((serverSocketChannel.isOpen()) && (selector.isOpen())) { // 생성한 Selector 객체와 ServerSocketChannel 객체가 정상적으로 생성되었는지 확인
                serverSocketChannel.configureBlocking(false); // 소켓 채널의 블로킹모드의 기본값은 true. 설정하지 않으면 블로킹모드로 작동
                serverSocketChannel.bind(new InetSocketAddress(8888)); // 포트 지정하고 생성된 채널객체에 할당한다. 

                serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT); // 채널 객체를 selector 객체에 등록한다. selector가 감지할 이벤트는 연결 요쳥에해당하는 OP_ACCEPT
                System.out.println("접속 대기중");

                while(true) {
                    selector.select(); // selector에 등록된 채널에서 변경 사항이 발생했는지 검사. selector에 i/o 이벤트가 발생하지 않으면 스레드는 여기서 블로킹된다. 블로킹을 피하고 싶으면 selectNow 를 사용
                    Iterator<SelectionKey> keys = selector.selectedKeys().iterator(); // selector에 등록된 채널 중 i/o 이벤트가 발생한 채널 목록
                    
                    while(keys.hasNext()) {
                        SelectionKey key = (SelectionKey) keys.next();
                        keys.remove(); // 동일 이벤트 감지 방지
                    

                        if(!key.isValid()) {
                            continue;
                        }

                        if(key.isAcceptable()) { // 연결요청
                            this.acceptOP(key, selector);
                        } else if(key.isReadable()) { // 데이터 수신
                            this.readOP(key);
                        } else if(key.isWritable()) { // 데이터 쓰기가능
                            this.writeOP(key);
                        }
                    }
                }
            } else {
                System.out.println("서버 소켓을 생성하지 못했습니다.");
            }

        } catch(IOException ex) {
            
        }
    }

    private void acceptOP(SelectionKey key, Selector selector) throws IOException {
        ServerSocketChannel serverChannel = (ServerSocketChannel) key.channel(); // 연결 요청 이벤트가 발생한 채널은 항상 ServerSocketChannel이므로 이벤트가 발생한 채널을 타입 캐스팅
        SocketChannel socketChannel = serverChannel.accept(); // ServerSocketChannel을 이용하여 클라이언트의 연결을 수락하고 연결된 소켓 채널을 가져옴
        socketChannel.configureBlocking(false); // 연결된 클라이언트 소켓 채널을 논블로킹 모드로 설정

        System.out.println("클라이언트 연결됨 : " + socketChannel.getRemoteAddress());

        keepDataTrack.put(socketChannel, new ArrayList<byte[]>());
        socketChannel.register(selector, SelectionKey.OP_READ); // 클라이언트 소켓 채널을 selector에 등록하여 i/o 이벤트를 감시
    }

    private void readOP(SelectionKey key) {
        try {
            SocketChannel socketChannel = (SocketChannel) key.channel();
            buffer.clear();
            int numRead = -1;
            try {
                numRead = socketChannel.read(buffer);
            } catch (IOException e) {
                System.err.println("데이터 읽기 에러!");
            }

            if(numRead == -1) {
                this.keepDataTrack.remove(socketChannel);
                System.out.println("클라이언트 연결 종료 :" + socketChannel.getRemoteAddress());
                socketChannel.close();
                key.cancel();
                return;
            }
            byte[] data = new byte[numRead];
            System.arraycopy(buffer.array(), 0, data, 0, numRead);
            System.out.println(new String(data, "UTF-8") + " from " + socketChannel.getRemoteAddress());

            doEchoJob(key, data);
        } catch(IOException ex) {

        }
    }

    private void writeOP(SelectionKey key) throws IOException {
        SocketChannel socketChannel = (SocketChannel) key.channel();

        List<byte[]> channelData = keepDataTrack.get(socketChannel);
        Iterator<byte[]> its = channelData.iterator();

        while(its.hasNext()) {
            byte[] it = its.next();
            its.remove();
            socketChannel.write(ByteBuffer.wrap(it));
        }

        key.interestOps(SelectionKey.OP_READ);
    }

    private void doEchoJob(SelectionKey key, byte[] data) {
        SocketChannel socketChannel = (SocketChannel) key.channel();
        List<byte[]> channelData = keepDataTrack.get(socketChannel);
        channelData.add(data);

        key.interestOps(SelectionKey.OP_WRITE);
    }
}
