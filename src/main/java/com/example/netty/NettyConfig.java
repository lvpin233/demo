package com.example.netty;

import org.springframework.stereotype.Component;

/**
 * Created by Lvpin on 2019/3/5.
 */
@Component
public class NettyConfig {
    private int port;

    public int getPort() {
        return 8083;
    }

    public void setPort(int port) {
        this.port = port;
    }
}
