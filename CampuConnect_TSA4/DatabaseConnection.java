
package campusconnect.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {

    private static final String URL =
            "jdbc:mariadb://localhost:3306/campusconnect_tsa4_db";

    public static Connection getConnection() throws SQLException {

        String user = System.getenv("LAB8_DB_USER");
        String password = System.getenv("LAB8_DB_PASSWORD");

        if (user == null || password == null) {
            throw new SQLException(
                    "Database credentials are not configured.");
        }

        return DriverManager.getConnection(URL, user, password);
    }
}
