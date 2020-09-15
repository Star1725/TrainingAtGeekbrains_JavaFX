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
                    //аутентификация
                    while (true){
                        System.out.println("Цикл аунтетификации");
                        String data = inputStreamNet.readUTF();
                        System.out.println("Сервер получил данные аунтотификации " + data);
                        if (data.startsWith("/auth")){
                            System.out.println("Установка времени timeout");
                            socket.setSoTimeout(TIMEOUT_CLOSE_CONNECT);
                            sendMsg(String.format("%s %s", "/timeout_on", TIMEOUT_CLOSE_CONNECT));
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
                                    sendMsg(String.format("%s %s", "/authok", newNickName));
                                    server.subscribe(this);
                                    System.out.println("Клиент " + nickName + " подключился");
                                    socket.setSoTimeout(0);
                                    break;
                                } else {
                                    sendMsg("/error1 Данная учётная запись уже используется");
                                }
                            } else {
                                sendMsg("/error2 Неверный логин / пароль");
                            }
                        }
                        if (data.startsWith("/reg")){
                            String[] token = data.split("\\s");
                            if (token.length < 4){
                                continue;
                            }
                            boolean b = server.getAuthService().registration(token[1], token[2], token[3]);
                            if (b){
                                sendMsg("/regok Регистрация успешна");
                            } else {
                                sendMsg("/regno Регистрация не прошла");
                            }
                        }
                        if (data.startsWith("/timeout_off")){
                            System.out.println("сброс времени timeout");
                            socket.setSoTimeout(0);
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
                            System.out.println("Сервер получил служебное сообщение \"" + msg + "\" от " + this.getNickName());
                            String[] token = msg.split("\\s", 3);
                            String forNickName = token[1];
                            String fromNickName = this.nickName;
                            msg = String.format("%s %s %s %s", token[0], forNickName, fromNickName, token[2]);
                            System.out.println("Сервер получил сообщение для " + forNickName + " от " + fromNickName + ": " + msg);
                            server.sendPrivatMsg(forNickName, msg, this);
                            continue;
                        }
                        System.out.println("Сервер получил сообщение для всех от " + nickName + ": " + msg);
                        server.broadcastMsg(String.format("%s %s", nickName, msg), this);//добавляем перед msg nickname, чтобы все знали от кого сообщение
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

    void sendMsg(String msg){
        try {
            msg = msg.trim();
            outputStreamNet.writeUTF(String.format("%s", msg));
            System.out.println("ClientHandler " + this.getNickName() + " отправил сообщение: \"" + msg + "\"");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
