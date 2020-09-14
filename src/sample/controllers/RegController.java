package sample.controllers;

import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import sample.ReadWriteNetHandler;

public class RegController {
    public TextField loginTxtFldForReg;
    public PasswordField passTxtFldForReg;
    public Button regBtnForReg;
    public PasswordField passTxtFldForRegConfirm;
    public TextField nickNameTxtFldForReg;

    public void setReadWriteNetHandler(ReadWriteNetHandler readWriteNetHandler) {
        this.readWriteNetHandler = readWriteNetHandler;
    }

    ReadWriteNetHandler readWriteNetHandler;

    public void onActionTryRegBtn(ActionEvent actionEvent) {
        if (readWriteNetHandler.getSocket() == null || readWriteNetHandler.getSocket().isClosed()){
            readWriteNetHandler.connectAndReadChat();
        }
        if ((!loginTxtFldForReg.getText().isEmpty() ||
                !passTxtFldForReg.getText().isEmpty() ||
                !passTxtFldForRegConfirm.getText().isEmpty() ||
                !nickNameTxtFldForReg.getText().isEmpty()) &&
                (passTxtFldForReg.getText().equals(passTxtFldForRegConfirm.getText()))){
            readWriteNetHandler.tryReg(loginTxtFldForReg.getText().trim(),
                    passTxtFldForReg.getText().trim(),
                    nickNameTxtFldForReg.getText().trim());

        }


    }
}
