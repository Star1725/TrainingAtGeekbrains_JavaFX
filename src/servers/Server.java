package servers;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.Vector;

public class Server {
    private List<ClientHandler> clients;
    private AuthServiсe authServiсe;

    private int PORT = 8189;

    public Server() {
        clients = new Vector<>();
        authServiсe = new SimpleAuthServiсe();

        try (ServerSocket serverSocket = new ServerSocket(PORT)){
            System.out.println("Server start");
            while (true){
                Socket socket = serverSocket.accept();
                System.out.println("connect client: " + socket.getRemoteSocketAddress());
                new ClientHandler(this, socket);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public AuthServiсe getAuthService() {
        return authServiсe;
    }

    public void broadcastMsg(String msg, ClientHandler clientHandler){
        for (ClientHandler client : clients) {
            if (!client.equals(clientHandler)){
                client.sendMsg(msg, clientHandler.getNickName());
            }
        }
    }

    public void sendPrivetMsg(String forNickName, String msg, ClientHandler clientHandler) {
        for (ClientHandler client : clients) {
            if (client.getNickName().equals(forNickName)){
                client.sendMsg(msg, clientHandler.getNickName());
            }
        }
    }

    public void subscribe(ClientHandler clientHandler){
        clients.add(clientHandler);
    }

    public void unsubscribe(ClientHandler clientHandler){
        clients.remove(clientHandler);
    }

    public boolean isAuthenticated(String log){
        for (ClientHandler client : clients) {
            if (client.getLogin().equals(log)){
                return true;
            }
        }
        return false;
    }



}
