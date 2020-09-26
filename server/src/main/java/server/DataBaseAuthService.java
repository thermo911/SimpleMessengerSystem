package server;

import java.sql.*;

public class DataBaseAuthService implements AuthService {

    private Connection connection;
    private PreparedStatement psSelect;
    private ResultSet rs;

    public DataBaseAuthService() {
        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:database.db");
            psSelect = connection.
                    prepareStatement("SELECT * FROM users WHERE login = ?;");
        } catch (SQLException | ClassNotFoundException throwables) {
            throwables.printStackTrace();
        }

    }

    @Override
    public String getNicknameByLoginAndPassword(String login, String password) {
        try (PreparedStatement psSelect = connection.
                prepareStatement("SELECT * FROM users WHERE login = ? AND password = ?;")) {
            psSelect.setObject(1, login);
            psSelect.setObject(2, password);
            ResultSet rs = psSelect.executeQuery();

            String nick = null;
            if(rs.next()) {
                nick = rs.getString("nickname");
            }
            return nick;
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            return null;
        }
    }

    @Override
    public boolean registration(String login, String password, String nickname) {
        if(userExists(login, nickname)) {
            return false;
        }

        try (PreparedStatement psInsert = connection.
                prepareStatement("INSERT INTO users (login, nickname, password) VALUES(?, ?, ?);")) {
            psInsert.setObject(1, login);
            psInsert.setObject(2, nickname);
            psInsert.setObject(3, password);
            psInsert.execute();

            return userExists(login, nickname);

        } catch (SQLException throwables) {
            throwables.printStackTrace();
            return false;
        }
    }

    boolean userExists(String login, String nickname) {
        try (PreparedStatement psSelect = connection.
                prepareStatement("SELECT * FROM users WHERE login = ? OR nickname = ?;")) {
            psSelect.setObject(1, login);
            psSelect.setObject(2, nickname);
            ResultSet rs = psSelect.executeQuery();

            int count = 0;
            while(rs.next()) {
                count++;
            }

            return count > 0;

        } catch (SQLException throwables) {
            throwables.printStackTrace();
            return false;
        }
    }
}
