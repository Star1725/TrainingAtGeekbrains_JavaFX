package sample.controllers;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import sample.ReadWriteNetHandler;
import sample.StartClient;

import javax.naming.Context;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.net.URL;
import java.util.ResourceBundle;

public class AuthController{
    public Button regBtn;
    public Label labelSecToClose;

    public void setTimeout(int timeout) {
        this.timeout = timeout/1000;
        System.out.println("Timeout = " + timeout);
    }

    private int timeout;

    public Stage getRegStage() {
        return regStage;
    }

    private Stage regStage;
    public TextField loginTxtFld;
    public PasswordField passTxtFld;
    public Button loginBtn;

    public boolean isAuthorization() {
        return isAuthorization;
    }

    public void setAuthorization(boolean authorization) {
        isAuthorization = authorization;
    }

    private boolean isAuthorization;

    public ReadWriteNetHandler getReadWriteNetHandler() {
        return readWriteNetHandler;
    }

    public void setReadWriteNetHandler(ReadWriteNetHandler readWriteNetHandler) {
        this.readWriteNetHandler = readWriteNetHandler;
    }

    private ReadWriteNetHandler readWriteNetHandler;
    private RegController regController;

    public void onActionRegBtn(ActionEvent actionEvent) {
        System.out.println("Попытка регистрации");
        createRegWindow();
        regStage.show();
    }

    public void onActionLoginBtn(ActionEvent actionEvent) {
        if (readWriteNetHandler.getSocket() == null || readWriteNetHandler.getSocket().isClosed()){
            readWriteNetHandler.connectAndReadChat();
        }
        if (!loginTxtFld.equals("") || !passTxtFld.getText().isEmpty()){
            readWriteNetHandler.tryAuth(loginTxtFld.getText().trim().toLowerCase(), passTxtFld.getText().trim());
            System.out.println("заходим в цикл ожидания авторизации");
            Thread authThread = new Thread(() -> {
                while (!isAuthorization){
                    try {
                        Thread.sleep(1000);
                        System.out.println("До конца аутентификации осталось - " + (timeout -= 1));
                        Platform.runLater(() -> {
                            labelSecToClose.setText(String.valueOf(timeout));
                        });
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    if (timeout == 0){
                        Platform.runLater(() -> {
                            labelSecToClose.setText("");
                        });
                        break;
                    }
                    System.out.println(isAuthorization);
                }
                if (isAuthorization){
                    Platform.runLater(() -> {
                        loginBtn.getScene().getWindow().hide();
                        labelSecToClose.setText("");
                    });
                }
            });
            authThread.setDaemon(true);
            authThread.start();
        }
    }

    public void showAlertWindow(String title, String text){
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle(title);
            alert.setContentText(text);
            alert.setHeaderText("");
            alert.showAndWait();
        });
    }

    private void createRegWindow(){
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("../windows/regWindow.fxml"));
            Parent parent = loader.load();
            regStage = new Stage();
            regStage.setTitle("Регистрация");
            regStage.setScene(new Scene(parent, 400, 230));
            regController = loader.getController();
            regController.setReadWriteNetHandler(readWriteNetHandler);
            regStage.initModality(Modality.APPLICATION_MODAL);
            regStage.setResizable(false);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}

