package sample.controllers;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import sample.ReadWriteNetHandler;

import java.io.IOException;

public class AuthController{
    public Button regBtn;
    public Label labelSecToClose;

    public void setTimeout(int timeout) {
        this.timeout = timeout/1000;
        System.out.println(String.format("timeout = %s sec", this.timeout));
    }

    private int timeout;

    public Stage getRegStage() {
        return regStage;
    }

    private Stage regStage;
    public TextField loginTxtFld;
    public PasswordField passTxtFld;
    public Button loginBtn;

    public void setAuthentication(boolean authentication) {
        isAuthentication = authentication;
        System.out.println("Аутентификация - " + authentication);
//        System.out.println("остановка потока ожидания аутентификации");
//        authThread.interrupt();
    }

    private boolean isAuthentication;

    private Thread authThread;

    public void setReadWriteNetHandler(ReadWriteNetHandler readWriteNetHandler) {
        this.readWriteNetHandler = readWriteNetHandler;
    }

    private ReadWriteNetHandler readWriteNetHandler;

    public void onActionRegBtn(ActionEvent actionEvent) {
        System.out.println("Попытка регистрации");
        if (readWriteNetHandler.getSocket() == null || readWriteNetHandler.getSocket().isClosed()){
            readWriteNetHandler.connectAndReadChat();
        }
        readWriteNetHandler.sendMsg("/timeout_off");
        if (authThread != null){
//            System.out.println("остановка потока ожидания аутентификации");
//            authThread.interrupt();
            Platform.runLater(() -> {
                labelSecToClose.setVisible(false);
            });
        }
        createRegWindow();
        regStage.show();
    }

    public void onActionLoginBtn(ActionEvent actionEvent) {
        if (readWriteNetHandler.getSocket() == null || readWriteNetHandler.getSocket().isClosed()){
            readWriteNetHandler.connectAndReadChat();
        }
        if (!loginTxtFld.equals("") || !passTxtFld.getText().isEmpty()){
            labelSecToClose.setVisible(true);
            readWriteNetHandler.tryAuth(loginTxtFld.getText().trim().toLowerCase(), passTxtFld.getText().trim());
            //поток ожидания аутентификации
            if (authThread == null || !authThread.isAlive()){
                authThread = new Thread(() -> {
                while (!isAuthentication){
                    try {
                        Thread.sleep(1000);
                        System.out.println(Thread.currentThread().getName() + " - до конца аутентификации осталось - " + (timeout -= 1));
                        Platform.runLater(() -> {
                            labelSecToClose.setText(String.valueOf(timeout));
                        });
                    } catch (InterruptedException e) {
                        break;
                    }
                    if (timeout == 0){
                        Platform.runLater(() -> {
                            labelSecToClose.setText("");
                        });
                        break;
                    }
                    System.out.println(isAuthentication);
                    }
                    if (isAuthentication){
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
            RegController regController = loader.getController();
            regController.setReadWriteNetHandler(readWriteNetHandler);
            regStage.initModality(Modality.APPLICATION_MODAL);
            regStage.setResizable(false);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}

