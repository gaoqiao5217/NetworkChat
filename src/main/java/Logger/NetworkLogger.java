package Logger;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class NetworkLogger implements ChatLogger {
    private String filePath;
    SimpleDateFormat dateFormat;
    String regex = "\\[(0[1-9]|[12][0-9]|3[01])-(Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec) (2[0-3]|[01][0-9]):([0-5][0-9])\\] ";

    public NetworkLogger(String filePath) {
        this.filePath = filePath;
        dateFormat = new SimpleDateFormat("dd-MMM HH:mm:ss", Locale.ENGLISH);
    }

    @Override
    public void log(String message) {
        String log;
        if (message.matches(regex + ".*")) {
            log = dateFormat.format(new Date()) + ": " +
                    message.replaceAll(regex, "").trim();
        } else {
            log = dateFormat.format(new Date()) + ": " + message;
        }
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath, true))) {
            writer.write(log);
            writer.newLine();
        } catch (IOException e) {
            System.err.println("Error writing to file: " + e.getMessage());
        }
    }
}
