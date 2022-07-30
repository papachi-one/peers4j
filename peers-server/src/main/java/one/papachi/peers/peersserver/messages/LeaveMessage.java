package one.papachi.peers.peersserver.messages;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.Data;
import one.papachi.peers.peersserver.data.GroupRole;

import java.nio.ByteBuffer;
import java.util.UUID;

@Data
@AllArgsConstructor
public class LeaveMessage implements PeerMessage {

    private UUID peerId;

    private UUID groupId;

    private GroupRole groupRole;

    @Override
    public Type getType() {
        return Type.LEAVE;
    }

    public LeaveMessage(UUID peerId, JsonNode json) {
        if (!json.get("messageType").textValue().equals("LEAVE"))
            throw new IllegalArgumentException("Incorrect message type. Expected 'LEAVE', found '" + json.get("messageType").textValue() + "'.");
        this.peerId = peerId;
        this.groupId = UUID.fromString(json.get("groupId").textValue());
    }

    public LeaveMessage(UUID peerId, byte[] data) {
        if ((data[0] & 0xFF) != 'L')
            throw new IllegalArgumentException("Incorrect message type. Expected 'L', found '" + ((char) data[0] & 0xFF) + "'.");
        this.peerId = peerId;
        ByteBuffer buffer = ByteBuffer.wrap(data, 1, data.length - 1);
        this.groupId = new UUID(buffer.getLong(), buffer.getLong());
    }

    @Override
    public String toJson() {
        return "{\"messageType\":\"LEAVE\",\"peerId\":\"" + peerId + "\",\"groupId\":\"" + groupId + "\"}";
    }

    @Override
    public byte[] toBinary() {
        ByteBuffer buffer = ByteBuffer.allocate(1 + 16 + 16);
        buffer.put((byte) 'L');
        buffer.putLong(peerId.getMostSignificantBits());
        buffer.putLong(peerId.getLeastSignificantBits());
        buffer.putLong(groupId.getMostSignificantBits());
        buffer.putLong(groupId.getLeastSignificantBits());
        return buffer.array();
    }

}
