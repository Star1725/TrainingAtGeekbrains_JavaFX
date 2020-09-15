package sample;

import javafx.application.Platform;
import javafx.stage.Stage;
import sample.controllers.AuthController;
import sample.controllers.ChatController;
import sample.controllers.RegController;
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

    private ChatController chatController;

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
                        System.out.println("Цикл аутентификации получил от сервера данные: " + data);
                        //установка timeout соединения при неудачной авторизации
                        if(data.startsWith("/timeout_on")){
                            authController.setTimeout(Integer.parseInt(token[1]));
                        } else if (data.startsWith("/authok")){
                            authController.setAuthentication(true);
                            chatController.setTitle(token[1]);
                            break;
                        } else if(data.startsWith("/error1")){
                            System.out.println("Показать окно ошибки \"" + data + "\"");
                            authController.showAlertWindow("Ошибка", token[1]);
                        } else if (data.startsWith("/error2")){
                            System.out.println("Показать окно ошибки \" " + data + "\"");
                            authController.showAlertWindow("Ошибка", token[1]);
                        } else if (data.startsWith("/regok")){
                            System.out.println("Показать окно удачной регистрации \" " + data + "\"");
                            authController.showAlertWindow("Информация", token[1]);
                            Platform.runLater(() -> {
                                authController.getRegStage().hide();
                            });
                        } else if (data.startsWith("/regno")) {
                            System.out.println("Показать окно неудачной регистрации \" " + data + "\"");
                            authController.showAlertWindow("Ошибка", token[1]);
                        }
                    }
                    //работа
                    while (true){
                        System.out.println("Ждем сообщения от сервера");
                        String msg = inputStreamNet.readUTF();

                        if (msg.startsWith("/")){
                            System.out.println("Цикл работы получил служебное сообщение от сервера: " + msg);
                            if (msg.equals("/end")){
                                break;
                            }
                            if (msg.startsWith("/clientlist")){
                                System.out.println(msg);
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
                    authController.showAlertWindow("Ошибка", "Истекло время авторизации на сервере");
                    System.out.println("Exception в цикле приёма сообщений от сервера");
                    //e.printStackTrace();
                } finally {
                    System.out.println("Client " + chatController.getNickName() + " disconnect from server");
                    try {
                        socket.close();
                        Platform.runLater(() -> {
                            authController.setAuthentication(false);
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
            System.out.println("Клиент отправил сообщение: " + msg);
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

    public void tryReg(String login, String password, String nickName){
        String msgReg = String.format("/reg %s %s %s", login, password, nickName);
        try {
            outputStreamNet.writeUTF(msgReg);
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println(msgReg);
    }
}
