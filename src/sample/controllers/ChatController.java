package sample.controllers;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import sample.ReadWriteNetHandler;
import sample.StartClient;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.ResourceBundle;

public class ChatController implements Initializable{

    private static final String TITLE = "Флудилка";
    public ListView listContacts;

    public interface listenerChatController {
        public void setAuthorisation(boolean authorisation);
    }

    listenerChatController listenerChatController;

    @FXML
    public ImageView imageViewPut;
    public ImageView imageViewEmoji;
    public ImageView imageViewSend;
    public TextField textFieldForSend;
    public VBox vBoxForFieldChat;

    public String getNickName() {
        return nickName;
    }

    private void setNickName(String nickName) {
        this.nickName = nickName;
    }

    private String nickName;

    public ReadWriteNetHandler getReadWriteNetHandler() {
        return readWriteNetHandler;
    }

    public void setReadWriteNetHandler(ReadWriteNetHandler readWriteNetHandler) {
        this.readWriteNetHandler = readWriteNetHandler;
    }

    private ReadWriteNetHandler readWriteNetHandler;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }


    @FXML
    public void onClickedForSend(MouseEvent mouseEvent) {
        sendMyMsg();
    }

    private void sendMyMsg() {
        String msg = textFieldForSend.getText().trim();
        readWriteNetHandler.sendMsg(msg);
        createMessage(true, msg);
    }

    @FXML
    public void onAction(javafx.event.ActionEvent actionEvent){
        sendMyMsg();
    }

    public void getMsg(String msg){
        createMessage(false, msg);
    }


    private void createMessage(boolean isMyMsg, String msg){
        if (!msg.isEmpty()) {
            String sendName;
            if (isMyMsg){
                sendName = "Вы";
            } else {
                String[] words = msg.split("\\s");
                msg = "";
                for (int i = 0; i < words.length - 1; i++) {
                    msg = msg + words[i] + " ";
                }
                System.out.println("createMessage - " + msg );
                sendName = words[words.length - 1];
            }

            Label labelNameAndTime = new Label( sendName + " в " + getCurTime());
            Label labelMes = new Label(msg.trim());
            labelMes.setWrapText(true);
            labelMes.setBackground(new Background(new BackgroundFill(javafx.scene.paint.Color.WHITE, new CornerRadii(10),
                    null)));
            labelMes.setPadding(new Insets(8, 8, 8, 8));
            labelMes.setBorder(new Border(new BorderStroke(javafx.scene.paint.Color.BLACK, BorderStrokeStyle.SOLID, new CornerRadii(10),
                    BorderWidths.DEFAULT)));

            VBox vBoxMsg = new VBox();
            vBoxMsg.getChildren().add(labelNameAndTime);
            vBoxMsg.getChildren().add(labelMes);
            if (isMyMsg){
                vBoxMsg.setAlignment(Pos.TOP_LEFT);
            } else {
                vBoxMsg.setAlignment(Pos.TOP_RIGHT);
            }
            Platform.runLater(() -> {
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

    public void setTitel(String nickName){
        setNickName(nickName);
        Platform.runLater(() -> {
            ((Stage) vBoxForFieldChat.getScene().getWindow()).setTitle(TITLE + " для " + nickName);
        });
    }

}
