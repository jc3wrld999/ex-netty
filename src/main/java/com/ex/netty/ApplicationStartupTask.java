package com.ex.netty;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import com.ex.netty.blocking.BlockingServer;
import com.ex.netty.blocking.NonBlockingServer;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ApplicationStartupTask implements ApplicationListener<ApplicationReadyEvent> {
    
    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        System.out.println("[Starting Spring Boot]");
        try {
            // new EchoServer().start();
            new NonBlockingServer().startEchoServer();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
}
