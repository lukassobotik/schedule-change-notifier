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
    static boolean isDebug = dotenv.get("DEBUG").equals("true");
    public static void main(String[] args) {
        List<String> oldData;
        try {
            oldData = readFile();
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }

        List<String> newData;
        newData = fetchData();

        if (newData == null) {
            Logger.log("Error while fetching data. Exiting...", true);
            return;
        }

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
        if (isDebug) Logger.log("Writing to file: " + array);
        fw.close();
    }

    public static List<String> readFile() throws IOException, ClassNotFoundException {
        var file = new File(dotenv.get("LOG_DIRECTORY_PATH") + "schedule.txt");
        if (!file.exists()) {
            var result = file.createNewFile();
            Logger.log("File created: " + result + " at " + dotenv.get("LOG_DIRECTORY_PATH") + "schedule.txt");
            return null;
        }

        var array = getScheduleFile(file);
        if (isDebug) Logger.log("Data from file: " + array);
        return array;
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
                message.setFlag(Flags.Flag.DELETED, true);
            } else {
                message.setFlag(Flags.Flag.DELETED, true);
            }
        }
        session.getTransport().close();
        store.close();
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
            if (isDebug) Logger.log("Fetched tables: " + tables);
            return tables;
        } catch (IOException e) {
            Logger.log("Error fetching data: " + e.getMessage(), true);
            return null;
        }
    }

    // You may need to change this to match your page
    // It gets the schedule url from the list of schedule links on the website.
    public static String getUrl() {
        var scheduleListUrl = "https://www.akademievsem.cz/evyhledavac-aakademie-vsem.html";
        var baseUrl = "https://www.akademievsem.cz/";
        var url = new StringBuilder();

        try {
            final var doc = Jsoup.connect(scheduleListUrl).get();
            // Gets the header (in this case an image) where the links are listed under
            for(var element : doc.getElementsByAttributeValueMatching("src", "data/images/img-akademie/evyhledavac_rozvrh_ak.png")) {
                var parent = element.parent();
                var links = Objects.requireNonNull(parent).getElementsByAttribute("href");
                var link = Objects.requireNonNull(links.last()).attr("href");
                url.append(baseUrl).append(link);
                if (isDebug) Logger.log("Fetching URL: " + url);
            }
        } catch (IOException e) {
            Logger.log("Error getting the URL: " + e.getMessage(), true);
            return null;
        }
        return url.toString();
    }
}