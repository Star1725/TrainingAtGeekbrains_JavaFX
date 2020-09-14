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
    public ListView<String> listContacts;
    public AnchorPane anchPaneChatField;
    public SplitPane splitPaneMainWindow;
    private Node paneContacts;

    public AnchorPane getAnchPanelListContacts() {
        return anchPanelListContacts;
    }

    public AnchorPane anchPanelListContacts;

    public void clickListClients(MouseEvent mouseEvent) {
        //различные нажатия на мышь
        //mouseEvent.
        String resevMsg = listContacts.getSelectionModel().getSelectedItem();
        textFieldForSend.setText("/w " + resevMsg + " ");
    }

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
        //скрытие списка контактов
        splitPaneMainWindow.getItems().remove(0);
    }

    @FXML
    public void onAction(javafx.event.ActionEvent actionEvent){
        sendAndCreateMsg();
    }
    @FXML
    public void onClickedForSend(MouseEvent mouseEvent) {
        sendAndCreateMsg();
    }

    private void sendAndCreateMsg() {
        String msg = textFieldForSend.getText().trim();
        readWriteNetHandler.sendMsg(msg);
        createGUIMessageForChat(true, msg);
    }

    public void getMsg(String msg){
        createGUIMessageForChat(false, msg);
    }

    private void createGUIMessageForChat(boolean isMyMsg, String msg){
        if (!msg.isEmpty()) {
            String privateMsgFor = "";
            String sendName;
            if (isMyMsg){//моё сообщение
                sendName = "Вы";
                if (msg.startsWith("/w")){//моё личное сообщение для
                    String[] token = msg.split("\\s");
                    msg ="";
                    for (int i = 2; i < token.length; i++) {
                        msg = msg + token[i] + " ";
                    }
                    privateMsgFor = " (личное для " + token[1] + ")";
                    System.out.println("createMessage" + privateMsgFor + " - " + msg );
                }
            } else if (msg.startsWith("/w")){//личное сообщение из чата
                String[] token = msg.split("\\s");
                msg = "";
                for (int i = 2; i < token.length - 1; i++) {
                    msg = msg + token[i] + " ";
                }
                System.out.println("private createMessage - " + msg );
                sendName = token[token.length - 1];
                privateMsgFor = " (личное)";
            } else {//сообщение из чата
                String[] token = msg.split("\\s");
                msg = "";
                for (int i = 0; i < token.length - 1; i++) {
                    msg = msg + token[i] + " ";
                }
                System.out.println("createMessage - " + msg );
                sendName = token[token.length - 1];
            }

            Label labelNameAndTime = new Label( sendName + " в " + getCurTime() + " " + privateMsgFor);
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

    public void setTitle(String nickName){
        setNickName(nickName);
        Platform.runLater(() -> {
            ((Stage) vBoxForFieldChat.getScene().getWindow()).setTitle(TITLE + " для " + nickName);
            //отображение списка контактов
            if (splitPaneMainWindow.getItems().size() == 1){
                splitPaneMainWindow.getItems().add(0, anchPanelListContacts);
            }
            splitPaneMainWindow.setDividerPosition(0, 0.3);
        });
    }

    public void updatedListViewContacts(String[] token){
        Platform.runLater(() -> {
            listContacts.getItems().clear();
            System.out.println("Очистили список");
            for (int i = 1; i < token.length - 1; i++) {
                if (token[i].equals(nickName)){
                    token[i] = String.format("%s (%s)", "Вы",token[i]);
                }
                listContacts.getItems().add(token[i]);
            }
            System.out.println("Вывели новый список");
        });
    }
}
