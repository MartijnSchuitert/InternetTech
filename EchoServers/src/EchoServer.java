import java.io.*;
import java.net.*;
import java.lang.Thread;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


public class EchoServer {

    private static ConcurrentHashMap<String, Socket> clients = new ConcurrentHashMap<String, Socket>();
    private static ArrayList<ClientConnection> clientConnections = new ArrayList<>();
    public String sender;

    public static void main(String[] args) {
        try {
            ServerSocket server = new ServerSocket(1337);
            while (true) {
                Socket client = server.accept();
                EchoHandler handler = new EchoHandler(client);
                handler.start();
            }
        } catch (Exception e) {
            System.err.println("Exception caught:" + e);
        }
    }

    public static ConcurrentHashMap<String, Socket> getClients() {
        return clients;
    }

    public static void setClients(ConcurrentHashMap<String, Socket> clients) {
        EchoServer.clients = clients;
    }

    public static ArrayList<ClientConnection> getClientConnections() {
        return clientConnections;
    }

    public static void setClientConnections(ArrayList<ClientConnection> clientConnections) {
        EchoServer.clientConnections = clientConnections;
    }
}

class EchoHandler extends Thread {
    Socket client;
    String sender;
    EchoHandler(Socket client) {
        this.client = client;
    }

    public void run() {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(client.getInputStream()));
            PrintWriter writer = new PrintWriter(client.getOutputStream(), true);
            writer.println("HELO Welcome to the server");
            writer.flush();

            while (true) {
                String line = reader.readLine();
                String name = Base64.getEncoder().encodeToString(MessageDigest.getInstance("MD5").digest(line.trim().getBytes()));
                writer.println("+OK " + name);
                name = line.split("HELO ")[1];
                writer.flush();
                EchoServer.getClients().put(name, client);
                EchoServer.getClientConnections().add(new ClientConnection(name, client));
                while (true){
                    line = reader.readLine();
                    System.out.println(line);
                    if (line.startsWith("BCST")){
                        System.out.println("CLients: " + EchoServer.getClients().size());
                        for (int i = 0; i < EchoServer.getClientConnections().size(); i++) {
                            EchoServer.getClientConnections().get(i).sendMessage(name + " " + line);
                        }
                    }
                    if (line.startsWith("DM")){
                        String username = line.split(": ")[1].trim();
                        String message = line.split(": ")[2].trim();
                        for (int i = 0; i < EchoServer.getClientConnections().size(); i++) {
                            if (EchoServer.getClientConnections().get(i).getName().equals(username)){
                                EchoServer.getClientConnections().get(i).sendMessage("Message from " + name + ": " +message);
                            }
                        }
                    }
                    if (line.equals("quit")){
                        for (int i = 0; i < EchoServer.getClientConnections().size(); i++) {
                            if (EchoServer.getClientConnections().get(i).getSocket() == client){
                                EchoServer.getClientConnections().remove(i);
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Exception caught: client disconnected.");
        } finally {
            try {
                client.close();
            } catch (Exception e) {
                ;
            }
        }
    }
}
