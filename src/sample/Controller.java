package sample;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.URL;
import java.net.UnknownHostException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.ResourceBundle;

public class Controller implements Initializable {

    @FXML
    public ImageView imageViewPut;
    public ImageView imageViewEmoji;
    public ImageView imageViewSend;
    public TextField textFieldForSend;
    public VBox vBoxForFieldChat;

    private final int PORT = 8189;
    private final String IP_ADDRESS = "localhost";
    private DataInputStream inputStreamNet;
    private DataOutputStream outputStreamNet;




    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            Socket socket = new Socket(IP_ADDRESS, PORT);
            inputStreamNet = new DataInputStream(socket.getInputStream());
            outputStreamNet = new DataOutputStream(socket.getOutputStream());
            System.out.println("Client connect to server");
            Thread threadReadMsgFromNet = new Thread(() -> {
                try {
                    while (true){
                        String msg = inputStreamNet.readUTF();
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
            Calendar calendar = Calendar.getInstance();
            int hour = calendar.get(Calendar.HOUR);
            int min = calendar.get(Calendar.MINUTE);
            Label labelNameAndTime = new Label("Имя в " + getCurTime());

            Label labelMes = new Label(msg);
            labelMes.setWrapText(true);
            labelMes.setBackground(new Background(new BackgroundFill(javafx.scene.paint.Color.WHITE, new CornerRadii(10),
                    null)));
            labelMes.setPadding(new Insets(8, 8, 8, 8));
            labelMes.setBorder(new Border(new BorderStroke(javafx.scene.paint.Color.BLACK, BorderStrokeStyle.SOLID, new CornerRadii(10),
                    BorderWidths.DEFAULT)));
            //графический поток

            Platform.runLater(() -> {
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
            });
        }
    }

    private String getCurTime() {
        Calendar calendar = Calendar.getInstance();
        DateFormat dateFormat = new SimpleDateFormat("HH:mm");
        return dateFormat.format(calendar.getTime());
    }

}
