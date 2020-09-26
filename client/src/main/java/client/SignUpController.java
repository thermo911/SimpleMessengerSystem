package client;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

public class SignUpController {

    private Controller controller;
    @FXML
    private TextField loginField;
    @FXML
    private TextField nicknameField;
    @FXML
    private TextField passwordField;
    @FXML
    private TextField confirmPasswordField;
    @FXML
    private TextArea textArea;

    public void tryToSignUp(ActionEvent actionEvent) {
        String login = loginField.getText().trim();
        String nickname = nicknameField.getText().trim();
        String password = passwordField.getText().trim();
        String confirmPassword = confirmPasswordField.getText().trim();

        if(login.isEmpty() || nickname.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            textArea.clear();
            textArea.appendText("Fill all fields\n");
            return;
        }

        if(!password.equals(confirmPassword)) {
            textArea.appendText("Passwords do not match\n");
            return;
        }

        controller.tryToCreateAcc(login, password, nickname);
    }

    public void setController(Controller controller) {
        this.controller = controller;
    }

    public void addMsgToTextArea(String msg) {
        textArea.appendText(msg + "\n");
    }
}
