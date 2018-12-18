public class ServerMessage
{
    private String line;

    public ServerMessage(final String line) {
        this.line = line;
    }

    public MessageType getMessageType() {
        MessageType result = MessageType.UNKOWN;
        try {
            if (this.line != null && this.line.length() > 0) {
                final String[] splits = this.line.split("\\s+");
                String lineTypePart = splits[0];
                if (lineTypePart.startsWith("-") || lineTypePart.startsWith("+")) {
                    lineTypePart = lineTypePart.substring(1);
                }
                result = MessageType.valueOf(lineTypePart);
            }
        }
        catch (IllegalArgumentException iaex) {
            System.out.println("[ERROR] Unknown command");
        }
        return result;
    }

    public String getPayload() {
        if (this.getMessageType().equals(MessageType.UNKOWN)) {
            return this.line;
        }
        if (this.line == null || this.line.length() < this.getMessageType().name().length() + 1) {
            return "";
        }
        int offset = 0;
        if (this.getMessageType().equals(MessageType.OK) || this.getMessageType().equals(MessageType.ERR)) {
            offset = 1;
        }
        return this.line.substring(this.getMessageType().name().length() + 1 + offset);
    }

    @Override
    public String toString() {
        return this.line;
    }

    public enum MessageType
    {
        HELO,
        BCST,
        PING,
        DSCN,
        OK,
        ERR,
        UNKOWN;
    }
}
