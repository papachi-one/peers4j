package one.papachi.peers.peersserver.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.net.InetAddress;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PeerAddress implements Serializable {

    public enum AddressType {
        LOCAL, PUBLIC
    }

    private AddressType type;

    private InetAddress ip;

    private int port;

}
