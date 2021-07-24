import com.google.common.base.CharMatcher;
import com.google.common.base.Splitter;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.mutable.MutableLong;
import org.apache.lucene.analysis.de.GermanAnalyzer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.jooq.codegen.maven.example.Tables;
import org.jooq.impl.DSL;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.sql.DriverManager.getConnection;

public class Main {

    private static final String URL = "jdbc:postgresql://localhost:5432/ttrss";
    private static final String USER = "postgres";
    private static final String PASSWORD = "example";
    public static final Splitter SPLITTER = Splitter.on(CharMatcher.javaLetterOrDigit().negate()).omitEmptyStrings();
    public static final GermanAnalyzer GERMAN_ANALYZER = new GermanAnalyzer();
    public static final EnglishAnalyzer ENGLISH_ANALYZER = new EnglishAnalyzer();

    public static void main(String[] args) throws ClassNotFoundException {
        Class.forName("org.postgresql.Driver");
        try (Connection c = getConnection(URL, USER, PASSWORD)) {
            Map<String, MutableLong> counterMap = new HashMap<>();
            DSL.using(c)
                    .fetch(Tables.TTRSS_ENTRIES)
                    .forEach(entry -> {
                        processTitle(entry.getTitle(), counterMap);
                    });

            keepEntriesWithHigherCounter(counterMap, 50L);
            getEntriesWithHighestFrequencyFirst(counterMap).forEach(System.out::println);
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    private static List<Map.Entry<String, MutableLong>> getEntriesWithHighestFrequencyFirst(Map<String, MutableLong> counterMap) {
        List<Map.Entry<String, MutableLong>> entries = new ArrayList<>(counterMap.entrySet());
        Comparator<Map.Entry<String, MutableLong>> comparing = Comparator.comparing(entry -> entry.getValue().getValue());
        Comparator<Map.Entry<String, MutableLong>> reversed = comparing.reversed();
        entries.sort(reversed);
        return entries;
    }

    private static void keepEntriesWithHigherCounter(Map<String, MutableLong> counterMap, long counter) {
        counterMap.entrySet().removeIf(next -> next.getValue().getValue() < counter);
    }

    private static void processTitle(String title, Map<String, MutableLong> counterMap) {
        String input = StringUtils.lowerCase(title);
        for (String word : SPLITTER.split(input)) {
            if (accept(word)) {
                MutableLong mutableLong = counterMap.getOrDefault(word, new MutableLong(0));
                mutableLong.increment();
                counterMap.put(word, mutableLong);
            }
        }
    }

    private static boolean accept(String word) {
        if (GERMAN_ANALYZER.getStopwordSet().contains(word)
                || ENGLISH_ANALYZER.getStopwordSet().contains(word)) {
            return false;
        }
        return !CharMatcher.digit().matchesAllOf(word)
                && word.length() > 1;
    }
}
