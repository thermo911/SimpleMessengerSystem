package client;

import java.io.*;
import java.util.ArrayList;

public class FileHistoryHandler {
    private String path;
    private File file;
    private BufferedReader br;
    private BufferedWriter bw;
    private ArrayList<String> stringArrayList;

    public FileHistoryHandler(String nickname) throws IOException {
        path = String.format("client/src/main/chat_history/%s_history.txt", nickname);
        file = new File(path);
        if(!file.exists()) {
            file.createNewFile();
        }
        stringArrayList = new ArrayList<>();

        readFile();
    }

    private void readFile() {
        try (BufferedReader reader = new BufferedReader(new FileReader(path))) {
            String str;
            while ((str = reader.readLine()) != null) {
                stringArrayList.add(str);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void write() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(path))) {
            for(String s: stringArrayList) {
                writer.write(s+"\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void appendLine(String str) {
        stringArrayList.add(str);
    }

    public ArrayList<String> getLines() {
        return stringArrayList;
    }


}
