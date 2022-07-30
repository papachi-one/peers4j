package one.papachi.peers.peersserver.app;

import one.papachi.peers.peersserver.data.GroupRole;
import one.papachi.peers.peersserver.messages.GenericMessage;
import one.papachi.peers.peersserver.messages.HelloMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

@RestController
@CrossOrigin("*")
public class AppController {

    @Autowired
    private AppService appService;

    @GetMapping(value = "/events/{peerId}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<String>> events(@PathVariable("peerId") String peerId) {
        UUID uuid = UUID.fromString(peerId);
        Flux<GenericMessage> sseFlux = Flux.create(fluxSink -> {
            appService.connected(uuid, fluxSink);
            fluxSink.next(new HelloMessage());
            fluxSink.onDispose(() -> appService.disconnected(uuid));
        });
        return sseFlux.map(GenericMessage::toJson).map(json -> ServerSentEvent.<String>builder(json).build());
    }

    @PostMapping(value = "/peers/{peerId}/groups/{groupId}")
    public void joinGroup(@PathVariable("peerId") String peerId, @PathVariable("groupId") String groupId) {
        appService.join(UUID.fromString(peerId), UUID.fromString(groupId), GroupRole.PEER);
    }

    @DeleteMapping(value = "/peers/{peerId}/groups/{groupId}")
    public void leaveGroup(@PathVariable("peerId") String peerId, @PathVariable("groupId") String groupId) {
        appService.leave(UUID.fromString(peerId), UUID.fromString(groupId));
    }

    @PutMapping(value = "/peers/{peerIdFrom}/{peerIdTo}")
    public void relayData(@PathVariable("peerIdFrom") String peerIdFrom, @PathVariable("peerIdTo") String peerIdTo, @RequestBody String data) {
        appService.data(UUID.fromString(peerIdFrom), UUID.fromString(peerIdTo), data.getBytes(StandardCharsets.UTF_8));
    }

}
