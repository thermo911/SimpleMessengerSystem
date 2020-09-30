package client;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ListView;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.ResourceBundle;

public class Controller implements Initializable {

    private SignUpController signUpController;

    @FXML
    public TextArea textArea;
    @FXML
    public TextField textField;
    @FXML
    public HBox authPanel;
    @FXML
    public TextField loginField;
    @FXML
    public PasswordField passwordField;
    @FXML
    public HBox msgPanel;
    @FXML
    public ListView<String> clientList;

    private final String IP_ADDRESS = "localhost";
    private final int PORT = 8189;

    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;

    private boolean authenticated;
    private String nickname;
    private final String TITLE = "Chat";

    private Stage stage;
    private Stage signUpStage;

    //private FileHistoryHandler fileHistoryHandler;

    public void setAuthenticated(boolean authenticated) {
        this.authenticated = authenticated;
        authPanel.setVisible(!authenticated);
        authPanel.setManaged(!authenticated);
        msgPanel.setVisible(authenticated);
        msgPanel.setManaged(authenticated);
        clientList.setVisible(authenticated);
        clientList.setManaged(authenticated);

        if (!authenticated) {
            nickname = "";
        }

        textArea.clear();
        setTitle(nickname);
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setAuthenticated(false);
        createSignUpStage();
        Platform.runLater(() -> {
            stage = (Stage) textField.getScene().getWindow();
            stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
                @Override
                public void handle(WindowEvent event) {
                    if(socket!=null && !socket.isClosed()) {
                        try {
                            out.writeUTF("/end");
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
        });
    }

    private void connect(){
        try {
            socket = new Socket(IP_ADDRESS, PORT);
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());

            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        //цикл аутентификации
                        while (true) {
                            String str = in.readUTF();

                            System.out.println(str);
                            if(str.startsWith("/")) {
                                if(str.startsWith("/end")) {
                                    throw new IOException();
                                }

                                if (str.startsWith("/authok")) {
                                    nickname = str.split(" ", 2)[1];
                                    setAuthenticated(true);
                                    break;
                                }

                                if (str.startsWith("/regok")) {
                                    signUpController.addMsgToTextArea("Account successfully created!");
                                    break;
                                }

                                if(str.startsWith("/regno")) {
                                    signUpController.addMsgToTextArea("User with this login or nickname already exists");
                                }
                            }

                            textArea.appendText(str + "\n");
                        }

                        /*fileHistoryHandler = new FileHistoryHandler(nickname);
                        ArrayList<String> list = fileHistoryHandler.getLines();

                        if(list.size()<=100) {
                            for(String s:list) {
                                textArea.appendText(s+"\n");
                            }
                        } else {
                            Iterator<String> it = list.iterator();
                            int i = 0;
                            while(i<100) {
                                it.next();
                                i++;
                            }
                            while(it.hasNext()) {
                                textArea.appendText(it.next()+"\n");
                            }
                        }*/

                        //цикл работы
                        while (true) {
                            String str = in.readUTF();

                            if(str.startsWith("/")) {
                                if (str.equals("/end")) {
                                    break;
                                }
                                if(str.startsWith("/clientlist")) {
                                    Platform.runLater(()->{
                                        clientList.getItems().clear();

                                        String[] tokens = str.split("\\s+");
                                        for(int i = 1; i< tokens.length; i++) {
                                            clientList.getItems().add(tokens[i]);
                                        }
                                    });
                                }
                            } else {
                                textArea.appendText(str + "\n");
                                //fileHistoryHandler.appendLine(str);
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        System.out.println("Мы отключились от сервера");
                        setAuthenticated(false);
                        //fileHistoryHandler.write();
                        try {
                            socket.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }).start();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendMsg(ActionEvent actionEvent) {
        try {
            if(!textField.getText().isEmpty()) {
                out.writeUTF(textField.getText());
                textField.clear();
                textField.requestFocus();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void tryToAuth(ActionEvent actionEvent) {
        if(socket == null || socket.isClosed()){
            connect();
        }

        try {
            out.writeUTF(String.format("/auth %s %s", loginField.getText().trim().toLowerCase(),
                    passwordField.getText().trim()));
            passwordField.clear();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void setTitle(String nick){
        Platform.runLater(()->{
            ((Stage)textField.getScene().getWindow()).setTitle(TITLE +" "+ nick);
        });
    }

    public void clickOnClient(MouseEvent mouseEvent) {
        String receiver = clientList.getSelectionModel().getSelectedItem();
        textField.clear();
        textField.setText(String.format("/private %s ", receiver));
    }

    private void createSignUpStage() {

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/SignUpWindow.fxml"));
            Parent root = loader.load();
            signUpStage = new Stage();
            signUpStage.setTitle("Creating an account");
            signUpStage.setScene(new Scene(root, 350, 250));

            signUpStage.initModality(Modality.APPLICATION_MODAL);

            signUpController = loader.getController();
            signUpController.setController(this);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void signingUp(ActionEvent actionEvent) {
        signUpStage.show();
    }

    public void tryToCreateAcc(String login, String password, String nickname) {
        String msg = String.format("/reg %s %s %s", login, password, nickname);
        if(socket == null || socket.isClosed()){
            connect();
        }

        try {
            out.writeUTF(msg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
