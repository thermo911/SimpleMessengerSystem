package server;

import java.sql.*;
import java.util.ArrayList;

public class DataBaseHistoryHandler {
    private Connection connection;

    public DataBaseHistoryHandler() {
        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:database.db");
        } catch (SQLException | ClassNotFoundException throwables) {
            throwables.printStackTrace();
        }
    }

    private String getNickByID(int id) {
        String nick = null;

        try(PreparedStatement ps = connection.
                prepareStatement("SELECT nickname FROM users WHERE id = ?")) {
            ps.setInt(1, id);
            ResultSet res = ps.executeQuery();

            if(res.next()) {
                nick = res.getString("nickname");
            }

            res.close();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }

        return nick;
    }

    //TODO
    public ArrayList<String> getHistory(String nickname) {
        ArrayList<String> list = new ArrayList<>();
        try(PreparedStatement ps = connection.
                prepareStatement("SELECT * FROM messages " +
                        "WHERE sender = (SELECT id FROM users WHERE nickname = ?) " +
                        "OR receiver = (SELECT id FROM users WHERE nickname = ?) " +
                        "OR receiver = 0")) {
            ps.setString(1, nickname);
            ps.setString(2, nickname);

            ResultSet res = ps.executeQuery();
            String str;

            while(res.next()) {
                if(res.getString("receiver").equals("null")) {
                    str = String.format("%s : %s", getNickByID(-100000000), res.getString("message"));
                }
            }

        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return list;
    }
}
