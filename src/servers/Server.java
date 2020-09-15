package servers;

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

    public void broadcastMsg(String msgFromNickName, ClientHandler clientHandler){
        for (ClientHandler client : clients) {
            if (!client.equals(clientHandler)){
                client.sendMsg(msgFromNickName);
            }
        }
    }

    public void sendPrivatMsg(String forNickName, String msg, ClientHandler clientHandler) {
        for (ClientHandler client : clients) {
            if (client.getNickName().equals(forNickName)){
                client.sendMsg(msg);
            }
        }
    }

    public void subscribe(ClientHandler clientHandler){
        clients.add(clientHandler);
        broadcastListClients();
    }

    public void unsubscribe(ClientHandler clientHandler){
        clients.remove(clientHandler);
        broadcastListClients();
    }

    public boolean isAuthenticated(String log){
        for (ClientHandler client : clients) {
            if (client.getLogin().equals(log)){
                return true;
            }
        }
        return false;
    }

    private void broadcastListClients(){
        StringBuilder sb = new StringBuilder("/clientlist ");
        for (ClientHandler client : clients) {
            sb.append(client.getNickName()).append(" ");
        }
        String msg = sb.toString();
        for (ClientHandler client : clients) {
            client.sendMsg(msg);
        }
    }
}
