import com.sun.deploy.config.ClientConfig;
import com.sun.security.ntlm.Server;
import sun.util.cldr.CLDRLocaleDataMetaInfo;

import java.io.*;
import java.net.Socket;
import java.nio.Buffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Scanner;
import java.util.Stack;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class Client {

    private Socket socket;
    MessageReader messageReader;
    MessageWriter writerThread;
    private boolean isConnected;
    NonblockingBufferedReader nonblockReader;

    private Stack<ServerMessage> serverMessages;
    private Stack<ClientMessage> clientMessages;

    public Client() {
        this.isConnected = false;
        this.clientMessages = new Stack<ClientMessage>();
        this.serverMessages = new Stack<ServerMessage>();
    }

    public void connect() {
        try {
            this.socket = new Socket("127.0.0.1", 1337);
            final InputStream input = this.socket.getInputStream();
            final BufferedReader reader = new BufferedReader(new InputStreamReader(input));
            this.messageReader = new MessageReader(reader);
            new Thread(new MessageReader(reader)).start();
            final OutputStream output = this.socket.getOutputStream();
            final PrintWriter writer = new PrintWriter(output);
            this.writerThread = new MessageWriter(writer);
            new Thread(this.writerThread).start();
            while (this.serverMessages.empty()) {
            }
            final ServerMessage serverMessage = this.serverMessages.pop();
            if (!serverMessage.getMessageType().equals(ServerMessage.MessageType.HELO)) {
                System.out.println("Expecting HELO but received: " + serverMessage.toString());
                this.disconnect();
            }
            System.out.println("Please fill in your username");
            final Scanner scanner = new Scanner(System.in);
            final String username = scanner.nextLine();
            final ClientMessage logInMessage = new ClientMessage(ClientMessage.MessageType.HELO, username);
            this.clientMessages.push(logInMessage);
            while (this.serverMessages.empty()) {
            }
            if (!(this.isConnected = this.validateServerMessage(logInMessage, this.serverMessages.pop()))) {
                System.out.println("error logging into server");
            } else {
                System.out.println("successfully connected to server.");
                isConnected = true;
                while (isConnected) {
                    System.out.println("-----------------------------------------------------------------------------------------------------------------");
                    System.out.println("what do you want to do");
                    System.out.println("-----------------------------------------------------------------------------------------------------------------");
                    System.out.println("to broadcast type: BCST -your message-");
                    System.out.println("to send a direct message to a person type: DM -the person you want to message with :- -your message-");
                    System.out.println("you have got to type it with : between the subjects");
                    System.out.println("example - DM : name : message");
                    String payload = scanner.nextLine();
                    if (payload.startsWith("DM")){
                        ClientMessage directMessage = new ClientMessage(ClientMessage.MessageType.DM, payload);
                        this.clientMessages.push(directMessage);
                    }else if (payload.startsWith("BCST")) {
                        ClientMessage broadcastMessage = new ClientMessage(ClientMessage.MessageType.BCST, payload);
                        this.clientMessages.push(broadcastMessage);
                    }else if (payload.equals("quit")){
                        writer.println(payload);
                        writer.flush();
                        disconnect();
                    }

                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public class MessageReader implements Runnable {
        private volatile boolean running;
        private BufferedReader reader;

        public MessageReader(final BufferedReader reader) {
            this.running = true;
            this.reader = reader;
        }

        @Override
        public void run() {
            while (this.running) {
                try {
                    final String line = reader.readLine();
                    final ServerMessage message = new ServerMessage(line);
                    Client.this.serverMessages.push(message);
                    System.out.println(">> "+ line);
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }

        public void kill() {
            this.running = false;
        }
    }

    public class MessageWriter implements Runnable {
        private volatile boolean running;
        PrintWriter writer;

        public MessageWriter(PrintWriter writer) {
            this.writer = writer;
            running = true;
        }

        @Override
        public void run() {
            while (this.running) {
                if (!Client.this.clientMessages.isEmpty()) {
                    this.writeToServer(Client.this.clientMessages.pop(), this.writer);
                }
            }
        }

        private void writeToServer(final ClientMessage message, final PrintWriter writer) {
            final String line = message.toString();
            writer.println(line);
            writer.flush();
        }

        public void kill() {
            this.running = false;
        }
    }

    private void disconnect() {
        if (this.messageReader != null) {
            this.messageReader.kill();
        }
        if (this.writerThread != null) {
            this.writerThread.kill();
        }
        if (this.nonblockReader != null) {
            this.nonblockReader.close();
        }
        this.isConnected = false;

    }

    private boolean validateServerMessage(final ClientMessage clientMessage, final ServerMessage serverMessage) {
        boolean isValid = false;
        try {
            final byte[] hash = MessageDigest.getInstance("MD5").digest(clientMessage.toString().getBytes());
            final String encodedHash = new String(Base64.getEncoder().encode(hash));
            if (serverMessage.getMessageType().equals(ServerMessage.MessageType.OK) && encodedHash.equals(serverMessage.getPayload())) {
                isValid = true;
            }
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return isValid;
    }

    private String readServerMessage(BufferedReader reader) {
        String line = null;
        try {
            line = reader.readLine();

            System.out.println("<< " + line);

        } catch (IOException e1) {
            e1.printStackTrace();
            System.out.println("Error reading buffer: " + e1.getMessage());
        }


        return line;
    }
}
