package servers;

import javafx.stage.Stage;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ClientHandler {
    private Server server;
    private Socket socket;
    private String nickName;
    DataInputStream inputStreamNet;
    DataOutputStream outputStreamNet;

    public ClientHandler(Server server, Socket socket) {
        try {
            this.server = server;
            this.socket = socket;
            inputStreamNet = new DataInputStream(socket.getInputStream());
            outputStreamNet = new DataOutputStream(socket.getOutputStream());
            System.out.println("Start Thread ClientHandler");
            Thread threadReadMsgFromNet = new Thread(() -> {
                try {
                    //аутентификация
                    while (true){
                        String msg = inputStreamNet.readUTF();
                        System.out.println("Сервер получил данные " + msg);
                        if (msg.startsWith("/auth")){
                            String[] token = msg.split("\\s");
                            String newNickName = server
                                    .getAuthServiсe()
                                    .getNickNameByLoginAndPassword(token[1], token[2]);
                            if (newNickName != null){
                                nickName = newNickName;
                                sendMsg("/authok " + nickName);
                                server.subscribe(this);
                                System.out.println("Клиент " + nickName + " подключился");
                                break;
                            } else {
                                sendMsg("Неверный логин / пароль");
                            }
                        }
                    }
                    //работа
                    while (true){
                        String msg = inputStreamNet.readUTF();
                        if (msg.equals("/end")){
                            outputStreamNet.writeUTF(msg);
                            break;
                        }
                        server.broadcastMsg(msg);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    System.out.println("disconnect client: " + socket.getRemoteSocketAddress());
                    server.unsubscribe(this);
                    try {
                        socket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
            threadReadMsgFromNet.setDaemon(true);
            threadReadMsgFromNet.start();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    void sendMsg(String msg){
        try {
            outputStreamNet.writeUTF(msg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
