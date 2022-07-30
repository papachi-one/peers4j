package one.papachi.peers.peersserver.messages;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.web.reactive.socket.WebSocketMessage;

import java.util.UUID;

public interface PeerMessage extends GenericMessage {
    enum Type {
        JOIN, LEAVE, ADDRESSES, DATA
    }

    static GenericMessage getPeerMessage(UUID peerId, WebSocketMessage webSocketMessage) throws JsonProcessingException {
        return switch (webSocketMessage.getType()) {
            case TEXT -> {
                ObjectMapper objectMapper = new ObjectMapper();
                String webSocketTextMessage = webSocketMessage.getPayloadAsText();
                JsonNode json = objectMapper.readTree(webSocketTextMessage);
                String messageType = json.get("messageType").textValue();
                yield switch (messageType) {
                    case "CONFIG" -> new ConfigMessage(peerId, json);
                    case "JOIN" -> new JoinMessage(peerId, json);
                    case "LEAVE" -> new LeaveMessage(peerId, json);
                    case "ADDRESSES" -> new AddressesMessage(peerId, json);
                    case "DATA" -> new DataMessage(peerId, json);
                    default -> throw new IllegalArgumentException("Unexpected value: " + messageType);
                };
            }
            case BINARY -> {
                DataBuffer payload = webSocketMessage.getPayload();
                byte[] data = new byte[payload.readableByteCount()];
                payload.read(data);
                int messageType = data[0] & 0xFF;
                yield switch (messageType) {
                    case 'C' -> new ConfigMessage(peerId, data);
                    case 'J' -> new JoinMessage(peerId, data);
                    case 'L' -> new LeaveMessage(peerId, data);
                    case 'A' -> new AddressesMessage(peerId, data);
                    case 'D' -> new DataMessage(peerId, data);
                    default -> throw new IllegalArgumentException("Unexpected value: " + messageType);
                };
            }
            case PING, PONG -> null;
        };
    }

    UUID getPeerId();

    Type getType();

}
