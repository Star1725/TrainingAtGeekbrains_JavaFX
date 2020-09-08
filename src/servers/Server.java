package servers;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.Vector;

public class Server {
    List<ClientHandler> clients;

    private int PORT = 8189;

    public Server() {
        clients = new Vector<>();
        try (ServerSocket serverSocket = new ServerSocket(PORT)){

            System.out.println("Server start");
            while (true){
                Socket socket = serverSocket.accept();
                System.out.println("Client connect");
                clients.add(new ClientHandler(this, socket));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public void broadcastMsg(String msg){
        for (ClientHandler client : clients) {
            client.sendMsg(msg);
        }
    }
}
