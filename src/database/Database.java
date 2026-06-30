package database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Database {

    private static final String URL =
            "jdbc:postgresql://localhost:5432/TransactionManagement";

    private static final String USER = "adam";
    private static final String PASSWORD = "";

    public static Connection connect() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}
