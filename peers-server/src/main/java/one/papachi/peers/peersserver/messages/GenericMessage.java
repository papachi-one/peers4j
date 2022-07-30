package one.papachi.peers.peersserver.messages;

import java.io.Serializable;

public interface GenericMessage extends Serializable {

    default String toJson() {
        return "{}";
    }

    default byte[] toBinary() {
        return new byte[0];
    }

}
