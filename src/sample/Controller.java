package sample;

import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

import static java.awt.Color.GRAY;

public class Controller {


    @FXML
    public ImageView imageViewPut;
    public ImageView imageViewEmoji;
    public ImageView imageViewSend;
    public TextField textFieldForSend;
    public VBox vBoxForFieldChat;

    @FXML
    public void onClickedForSend(MouseEvent mouseEvent) {
        getTextMessage();
    }
    @FXML
    public void onAction(javafx.event.ActionEvent actionEvent) {
        getTextMessage();
    }

    private void getTextMessage(){
        if (!(textFieldForSend.getText().isEmpty())) {
            Label label = new Label("" + textFieldForSend.getText());
            label.setWrapText(true);
            label.setBackground(new Background(new BackgroundFill(javafx.scene.paint.Color.WHITE, new CornerRadii(10),
                    null)));
            label.setPadding(new Insets(8, 8, 8, 8));
            label.setAlignment(Pos.TOP_LEFT);
            label.setBorder(new Border(new BorderStroke(javafx.scene.paint.Color.BLACK,
                    BorderStrokeStyle.SOLID, new CornerRadii(10), BorderWidths.DEFAULT)));
            vBoxForFieldChat.getChildren().add(label);
            vBoxForFieldChat.setPadding(new Insets(8, 8, 8, 8));
            vBoxForFieldChat.setSpacing(8);
            vBoxForFieldChat.setAlignment(Pos.TOP_LEFT);
            textFieldForSend.requestFocus();
            textFieldForSend.clear();
        }
    }
}
