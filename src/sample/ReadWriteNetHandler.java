package sample;

import javafx.application.Platform;
import javafx.stage.Stage;
import sample.controllers.AuthController;
import sample.controllers.ChatController;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ReadWriteNetHandler {
    public Socket getSocket() {
        return socket;
    }

    private Socket socket;
    private final int PORT = 8189;
    private final String IP_ADDRESS = "localhost";
    private DataInputStream inputStreamNet;
    private DataOutputStream outputStreamNet;

    public ChatController getChatController() {
        return chatController;
    }
    public void setChatController(ChatController chatController) {
        chatController = chatController;
    }
    private ChatController chatController;

    public AuthController getAuthController() {
        return authController;
    }
    public void setAuthController(AuthController authController) {
        authController = authController;
    }
    private AuthController authController;

    public ReadWriteNetHandler(ChatController chatController, AuthController authController) {
        this.chatController = chatController;
        this.authController = authController;
    }

    public void connectAndReadChat(){
        try {
            socket = new Socket(IP_ADDRESS, PORT);
            inputStreamNet = new DataInputStream(socket.getInputStream());
            outputStreamNet = new DataOutputStream(socket.getOutputStream());
            System.out.println("Создали поток для приема данных от сервера");
            Thread threadReadMsgFromNet = new Thread(() -> {
                try {
                    //аутентификация
                    while (true){
                        System.out.println("Ждем данные аутентификации от сервера");
                        String data = inputStreamNet.readUTF();
                        String[] token = data.split("\\s", 2);
                        System.out.println("Цикл авторизации получил от сервера данные: " + data);
                        if (data.startsWith("/authok")){
                            authController.setAuthorization(true);
                            chatController.setTitle(data.split(" ", 2)[1]);
                            System.out.println("вышли из цикла авторизации");
                            break;
                        } else if(data.startsWith("/error1")){
                            System.out.println("Показать окно ошибки \" " + data + "\"");
                            authController.showAlertWindow("Ошибка", token[1]);
                        } else if (data.startsWith("/error2")){
                            System.out.println("Показать окно ошибки \" " + data + "\"");
                            authController.showAlertWindow("Ошибка", token[1]);
                        }
                    }
                    //работа
                    while (true){
                        System.out.println("Ждем сообщение от сервера");
                        String msg = inputStreamNet.readUTF();

                        if (msg.startsWith("/")){
                            System.out.println("Цикл работы получил служебное сообщение от сервера: " + msg);
                            if (msg.equals("/end")){
                                break;
                            }
                            if (msg.startsWith("/clientlist")){
                                System.out.println(1);
                                String[] token = msg.split("\\s+");
                                chatController.updatedListViewContacts(token);
                            }
                            if (msg.startsWith("/w")){
                                System.out.println("Клиент " + chatController.getNickName() + " получил личное сообщение " + msg);
                                chatController.getMsg(msg);
                            }
                        } else {
                            System.out.println("Клиент " + chatController.getNickName() + " получил сообщение " + msg);
                            chatController.getMsg(msg);
                        }
                    }
                } catch (IOException e) {
                    System.out.println("Exception в цикле приёма сообщений от сервера");
                    e.printStackTrace();
                } finally {
                    System.out.println("Client " + chatController.getNickName() + " disconnect from server");
                    try {
                        socket.close();
                        Platform.runLater(() -> {
                            authController.setAuthorization(false);
                            ((Stage)authController.loginBtn.getScene().getWindow()).show();
                        });
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

    public void sendMsg(String msg){
        try {
            outputStreamNet.writeUTF(msg);
            System.out.println("Клиент отправил сообщение " + msg);
        } catch (IOException e) {
            System.out.println("Exception в методе отправки сообщений серверу");
            e.printStackTrace();
        }
    }

    public void tryAuth(String log, String pas) {
        if (socket != null || socket.isClosed()){
            System.out.println(socket.toString());
            try {
                System.out.println("Отправляем сереверу логин: " + log + " и пароль: " + pas);
                outputStreamNet.writeUTF(String.format("/auth %s %s", log, pas));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
