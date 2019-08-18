package com.transaction.core.config;

import com.transaction.core.ws.WebSocketClient;
import com.transaction.core.ws.WebSocketService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Resource;

@Configuration
@Slf4j
public class WebSocketConfig {

    /**
     * 启动zt交易所用的webSocket客户端
     * @param webSocketService
     * @return
     */
    @Bean(name = "ztWebSocketClient")
    @Resource(name = "ztWebSocketService")
    public WebSocketClient webSocketClient(WebSocketService webSocketService) {
        return WebSocketClient.builder().webSocketService(webSocketService).build();
    }


    @Bean(name = "ztWebSocketClientCNT")
    @Resource(name = "ztWebSocketServiceCNT")
    public WebSocketClient webSocketClientCNT(WebSocketService webSocketService) {
        return WebSocketClient.builder().webSocketService(webSocketService).build();
    }

}
