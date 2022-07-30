package one.papachi.peers.peersserver.messages;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.Data;
import one.papachi.peers.peersserver.data.PeerAddress;

import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
public class AddressesMessage implements PeerMessage {

    private UUID peerId;

    private List<PeerAddress> addresses = new ArrayList<>();

    public AddressesMessage(UUID peerId, JsonNode json) {
        if (!json.get("messageType").textValue().equals("ADDRESS_LIST"))
            throw new IllegalArgumentException("Incorrect message type. Expected 'ADDRESS_LIST', found '" + json.get("messageType").textValue() + "'.");
        this.peerId = peerId;
        JsonNode message = json.get("message");
        for (Iterator<JsonNode> it = message.get("addresses").elements(); it.hasNext(); ) {
            JsonNode address = it.next();
            String type = address.get("type").textValue();
            String ip = address.get("ip").textValue();
            int port = address.get("port").intValue();
            try {
                PeerAddress peerAddress = new PeerAddress(PeerAddress.AddressType.valueOf(type), InetAddress.getByName(ip), port);
                this.addresses.add(peerAddress);
            } catch (UnknownHostException e) {
            }
        }
    }

    public AddressesMessage(UUID peerId, byte[] data) {
        if ((data[0] & 0xFF) != 'A')
            throw new IllegalArgumentException("Incorrect message type. Expected 'A', found '" + ((char) data[0] & 0xFF) + "'.");
        this.peerId = peerId;
        ByteBuffer buffer = ByteBuffer.wrap(data, 1, data.length - 1);
        while (buffer.hasRemaining()) {
            PeerAddress.AddressType type = PeerAddress.AddressType.values()[buffer.get() & 0xFF];
            byte[] addr = new byte[16];
            buffer.get(addr);
            int port = buffer.getShort() & 0xFFFF;
            try {
                InetAddress ip = InetAddress.getByAddress(addr);
                if (((Inet6Address) ip).isIPv4CompatibleAddress()) {
                    byte[] address = ip.getAddress();
                    addr = new byte[4];
                    System.arraycopy(address, 12, addr, 0, addr.length);
                    ip = InetAddress.getByAddress(addr);
                }
                PeerAddress peerAddress = new PeerAddress(type, ip, port);
                this.addresses.add(peerAddress);
            } catch (UnknownHostException e) {
            }
        }
    }

    @Override
    public Type getType() {
        return Type.ADDRESSES;
    }

    @Override
    public String toJson() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("{\"messageType\":\"ADDRESSES\",\"peerId\":\"" + peerId + "\",\"addresses\":[");
        boolean first = true;
        for (PeerAddress address : addresses) {
            if (!first)
                stringBuilder.append(",");
            else
                first = false;
            PeerAddress.AddressType type = address.getType();
            String ip = address.getIp().getHostAddress();
            int port = address.getPort();
            stringBuilder.append("{\"type\":\"" + type + "\",\"ip\":\"" + ip + "\",\"port\":\"" + port + "\"}");
        }
        stringBuilder.append("]}");
        return stringBuilder.toString();
    }

    @Override
    public byte[] toBinary() {
        ByteBuffer buffer = ByteBuffer.allocate(1 + 16 + (addresses.size() * (1 + 16 + 2)));
        buffer.put((byte) 'A');
        buffer.putLong(peerId.getMostSignificantBits());
        buffer.putLong(peerId.getLeastSignificantBits());
        for (PeerAddress address : addresses) {
            byte type = (byte) address.getType().ordinal();
            byte[] ip = address.getIp().getAddress();
            if (ip.length == 4) {
                byte[] b = new byte[16];
                b[10] = (byte) 0xFF;
                b[11] = (byte) 0xFF;
                System.arraycopy(ip, 0, b, 12, 4);
                ip = b;
            }
            short port = (short) address.getPort();
            buffer.put(type).put(ip).putShort(port);
        }
        return buffer.array();
    }
}
