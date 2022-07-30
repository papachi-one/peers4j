package one.papachi.peers.peersserver.messages;

public class HelloMessage implements GenericMessage {

    @Override
    public String toJson() {
        return "{\"server\":\"papachi-p2p-srv\"}";
    }

}
