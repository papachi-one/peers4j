package one.papachi.peers.peersserver.websocket;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.HandlerMapping;
import org.springframework.web.reactive.handler.SimpleUrlHandlerMapping;

import java.util.Map;

@Configuration
public class WebSocketConfiguration {

    @Autowired
    private PeerWebSocketHandler peerWebSocketHandler;

    @Bean
    public HandlerMapping webSocketHandlerMapping() {
        return new SimpleUrlHandlerMapping(Map.of("/ws/events/**", peerWebSocketHandler), 1);
    }

}
