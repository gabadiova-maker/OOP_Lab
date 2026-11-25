package bookbridge.dao;

import java.sql.Connection;
import java.sql.SQLException;

public class DatabaseTest {

    public static void main(String[] args) {

        System.out.println("=== BookBridge Database Test ===");

        try (Connection conn = DatabaseConnection.getConnection()) {
            System.out.println("Database connected successfully!");
        } catch (SQLException e) {
            System.out.println("Connection failed: " + e.getMessage());
        }
    }
}
