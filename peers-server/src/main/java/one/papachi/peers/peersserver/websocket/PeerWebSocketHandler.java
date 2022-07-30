package one.papachi.peers.peersserver.websocket;

import com.fasterxml.jackson.core.JsonProcessingException;
import one.papachi.peers.peersserver.app.AppService;
import one.papachi.peers.peersserver.messages.ConfigMessage;
import one.papachi.peers.peersserver.messages.GenericMessage;
import one.papachi.peers.peersserver.messages.HelloMessage;
import one.papachi.peers.peersserver.messages.PeerMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;
import org.springframework.web.util.UriTemplate;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Component
public class PeerWebSocketHandler implements WebSocketHandler {

    private static final UriTemplate pathTemplate = new UriTemplate("/ws/events/{peerId}");

    private static UUID getPeerId(WebSocketSession session) {
        String path = session.getHandshakeInfo().getUri().getPath();
        Map<String, String> map = pathTemplate.match(path);
        UUID peerId = Optional.ofNullable(map.get("peerId")).map(UUID::fromString).orElse(null);
        return peerId;
    }

    @Autowired
    private AppService appService;

    @Override
    public Mono<Void> handle(WebSocketSession session) {
        UUID peerId = getPeerId(session);
        if (peerId == null)
            return session.close();
        Flux<GenericMessage> flux = Flux.create(fluxSink -> {
            appService.connected(peerId, fluxSink);
            fluxSink.next(new HelloMessage());
            fluxSink.onDispose(() -> appService.disconnected(peerId));
        });
        Mono<Void> input = session.receive().doOnNext(webSocketMessage -> handleIncomingMessage(peerId, webSocketMessage)).then();
        Mono<Void> output = session.send(flux.map(message ->
                Optional.ofNullable(appService.getPeerMode(peerId)).orElse(ConfigMessage.Mode.JSON) == ConfigMessage.Mode.JSON
                        ? session.textMessage(message.toJson()) : session.binaryMessage(factory -> factory.wrap(message.toBinary()))
        )).then();
        Mono<Void> mono = Mono.zip(input, output).doFinally(signalType -> {
            appService.disconnected(peerId);
        }).then();
        return mono;
    }

    private void handleIncomingMessage(UUID peerId, WebSocketMessage webSocketMessage) {
        try {
            GenericMessage peerMessage = PeerMessage.getPeerMessage(peerId, webSocketMessage);
            appService.handlePeerMessage(peerMessage);
        } catch (JsonProcessingException e) {
        }
    }

}
