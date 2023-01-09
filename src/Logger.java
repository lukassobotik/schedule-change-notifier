import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Logger {
    public static void log(String message, boolean isErrorMessage) {
        if (isErrorMessage) {
            var date = java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss"));
            message = date + "           [ ERROR ]            " + message;
            System.out.println("\033[0;31m" + message + "\033[0m");
            saveToFile(message);
        } else {
            log(message);
        }
    }

    public static void log(String message) {
        var date = java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss"));
        message = date + "           [ INFO ]             " + message;
        System.out.println(message);
        saveToFile(message);
    }

    private static void saveToFile(String message) {
        try {
            var list = readFile();
            if (list == null) {
                list = new ArrayList<>();
            }
            list.add(message);
            FileWriter fw = new FileWriter(Main.dotenv.get("LOG_DIRECTORY_PATH") + "log.txt");
            for (var el : list) {
                fw.write(el + "\n");
            }
            fw.close();
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private static List<String> readFile() throws IOException, ClassNotFoundException {
        var file = new File(Main.dotenv.get("LOG_DIRECTORY_PATH") + "log.txt");
        if (!file.exists()) {
            file.createNewFile();
            return null;
        }

        return Main.getScheduleFile(file);
    }
}
