package sample;

import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import java.util.Calendar;

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

            Calendar calendar = Calendar.getInstance();
            int hour = calendar.get(Calendar.HOUR);
            int min = calendar.get(Calendar.MINUTE);
            Label labelNameAndTime = new Label("Имя в " + hour + ":" + min);

            Label labelMes = new Label("" + textFieldForSend.getText());
            labelMes.setWrapText(true);
            labelMes.setBackground(new Background(new BackgroundFill(javafx.scene.paint.Color.WHITE, new CornerRadii(10),
                    null)));
            labelMes.setPadding(new Insets(8, 8, 8, 8));
            labelMes.setBorder(new Border(new BorderStroke(javafx.scene.paint.Color.BLACK, BorderStrokeStyle.SOLID, new CornerRadii(10),
                    BorderWidths.DEFAULT)));

            VBox vBox = new VBox();

            vBox.getChildren().add(labelNameAndTime);
            vBox.getChildren().add(labelMes);
            vBox.setAlignment(Pos.TOP_LEFT);

            vBoxForFieldChat.getChildren().add(vBox);
            vBoxForFieldChat.setPadding(new Insets(8, 8, 8, 8));
            vBoxForFieldChat.setSpacing(8);
            textFieldForSend.requestFocus();
            textFieldForSend.clear();
        }
    }
}
