package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.Vector;
import java.util.logging.*;

public class Server {
    private List<ClientHandler> clients;
    private AuthService authService;
    private static final Logger logger = Logger.getLogger(Server.class.getName());
    private static Handler handler;

    private int PORT = 8170;
    ServerSocket server = null;
    Socket socket = null;

    public Server() {
        clients = new Vector<>();
        //authService = new SimpleAuthService();
        authService = new DataBaseAuthService();
        handler = new ConsoleHandler();
        handler.setLevel(Level.INFO);
        handler.setFormatter(new SimpleFormatter());
        logger.setLevel(Level.INFO);
        logger.addHandler(handler);
        logger.setUseParentHandlers(false);

        try {
            server = new ServerSocket(PORT);
            logger.log(Level.SEVERE, "Сервер запущен");

            while (true) {
                socket = server.accept();
                logger.log(Level.INFO, "Клиент подключился");
                new ClientHandler(this, socket);
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                server.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public AuthService getAuthService() {
        return authService;
    }

    public void broadcastMsg(ClientHandler sender, String msg){
        String message = String.format("%s : %s", sender.getNickname(), msg);
        logger.log(Level.INFO, String.format("broadcast message from %s", sender));
        for (ClientHandler c : clients) {
            c.sendMsg(message);
        }
    }

    public void sendPrivateMsg(ClientHandler sender, String nickname, String msg) {
        for(ClientHandler client: clients) {
            if(nickname.equals(client.getNickname())) {
                client.sendMsg(String.format("private [%s -> %s]: %s", sender.getNickname(), nickname, msg));
                logger.log(Level.INFO, String.format("private message from %s to %s", sender, nickname));
                return;
            }
            if(!nickname.equals(sender.getNickname())) {
                sender.sendMsg(String.format("private [%s -> %s]: %s", sender.getNickname(), nickname, msg));
            }
        }
        sender.sendMsg(String.format("user %s not found", nickname));
    }

    public void subscribe(ClientHandler clientHandler) {
        clients.add(clientHandler);
        logger.log(Level.INFO, String.format("%s subscribed", clientHandler.getLogin()));
        broadcastClientList();
    }

    public void unsubscribe(ClientHandler clientHandler) {
        clients.remove(clientHandler);
        logger.log(Level.INFO, String.format("%s unsubscribed", clientHandler.getLogin()));
        broadcastClientList();
    }

    public boolean isLoginAuthenticated(String login) {
        for (ClientHandler client : clients) {
            if(client.getLogin().equals(login)) {
                return true;
            }
        }
        return false;
    }

    private void broadcastClientList() {
        StringBuilder sb = new StringBuilder("/clientlist ");
        for (ClientHandler c : clients) {
            sb.append(c.getNickname()+" ");
        }

        String msg = sb.toString();

        for (ClientHandler c : clients) {
            c.sendMsg(msg);
        }
    }
}
