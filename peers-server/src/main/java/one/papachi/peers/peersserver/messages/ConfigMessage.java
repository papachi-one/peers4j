package one.papachi.peers.peersserver.messages;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.nio.ByteBuffer;
import java.util.UUID;

@Data
@AllArgsConstructor
public class ConfigMessage implements GenericMessage {

    public enum Mode {
        JSON, BINARY
    }

    private UUID peerId;

    private Mode mode;

    public ConfigMessage(UUID peerId, JsonNode json) {
        if (!json.get("messageType").textValue().equals("CONFIG"))
            throw new IllegalArgumentException("Incorrect message type. Expected 'CONFIG', found '" + json.get("messageType").textValue() + "'.");
        this.peerId = peerId;
        this.mode = Mode.valueOf(json.get("mode").textValue());
    }

    public ConfigMessage(UUID peerId, byte[] data) {
        if ((data[0] & 0xFF) != 'C')
            throw new IllegalArgumentException("Incorrect message type. Expected 'C', found '" + ((char) data[0] & 0xFF) + "'.");
        this.peerId = peerId;
        this.mode = Mode.values()[data[1] & 0xFF];
    }

    @Override
    public String toJson() {
        return "{\"messageType\":\"CONFIG\",\"mode\":\"" + mode + "\"}";
    }

    @Override
    public byte[] toBinary() {
        ByteBuffer buffer = ByteBuffer.allocate(1 + 1);
        buffer.put((byte) 'C');
        buffer.put((byte) mode.ordinal());
        return buffer.array();
    }
}
