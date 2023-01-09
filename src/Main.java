import io.github.cdimascio.dotenv.Dotenv;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.*;
import java.time.Month;
import java.util.*;

public class Main {
    static List<String> HTMLTables = new ArrayList<>();
    static Set<String> recipients = new HashSet<>();
    static Dotenv dotenv = Dotenv.load();

    public static void main(String[] args) {
        List<String> oldData;
        try {
            oldData = readFile();
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }

        List<String> newData;
        newData = fetchData();

        // Avoid the first run saying data changed
        if (Objects.equals(oldData, null)) {
            Logger.log("First run, no data to compare");
            try {
                saveToFile(newData);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            return;
        }

        // Change Detection
        if (newData.equals(oldData)) {
            Logger.log("No change");
        } else {
            Logger.log("Change detected");
            try {
                saveToFile(newData);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            try {
                getRecipients();
                sendEmail();
            } catch (IOException | MessagingException e) {
                Logger.log("Error sending email:" + e.getMessage(), true);
            }
        }
    }

    public static void saveToFile(List<String> array) throws IOException {
        FileWriter fw = new FileWriter(dotenv.get("LOG_DIRECTORY_PATH") + "schedule.txt");
        for (var el : array) {
            fw.write(el + "\n");
        }
        fw.close();
    }

    public static List<String> readFile() throws IOException, ClassNotFoundException {
        var file = new File(dotenv.get("LOG_DIRECTORY_PATH") + "schedule.txt");
        if (!file.exists()) {
            var result = file.createNewFile();
            Logger.log("File created: " + result + " at " + dotenv.get("LOG_DIRECTORY_PATH") + "schedule.txt");
            return null;
        }

        return getScheduleFile(file);
    }

    static List<String> getScheduleFile(File scheduleFile) throws FileNotFoundException {
        var scanner = new Scanner(scheduleFile);

        List<String> array = new ArrayList<>();
        while (scanner.hasNextLine()) {
            var line = scanner.nextLine();
            array.add(line);
        }

        scanner.close();
        return array;
    }

    public static void getRecipients() throws MessagingException, IOException {
        var properties = System.getProperties();
        properties.put("mail.pop3.host", dotenv.get("POP_HOST"));
        properties.put("mail.pop3.port", dotenv.get("POP_PORT"));
        properties.put("mail.pop3.ssl.enable", "false");
        properties.put("mail.pop3.auth", "true");
        var session = Session.getInstance(properties, new javax.mail.Authenticator(){
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(dotenv.get("FROM_EMAIL"), dotenv.get("PASSWORD"));
            }
        });
        var store = session.getStore("pop3");

        store.connect(dotenv.get("HOST"), dotenv.get("FROM_EMAIL"), dotenv.get("PASSWORD"));

        var emailFolder = store.getFolder("INBOX");
        emailFolder.open(Folder.READ_ONLY);
        Logger.log("Total Messages:" + emailFolder.getMessageCount());
        for (Message message : emailFolder.getMessages()) {
            Logger.log("---------------------------------");
            Logger.log("Subject: " + message.getSubject());
            Logger.log("From: " + message.getFrom()[0]);
            if (message.getSubject().trim().equalsIgnoreCase("subscribe")) {
                recipients.add(message.getFrom()[0].toString().split("<")[1].split(">")[0].trim());
            } else if (message.getSubject().trim().equalsIgnoreCase("unsubscribe")) {
                recipients.remove(message.getFrom()[0].toString().split("<")[1].split(">")[0].trim());
            }
        }
    }

    public static void sendEmail() throws IOException {
        var from = dotenv.get("FROM_EMAIL");
        var host = dotenv.get("SMTP_HOST");

        var properties = System.getProperties();
        properties.put("mail.smtp.host", host);
        properties.put("mail.smtp.port", dotenv.get("SMTP_PORT"));
        properties.put("mail.smtp.ssl.enable", "false");
        properties.put("mail.smtp.auth", "true");

        var session = Session.getInstance(properties, new javax.mail.Authenticator(){
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(from, dotenv.get("PASSWORD"));
            }
        });

        try {
            if (recipients.size() == 0) {
                Logger.log("No recipients");
                return;
            }

            var message = new MimeMessage(session);
            message.setFrom(new InternetAddress(from));

            for (String recipient : recipients) {
                message.addRecipient(Message.RecipientType.TO, new InternetAddress(recipient));
                Logger.log("Sending email to " + recipient);
            }

            // All of these you can change to whatever you want
            message.setSubject("Rozvrh na VŠEM se změnil");
            var html = new StringBuilder();
            html.append("<html><body><h1>Rozvrh na VŠEM se změnil</h1><p>Nový rozvrh je na adrese: <a href=\"").append(getUrl()).append("\">").append(getUrl()).append("</a></p><p>Nový rozvrh:</p>");
            for (String table : HTMLTables) {
                // This is to remove the unnecessary tables
                if (table.contains("<tr class=\"row_0 row_first even\">") || table.contains("Ekonomika")) continue;

                html.append(table);
            }

            message.setContent(html.toString(), "text/html; charset=UTF-8");

            Transport.send(message);
            Logger.log("Email sent successfully");
        } catch (MessagingException mex) {
            mex.printStackTrace();
        }
    }

    public static List<String> fetchData() {
        List<String> tables = new ArrayList<>();
        try {
            final var doc = Jsoup.connect(getUrl()).get();
            // You may need to change the selector to match your page
            final var newsHeadlines = doc.select("table");
            for (Element headline : newsHeadlines) {
                tables.add(headline.text());
                HTMLTables.add(headline.html());
            }
        } catch (IOException e) {
            Logger.log("Error fetching data: " + e.getMessage(), true);
        }
        return tables;
    }

    // You may need to change this to match your page
    // It checks if a page with the next month exists, if it does, it returns the url of that page, otherwise it returns the url of the current month
    public static String getUrl() {
        var url = "https://www.akademievsem.cz/rozvrh-akademie-vsem-" + getMonth(true) + ".html";

        try {
            Jsoup.connect(url).get();
        } catch (IOException e) {
            url = "https://www.akademievsem.cz/rozvrh-akademie-vsem-" + getMonth(false) + ".html";
        }
        return url;
    }

    // You may not need this if you don't have a page with the next month's schedule
    public static String getMonth(boolean plusOne) {
        var s = "";
        Month month;
        if (plusOne) {
            month = java.time.LocalDate.now().plusMonths(1).getMonth();
        } else {
            month = java.time.LocalDate.now().getMonth();
        }
        switch (month) {
            case JANUARY -> s = "leden";
            case FEBRUARY -> s = "unor";
            case MARCH -> s = "brezen";
            case APRIL -> s = "duben";
            case MAY -> s = "kveten";
            case JUNE -> s = "cerven";
            case JULY -> s = "cervenec";
            case AUGUST -> s = "srpen";
            case SEPTEMBER -> s = "zari";
            case OCTOBER -> s = "rijen";
            case NOVEMBER -> s = "listopad";
            case DECEMBER -> s = "prosinec";
        }
        return s;
    }
}