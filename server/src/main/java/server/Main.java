package server;

public class Main {
    public static void main(String[] args) {
        DataBaseAuthService db = new DataBaseAuthService();
        System.out.println(db.userExists("qwe", "qwe"));
        System.out.println(db.getNicknameByLoginAndPassword("qwe", "qwe"));
    }
}
