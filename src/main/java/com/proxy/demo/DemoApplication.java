package com.proxy.demo;

import com.proxy.demo.server.NettyServer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class DemoApplication {

    public static void main(String[] args) throws Exception {

        NettyServer.bind(3333);

        SpringApplication.run(DemoApplication.class, args);
    }

}
