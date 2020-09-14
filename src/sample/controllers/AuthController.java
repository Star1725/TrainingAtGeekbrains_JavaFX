package sample.controllers;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import sample.ReadWriteNetHandler;
import sample.StartClient;

import javax.naming.Context;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.URL;
import java.util.ResourceBundle;

public class AuthController {
    public Button regBtn;
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



    public void onActionRegBtn(ActionEvent actionEvent) {
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
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    System.out.println(isAuthorization);
                }
                Platform.runLater(() -> {
                    loginBtn.getScene().getWindow().hide();

                });
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
}

