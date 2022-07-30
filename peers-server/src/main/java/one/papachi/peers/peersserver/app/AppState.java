package one.papachi.peers.peersserver.app;

import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;
import com.hazelcast.topic.ITopic;
import lombok.Getter;
import one.papachi.peers.peersserver.data.GroupRole;
import one.papachi.peers.peersserver.messages.ConfigMessage;
import one.papachi.peers.peersserver.messages.GenericMessage;
import one.papachi.peers.peersserver.messages.PeerMessage;
import org.springframework.stereotype.Component;
import reactor.core.publisher.FluxSink;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Component
@Getter
public class AppState {

    private final HazelcastInstance hazelcastInstance = Hazelcast.newHazelcastInstance();
    private final UUID serverId = hazelcastInstance.getCluster().getLocalMember().getUuid();
    private final ITopic<PeerMessage> messagesGlobal = hazelcastInstance.getReliableTopic("global");
    private final ITopic<PeerMessage> messageLocal = hazelcastInstance.getReliableTopic(serverId.toString());
    private final IMap<UUID, Map<UUID, GroupRole>> peerIdToGroups = hazelcastInstance.getMap("peers");
    private final IMap<UUID, Map<UUID, GroupRole>> groupIdToPeers = hazelcastInstance.getMap("groups");
    private final IMap<UUID, UUID> peerIdToServer = hazelcastInstance.getMap("servers");
    private final Map<UUID, ConfigMessage.Mode> modes = Collections.synchronizedMap(new HashMap<>());
    private final Map<UUID, FluxSink<GenericMessage>> sinks = Collections.synchronizedMap(new HashMap<>());

}
