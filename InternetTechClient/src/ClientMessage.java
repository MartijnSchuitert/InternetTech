public class ClientMessage {

    private String line;
    private MessageType type;

    public ClientMessage(final MessageType type, final String line ){
        this.line = line;
        this.type = type;
    }

    @Override
    public String toString() {
        return this.type + " " + this.line;
    }

    public enum MessageType{
        HELO,
        BCST,
        PONG,
        QUIT,
        DM;
    }
}
