import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ClientConnection {

    private String name;
    private Socket socket;
    private BufferedReader reader ;
    private PrintWriter writer;


    public ClientConnection(String name, Socket socket){
        this.name = name;
        this.socket = socket;
        try{
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            writer = new PrintWriter(socket.getOutputStream(), true);
        }catch (IOException e){

        }
    }
    public void sendMessage(String message){
        writer.println(message);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Socket getSocket() {
        return socket;
    }

    public void setSocket(Socket socket) {
        this.socket = socket;
    }
}
