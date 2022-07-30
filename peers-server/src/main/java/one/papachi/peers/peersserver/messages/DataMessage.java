package one.papachi.peers.peersserver.messages;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.nio.ByteBuffer;
import java.util.Base64;
import java.util.UUID;

@Data
@AllArgsConstructor
public class DataMessage implements PeerMessage {

    private UUID peerId;

    private UUID dstPeerId;

    byte[] data;

    public DataMessage(UUID peerId, JsonNode json) {
        if (!json.get("messageType").textValue().equals("DATA"))
            throw new IllegalArgumentException("Incorrect message type. Expected 'DATA', found '" + json.get("messageType").textValue() + "'.");
        this.peerId = peerId;
        this.dstPeerId = UUID.fromString(json.get("peerId").textValue());
        this.data = Base64.getDecoder().decode(json.get("data").textValue());
    }

    public DataMessage(UUID peerId, byte[] data) {
        if ((data[0] & 0xFF) != 'D')
            throw new IllegalArgumentException("Incorrect message type. Expected 'D', found '" + ((char) data[0] & 0xFF) + "'.");
        this.peerId = peerId;
        ByteBuffer buffer = ByteBuffer.wrap(data, 1, data.length - 1);
        this.dstPeerId = new UUID(buffer.getLong(), buffer.getLong());
        this.data = new byte[data.length - 1];
        buffer.get(this.data);
    }

    @Override
    public Type getType() {
        return Type.DATA;
    }

    @Override
    public String toJson() {
        return "{\"messageType\":\"DATA\",\"peerId\":\"" + peerId + "\",\"data\":\"" + Base64.getEncoder().encodeToString(data) + "\"}";
    }

    public byte[] toBinary() {
        ByteBuffer buffer = ByteBuffer.allocate(1 + 16 + data.length);
        buffer.put((byte) 'D');
        buffer.putLong(peerId.getMostSignificantBits());
        buffer.putLong(peerId.getLeastSignificantBits());
        buffer.put(data);
        return buffer.array();
    }
}
