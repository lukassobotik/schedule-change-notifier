import io.github.cdimascio.dotenv.Dotenv;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.IOException;
import java.time.Month;
import java.util.*;

public class Main {
    static ArrayList<String> HTMLTables = new ArrayList<>();
    static ArrayList<String> recipients = new ArrayList<>();

    static Dotenv dotenv = Dotenv.load();

    public static void main(String[] args) {
//        int period = 3600000; // 1 hour
        int period = 10000;
        Timer timer = new Timer();

        final ArrayList<String>[] newData = new ArrayList[]{new ArrayList<>()};
        final ArrayList<String>[] oldData = new ArrayList[]{new ArrayList<>()};
        timer.scheduleAtFixedRate(new TimerTask() {
            public void run() {
                newData[0] = FetchData();

                // Avoid the first run saying data changed
                if (Objects.equals(oldData[0], new ArrayList<>())) {
                    System.out.println("No change");
                    oldData[0] = newData[0];
                    return;
                }

                if (newData[0].equals(oldData[0])) {
                    System.out.println("No change");
                } else {
                    System.out.println("Change detected");
                    oldData[0] = newData[0];
                    try {
                        sendEmail();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }, 0, period);
    }

    public static void sendEmail() throws IOException {
        String from = dotenv.get("FROM_EMAIL");
        String host = dotenv.get("SMTP_HOST");

        Properties properties = System.getProperties();
        properties.put("mail.smtp.host", host);
        properties.put("mail.smtp.port", dotenv.get("SMTP_PORT"));
        properties.put("mail.smtp.ssl.enable", "false");
        properties.put("mail.smtp.auth", "true");

        Session session = Session.getInstance(properties, new javax.mail.Authenticator(){
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(from, dotenv.get("PASSWORD"));
            }
        });

        try {
            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(from));

            for (String recipient : recipients) {
                message.addRecipient(Message.RecipientType.TO, new InternetAddress(recipient));
                System.out.println("Sending email to " + recipient);
            }

            message.setSubject("Rozvrh na VŠEM se změnil");
            StringBuilder html = new StringBuilder();
            html.append("<html><body><h1>Rozvrh na VŠEM se změnil</h1><p>Nový rozvrh je na adrese: <a href=\"").append(getUrl()).append("\">").append(getUrl()).append("</a></p><p>Nový rozvrh:</p>");
            for (String table : HTMLTables) {
                if (table.contains("<tr class=\"row_0 row_first even\">") || table.contains("Ekonomika")) continue;
                html.append(table);
            }

            message.setContent(html.toString(), "text/html; charset=UTF-8");

            Transport.send(message);
        } catch (MessagingException mex) {
            mex.printStackTrace();
        }
    }

    public static ArrayList<String> FetchData() {
        Document doc;
        ArrayList<String> tables = new ArrayList<>();
        try {
            doc = Jsoup.connect(getUrl()).get();
            Elements newsHeadlines = doc.select("table");
            for (Element headline : newsHeadlines) {
                tables.add(headline.text());
                HTMLTables.add(headline.html());
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return tables;
    }

    public static String getUrl() {
        var url = "https://www.akademievsem.cz/rozvrh-akademie-vsem-" + getMonth(true) + ".html";

        try {
            Jsoup.connect(url).get();
        } catch (IOException e) {
            url = "https://www.akademievsem.cz/rozvrh-akademie-vsem-" + getMonth(false) + ".html";
        }
        return url;
    }

    public static String getMonth(boolean plusOne) {
        String s = "";
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