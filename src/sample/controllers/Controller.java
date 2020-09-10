package sample.controllers;

import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.ResourceBundle;

public class Controller implements Initializable{

    @FXML
    public ImageView imageViewPut;
    public ImageView imageViewEmoji;
    public ImageView imageViewSend;
    public TextField textFieldForSend;
    public VBox vBoxForFieldChat;

    private String nickName;

    private final int PORT = 8189;
    private final String IP_ADDRESS = "localhost";
    private Socket socket;
    private DataInputStream inputStreamNet;
    private DataOutputStream outputStreamNet;

    @FXML
    private Button loginBtn;
    @FXML
    private PasswordField passTxtFld;
    @FXML
    private TextField loginTxtFld;
    @FXML
    private Button regBtn;


    public void onActionRegBtn(javafx.event.ActionEvent actionEvent) {

    }

    public void onActionLoginBtn(javafx.event.ActionEvent actionEvent) {
//        Node sourse = (Node) actionEvent.getSource();
//        Stage stage = (Stage) sourse.getScene().getWindow();
//        stage.hide();
        if (socket == null || socket.isClosed()){
            connect();
        }
        try {
            System.out.println("Отправляем сереверу " + loginTxtFld.getText().trim().toLowerCase() + " " + passTxtFld.getText().trim());
            outputStreamNet.writeUTF(String.format("/auth %s %s", loginTxtFld.getText().trim().toLowerCase(), passTxtFld.getText().trim()));
            passTxtFld.clear();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        connect();
    }

    private void connect(){
        try {
            socket = new Socket(IP_ADDRESS, PORT);
            inputStreamNet = new DataInputStream(socket.getInputStream());
            outputStreamNet = new DataOutputStream(socket.getOutputStream());
            System.out.println("Client connect to server");
            Thread threadReadMsgFromNet = new Thread(() -> {
                try {
                    //аутентификация
                    while (true){
                        String msg = inputStreamNet.readUTF();
                        if (msg.startsWith("/authok")){
                            nickName = msg.split(" ", 2)[1];
                            System.out.println("Скрыть авторизацию");
                            Platform.runLater(() -> {
                                loginBtn.getScene().getWindow().hide();
                            });
                            //getMsg(msg);
                            break;
                        }
                    }
                    //работа
                    while (true){
                        String msg = inputStreamNet.readUTF();
                        System.out.println("Клиент " + nickName + " получил сообщение " + msg);
                        getMsg(msg);
                        if (msg.equals("/end")){
                            break;
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    System.out.println("Client disconnect from server");
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


    @FXML
    public void onClickedForSend(MouseEvent mouseEvent) {
        sendMsg();
    }
    @FXML
    public void onAction(javafx.event.ActionEvent actionEvent) {
        sendMsg();
    }

    private void sendMsg(){
        try {
            outputStreamNet.writeUTF(textFieldForSend.getText());
        } catch (IOException e) {
            e.printStackTrace();
        }
        createMessage(true, textFieldForSend.getText());
    }

    private void getMsg(String msg){
        createMessage(false, msg);
    }


    private void createMessage(boolean isMyMsg, String msg){
        if (!msg.isEmpty()) {
            System.out.println(msg);
            Label labelNameAndTime = new Label("Имя в " + getCurTime());

            Label labelMes = new Label(msg);
            labelMes.setWrapText(true);
            labelMes.setBackground(new Background(new BackgroundFill(javafx.scene.paint.Color.WHITE, new CornerRadii(10),
                    null)));
            labelMes.setPadding(new Insets(8, 8, 8, 8));
            labelMes.setBorder(new Border(new BorderStroke(javafx.scene.paint.Color.BLACK, BorderStrokeStyle.SOLID, new CornerRadii(10),
                    BorderWidths.DEFAULT)));
            //графический поток

                VBox vBoxMsg = new VBox();
                vBoxMsg.getChildren().add(labelNameAndTime);
                vBoxMsg.getChildren().add(labelMes);
                if (isMyMsg){
                    vBoxMsg.setAlignment(Pos.TOP_LEFT);
                } else {
                    vBoxMsg.setAlignment(Pos.TOP_RIGHT);
                }
                vBoxForFieldChat.getChildren().add(vBoxMsg);
                vBoxForFieldChat.setPadding(new Insets(8, 8, 8, 8));
                vBoxForFieldChat.setSpacing(8);
                textFieldForSend.requestFocus();
                textFieldForSend.clear();
        }
    }

    private String getCurTime() {
        Calendar calendar = Calendar.getInstance();
        DateFormat dateFormat = new SimpleDateFormat("HH:mm");
        return dateFormat.format(calendar.getTime());
    }

}
