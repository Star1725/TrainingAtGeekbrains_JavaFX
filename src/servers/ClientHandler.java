package servers;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketTimeoutException;

public class ClientHandler {
    private static final int TIMEOUT_CLOSE_CONNECT = 15000;
    private Server server;
    private Socket socket;

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public String getLogin() {
        return login;
    }

    private String login;

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
                    socket.setSoTimeout(TIMEOUT_CLOSE_CONNECT);
                    sendMsg("/timeout " + TIMEOUT_CLOSE_CONNECT, null);
                    //аутентификация
                    while (true){
                        System.out.println("Цикл аунтотификации");
                        String data = inputStreamNet.readUTF();
                        System.out.println("Сервер получил данные аунтотификации " + data);
                        if (data.startsWith("/auth")){
                            String[] token = data.split("\\s");
                            if (token.length < 3){
                                continue;
                            }
                            String newNickName = server
                                    .getAuthService()
                                    .getNickNameByLoginAndPassword(token[1], token[2]);
                            login = token[1];
                            if (newNickName != null){
                                if (!server.isAuthenticated(login)){
                                    nickName = newNickName;
                                    sendMsg("/authok ", newNickName);
                                    server.subscribe(this);
                                    System.out.println("Клиент " + nickName + " подключился");
                                    socket.setSoTimeout(0);
                                    break;
                                } else {
                                    sendMsg("/error1 Данная учётная запись уже используется", this.nickName);
                                }
                            } else {
                                sendMsg("/error2 Неверный логин / пароль", this.nickName);
                            }
                        }
                        if (data.startsWith("/reg")){
                            String[] token = data.split("\\s");
                            if (token.length < 4){
                                continue;
                            }
                            boolean b = server.getAuthService().registration(token[1], token[2], token[3]);
                            if (b){
                                sendMsg("/regok Регистрация успешна", null);
                            } else {
                                sendMsg("/regno Регистрация не прошла", null);
                            }
                        }
                    }
                    //работа
                    while (true){
                        System.out.println("Цикл работы");
                        String msg = inputStreamNet.readUTF();

                        if (msg.startsWith("/end")){
                            System.out.println("Сервер получил служебное сообщение /end от " + this.getNickName());
                            outputStreamNet.writeUTF(msg);
                            break;
                        }
                        if (msg.startsWith("/w")){
                            System.out.println("Сервер получил служебное сообщение /w от " + this.getNickName());
                            String[] token = msg.split("\\s", 3);
                            String forNickName = token[1];
                            System.out.println("Сервер получил сообщение для " + forNickName + " от " + nickName + ": " + msg);
                            server.sendPrivatMsg(forNickName, msg, this);
                            continue;
                        }
                        System.out.println("Сервер получил сообщение для всех от " + nickName + ": " + msg);
                        server.broadcastMsg(msg, this);
                    }
                } catch (SocketTimeoutException e){
                    System.out.println(e.getMessage());
                }
                catch (IOException e) {
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

    void sendMsg(String msg, String fromNickName){
        try {
            msg = msg.trim();
            outputStreamNet.writeUTF(String.format("%s %s", msg, fromNickName));
            System.out.println("ClientHandler " + this.getNickName() + " отправил сообщение: \"" + msg + "\" от " + fromNickName + " для " + this.getNickName());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
