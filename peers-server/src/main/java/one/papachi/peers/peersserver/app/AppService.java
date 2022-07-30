package one.papachi.peers.peersserver.app;

import com.hazelcast.map.IMap;
import com.hazelcast.topic.ITopic;
import com.hazelcast.topic.Message;
import one.papachi.peers.peersserver.data.GroupRole;
import one.papachi.peers.peersserver.data.PeerAddress;
import one.papachi.peers.peersserver.messages.AddressesMessage;
import one.papachi.peers.peersserver.messages.ConfigMessage;
import one.papachi.peers.peersserver.messages.DataMessage;
import one.papachi.peers.peersserver.messages.GenericMessage;
import one.papachi.peers.peersserver.messages.JoinMessage;
import one.papachi.peers.peersserver.messages.LeaveMessage;
import one.papachi.peers.peersserver.messages.PeerMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.FluxSink;

import javax.annotation.PostConstruct;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class AppService {

    @Autowired
    private AppState appState;

    @PostConstruct
    private void init() {
        appState.getMessagesGlobal().addMessageListener(this::onMessage);
        appState.getMessageLocal().addMessageListener(this::onMessage);
    }

    private void onMessage(Message<PeerMessage> message) {
        PeerMessage messageObject = message.getMessageObject();
        switch (messageObject.getType()) {
            case JOIN -> {
                // the message
                JoinMessage joinMessage = (JoinMessage) messageObject;

                // params
                UUID peerId = joinMessage.getPeerId();
                UUID groupId = joinMessage.getGroupId();
                GroupRole groupRole = joinMessage.getGroupRole();

                // app state
                IMap<UUID, Map<UUID, GroupRole>> groupIdToPeers = appState.getGroupIdToPeers();
                Map<UUID, FluxSink<GenericMessage>> sinks = appState.getSinks();

                // peers
                Map<UUID, GroupRole> peerIdToGroupRole = groupIdToPeers.get(groupId);
                List<UUID> peers = peerIdToGroupRole
                        .entrySet()
                        .stream()
                        .map(entry ->
                                switch (groupRole) {
                                    case PEER -> entry.getValue() == GroupRole.PEER ? entry.getKey() : null;
                                    case CLIENT -> entry.getValue() == GroupRole.SERVER ? entry.getKey() : null;
                                    case SERVER -> entry.getValue() == GroupRole.CLIENT ? entry.getKey() : null;
                                }
                        )
                        .filter(Objects::nonNull)
                        .filter(uuid -> !uuid.equals(peerId))
                        .toList();

                // join message to peers
                peers.stream()
                        .map(sinks::get)
                        .filter(Objects::nonNull)
                        .forEach(fluxSink -> fluxSink.next(joinMessage));

                // join messages to peer
                Optional.ofNullable(sinks.get(peerId)).ifPresent(sink -> peers.stream()
                        .map(uuid -> new JoinMessage(uuid, groupId, null))
                        .forEach(msg -> sink.next(msg)));
            }
            case LEAVE -> {
                // the message
                LeaveMessage leaveMessage = (LeaveMessage) messageObject;

                // params
                UUID peerId = leaveMessage.getPeerId();
                UUID groupId = leaveMessage.getGroupId();
                GroupRole groupRole = leaveMessage.getGroupRole();

                // app state
                IMap<UUID, Map<UUID, GroupRole>> groupIdToPeers = appState.getGroupIdToPeers();
                Map<UUID, FluxSink<GenericMessage>> sinks = appState.getSinks();

                // peers
                Map<UUID, GroupRole> peerIdToGroupRole = groupIdToPeers.get(groupId);
                List<UUID> peers = peerIdToGroupRole
                        .entrySet()
                        .stream()
                        .map(entry ->
                                switch (groupRole) {
                                    case PEER -> entry.getValue() == GroupRole.PEER ? entry.getKey() : null;
                                    case CLIENT -> entry.getValue() == GroupRole.SERVER ? entry.getKey() : null;
                                    case SERVER -> entry.getValue() == GroupRole.CLIENT ? entry.getKey() : null;
                                }
                        )
                        .filter(Objects::nonNull)
                        .filter(uuid -> !uuid.equals(peerId))
                        .toList();

                // leave message to peers
                peers.stream()
                        .map(sinks::get)
                        .filter(Objects::nonNull)
                        .forEach(fluxSink -> fluxSink.next(leaveMessage));
            }
            case ADDRESSES -> {
                // the message
                AddressesMessage addressesMessage = (AddressesMessage) messageObject;

                // params
                UUID peerId = addressesMessage.getPeerId();
                List<PeerAddress> addresses = addressesMessage.getAddresses();

                // app state
                IMap<UUID, Map<UUID, GroupRole>> peerIdToGroups = appState.getPeerIdToGroups();
                IMap<UUID, Map<UUID, GroupRole>> groupIdToPeers = appState.getGroupIdToPeers();
                Map<UUID, FluxSink<GenericMessage>> sinks = appState.getSinks();

                // peers
                Map<UUID, GroupRole> groupIdToGroupRole = peerIdToGroups.get(peerId);
                Set<UUID> peers = groupIdToGroupRole.entrySet().stream()
                        .map(entry ->
                                Optional.ofNullable(groupIdToPeers.get(entry.getKey()))
                                        .orElseGet(Collections::emptyMap)
                                        .entrySet()
                                        .stream()
                                        .map(entry2 ->
                                                switch (entry.getValue()) {
                                                    case PEER -> entry2.getValue() == GroupRole.PEER ? entry2.getKey() : null;
                                                    case CLIENT -> entry2.getValue() == GroupRole.SERVER ? entry2.getKey() : null;
                                                    case SERVER -> entry2.getValue() == GroupRole.CLIENT ? entry2.getKey() : null;
                                                }
                                        )
                                        .filter(Objects::nonNull)
                                        .filter(uuid -> !uuid.equals(peerId))
                                        .toList()
                        )
                        .filter(Objects::nonNull)
                        .flatMap(Collection::stream)
                        .filter(uuid -> !uuid.equals(peerId))
                        .collect(Collectors.toSet());

                // send addresses message to peers
                peers.stream()
                        .map(sinks::get)
                        .filter(Objects::nonNull)
                        .forEach(fluxSink -> fluxSink.next(addressesMessage));
            }
            case DATA -> {
                // the message
                DataMessage dataMessage = (DataMessage) messageObject;

                // params
                UUID dstPeerId = dataMessage.getDstPeerId();

                // app state
                Map<UUID, FluxSink<GenericMessage>> sinks = appState.getSinks();

                // send data message to dst peer
                Optional.ofNullable(sinks.get(dstPeerId)).ifPresent(fs -> fs.next(dataMessage));
            }

        }
    }

    public void join(UUID peerId, UUID groupId, GroupRole groupRole) {
        join(new JoinMessage(peerId, groupId, groupRole));
    }

    public void join(JoinMessage message) {
        UUID peerId = message.getPeerId();
        UUID groupId = message.getGroupId();
        GroupRole groupRole = message.getGroupRole();
        IMap<UUID, Map<UUID, GroupRole>> peerIdToGroups = appState.getPeerIdToGroups();
        IMap<UUID, Map<UUID, GroupRole>> groupIdToPeers = appState.getGroupIdToPeers();
        ITopic<PeerMessage> messagesGlobal = appState.getMessagesGlobal();
        peerIdToGroups.executeOnKey(peerId, entry -> {
            Map<UUID, GroupRole> value = Optional.ofNullable(entry.getValue()).orElseGet(HashMap::new);
            value.put(groupId, groupRole);
            entry.setValue(value);
            return value;
        });
        groupIdToPeers.executeOnKey(groupId, entry -> {
            Map<UUID, GroupRole> value = Optional.ofNullable(entry.getValue()).orElseGet(HashMap::new);
            value.put(peerId, groupRole);
            entry.setValue(value);
            return value;
        });
        messagesGlobal.publish(message);
    }

    public void leave(UUID peerId, UUID groupId) {
        leave(new LeaveMessage(peerId, groupId, null));
    }

    public void leave(LeaveMessage message) {
        UUID peerId = message.getPeerId();
        UUID groupId = message.getGroupId();
        IMap<UUID, Map<UUID, GroupRole>> peerIdToGroups = appState.getPeerIdToGroups();
        IMap<UUID, Map<UUID, GroupRole>> groupIdToPeers = appState.getGroupIdToPeers();
        ITopic<PeerMessage> messagesGlobal = appState.getMessagesGlobal();
        peerIdToGroups.executeOnKey(peerId, entry -> {
            Map<UUID, GroupRole> value = Optional.ofNullable(entry.getValue()).orElseGet(HashMap::new);
            message.setGroupRole(value.remove(groupId));
            entry.setValue(value);
            return value;
        });
        groupIdToPeers.executeOnKey(groupId, entry -> {
            Map<UUID, GroupRole> value = Optional.ofNullable(entry.getValue()).orElseGet(HashMap::new);
            value.remove(peerId);
            value = value.isEmpty() ? null : value;
            entry.setValue(value);
            return value;
        });
        messagesGlobal.publish(message);
    }

    public void addresses(UUID peerId, List<PeerAddress> addresses) {
        addresses(new AddressesMessage(peerId, addresses));
    }

    public void addresses(AddressesMessage message) {
        ITopic<PeerMessage> messagesGlobal = appState.getMessagesGlobal();
        messagesGlobal.publish(message);
    }

    public void data(UUID peerId, UUID dstPeerId, byte[] data) {
        data(new DataMessage(peerId, dstPeerId, data));
    }

    public void data(DataMessage message) {
        UUID dstPeerId = message.getDstPeerId();
        IMap<UUID, UUID> peerIdToServer = appState.getPeerIdToServer();
        UUID dstServerId = peerIdToServer.get(dstPeerId);
        ITopic<PeerMessage> messagesRemote = appState.getHazelcastInstance().getReliableTopic(dstServerId.toString());
        messagesRemote.publish(message);
    }

    public void handlePeerMessage(GenericMessage message) {
        if (message instanceof PeerMessage peerMessage) {
            switch (peerMessage.getType()) {
                case JOIN -> join((JoinMessage) peerMessage);
                case LEAVE -> leave((LeaveMessage) peerMessage);
                case ADDRESSES -> addresses((AddressesMessage) peerMessage);
                case DATA -> data((DataMessage) peerMessage);
            }
        } else if (message instanceof ConfigMessage configMessage) {
            UUID peerId = configMessage.getPeerId();
            ConfigMessage.Mode mode = configMessage.getMode();
        }
    }

    public void connected(UUID peerId, FluxSink<GenericMessage> fluxSink) {
        appState.getSinks().put(peerId, fluxSink);
        appState.getPeerIdToServer().put(peerId, appState.getServerId());
    }

    public void disconnected(UUID peerId) {
        Optional.ofNullable(appState.getSinks().remove(peerId)).ifPresent(fluxSink -> fluxSink.complete());
        appState.getModes().remove(peerId);
        appState.getPeerIdToServer().remove(peerId);
        Map<UUID, GroupRole> groupIdToGroupRole = appState.getPeerIdToGroups().get(peerId);
        groupIdToGroupRole.keySet().forEach(groupId -> leave(peerId, groupId));
    }

    public ConfigMessage.Mode getPeerMode(UUID peerId) {
        return appState.getModes().get(peerId);
    }
}
