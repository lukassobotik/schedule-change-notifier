import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.time.Month;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

public class Main {
    public static void main(String[] args) {
        int period = 3600000;
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
                } else{
                    System.out.println("Change detected");
                    oldData[0] = newData[0];
                }
            }
        }, 0, period);
    }

    public static ArrayList<String> FetchData() {
        Document doc;
        ArrayList<String> tables = new ArrayList<>();
        try {
            doc = Jsoup.connect(getUrl()).get();
            Elements newsHeadlines = doc.select("table");
            for (Element headline : newsHeadlines) {
                tables.add(headline.text());
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