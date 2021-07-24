import org.jooq.codegen.maven.example.Tables;
import org.jooq.impl.DSL;

import java.sql.Connection;
import java.sql.SQLException;

import static java.sql.DriverManager.getConnection;

public class Main {

    private static final String URL = "jdbc:postgresql://localhost:5432/ttrss";
    private static final String USER = "postgres";
    private static final String PASSWORD = "example";

    public static void main(String[] args) throws ClassNotFoundException {
        Class.forName("org.postgresql.Driver");
        try (Connection c = getConnection(URL, USER, PASSWORD)) {
            DSL.using(c)
                    .fetch(Tables.TTRSS_ENTRIES)
                    .forEach(entry -> System.out.println(entry.getTitle()));
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }
}
