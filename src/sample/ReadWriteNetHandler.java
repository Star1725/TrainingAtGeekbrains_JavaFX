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
                        System.out.println("Цикл авторизации получил от сервера данные: " + data);
                        if (data.startsWith("/authok")){
                            authController.setAuthorization(true);
                            chatController.setTitel(data.split(" ", 2)[1]);
                            System.out.println("вышли из цикла авторизации");
                            break;
                        } else {
                            System.out.println("Показать окно ошибки");
                            authController.showAlertWindow("Ошибка", data);
                        }
                    }
                    //работа
                    while (true){
                        System.out.println("Ждем сообщение от сервера");
                        String msg = inputStreamNet.readUTF();
                        System.out.println("Цикл работы получил сообщение от сервера: " + msg);
                        System.out.println("Клиент " + chatController.getNickName() + " получил сообщение " + msg);
                        if (msg.equals("/end")){
                            break;
                        }
                        chatController.getMsg(msg);
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
                            ((Stage)authController.loginBtn.getScene().getWindow()).showAndWait();
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
