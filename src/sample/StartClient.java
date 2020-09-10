package sample;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import sample.controllers.Controller;

import java.io.IOException;

public class StartClient extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{

        Parent mainRoot = FXMLLoader.load(getClass().getResource("windows/mainWindowChat.fxml"));

        primaryStage.setTitle("Hello World");
        primaryStage.setScene(new Scene(mainRoot, 800, 600));
        primaryStage.show();


        Stage modalStage = createModalAutWindow(primaryStage);
        modalStage.show();

    }

    private Stage createModalAutWindow(Stage primaryStage){
        Stage stage = new Stage();
        try {
            Parent root = FXMLLoader.load(getClass().getResource("windows/authWindow.fxml"));
            stage.setTitle("Авторизация");
            stage.setScene(new Scene(root, 300, 200));
            stage.setResizable(false);
            //stage.initModality(Modality.WINDOW_MODAL);
            stage.initOwner(primaryStage);
            stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
                @Override
                public void handle(WindowEvent event) {
                    Platform.exit();
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
        return stage;
    }







    public static void main(String[] args) {
        launch(args);
    }
}
