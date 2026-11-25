
package bookbridge.dao;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class BookBridgeDAO {

    public void addMember(String id, String name, String type)
            throws SQLException {

        if (id == null || id.isBlank()
                || name == null || name.isBlank()) {
            throw new IllegalArgumentException(
                    "Member ID and name are required.");
        }

        if (!List.of("STUDENT", "FACULTY", "GUEST").contains(type)) {
            throw new IllegalArgumentException(
                    "Invalid membership type.");
        }

        String sql = "INSERT INTO member "
                + "(member_id, member_name, membership_type) "
                + "VALUES (?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, id.trim());
            stmt.setString(2, name.trim());
            stmt.setString(3, type);

            if (stmt.executeUpdate() != 1) {
                throw new SQLException("Member was not saved.");
            }
        }
    }

    public void addBook(String isbn, String title, int copies)
            throws SQLException {

        if (isbn == null || isbn.isBlank()
                || title == null || title.isBlank()) {
            throw new IllegalArgumentException(
                    "ISBN and title are required.");
        }

        if (copies < 1) {
            throw new IllegalArgumentException(
                    "Copies must be at least 1.");
        }

        String sql = "INSERT INTO book "
                + "(isbn, title, total_copies, available_copies) "
                + "VALUES (?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, isbn.trim());
            stmt.setString(2, title.trim());
            stmt.setInt(3, copies);
            stmt.setInt(4, copies);

            if (stmt.executeUpdate() != 1) {
                throw new SQLException("Book was not saved.");
            }
        }
    }

    public List<String[]> getAllMembers() throws SQLException {
        List<String[]> members = new ArrayList<>();

        String sql = "SELECT member_id, member_name, membership_type "
                + "FROM member ORDER BY member_id";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                members.add(new String[]{
                    rs.getString("member_id"),
                    rs.getString("member_name"),
                    rs.getString("membership_type")
                });
            }
        }

        return members;
    }

    public List<String[]> getAllBooks() throws SQLException {
        List<String[]> books = new ArrayList<>();

        String sql = "SELECT isbn, title, total_copies, available_copies "
                + "FROM book ORDER BY isbn";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                books.add(new String[]{
                    rs.getString("isbn"),
                    rs.getString("title"),
                    rs.getString("total_copies"),
                    rs.getString("available_copies")
                });
            }
        }

        return books;
    }

    public String[] findMember(String id) throws SQLException {
        String sql = "SELECT member_id, member_name, membership_type "
                + "FROM member WHERE member_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new String[]{
                        rs.getString("member_id"),
                        rs.getString("member_name"),
                        rs.getString("membership_type")
                    };
                }
            }
        }

        return null;
    }

    public String[] findBook(String isbn) throws SQLException {
        String sql = "SELECT isbn, title, total_copies, available_copies "
                + "FROM book WHERE isbn = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, isbn);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new String[]{
                        rs.getString("isbn"),
                        rs.getString("title"),
                        rs.getString("total_copies"),
                        rs.getString("available_copies")
                    };
                }
            }
        }

        return null;
    }

    private int maximumDays(String type) {
        return switch (type) {
            case "STUDENT" -> 14;
            case "FACULTY" -> 30;
            case "GUEST" -> 7;
            default -> throw new IllegalArgumentException(
                    "Invalid membership type.");
        };
    }

    public int borrowBook(String memberId, String isbn, int days)
            throws SQLException {

        if (days < 1 || days > 30) {
            throw new IllegalArgumentException(
                    "Loan days must be between 1 and 30.");
        }

        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);

            try {
                String memberType;

                try (PreparedStatement stmt = conn.prepareStatement(
                        "SELECT membership_type FROM member "
                        + "WHERE member_id = ?")) {

                    stmt.setString(1, memberId);

                    try (ResultSet rs = stmt.executeQuery()) {
                        if (!rs.next()) {
                            throw new IllegalArgumentException(
                                    "Member not found.");
                        }
                        memberType = rs.getString(1);
                    }
                }

                if (days > maximumDays(memberType)) {
                    throw new IllegalArgumentException(
                            "Loan exceeds membership limit.");
                }

                int available;

                try (PreparedStatement stmt = conn.prepareStatement(
                        "SELECT available_copies FROM book "
                        + "WHERE isbn = ? FOR UPDATE")) {

                    stmt.setString(1, isbn);

                    try (ResultSet rs = stmt.executeQuery()) {
                        if (!rs.next()) {
                            throw new IllegalArgumentException(
                                    "Book not found.");
                        }
                        available = rs.getInt(1);
                    }
                }

                if (available < 1) {
                    throw new IllegalStateException(
                            "Book unavailable. Reservation required.");
                }

                try (PreparedStatement stmt = conn.prepareStatement(
                        "UPDATE book SET available_copies = "
                        + "available_copies - 1 WHERE isbn = ? "
                        + "AND available_copies > 0")) {

                    stmt.setString(1, isbn);

                    if (stmt.executeUpdate() != 1) {
                        throw new SQLException(
                                "Unable to update book availability.");
                    }
                }

                LocalDate loanDate = LocalDate.now();
                LocalDate dueDate = loanDate.plusDays(days);

                int loanId;

                try (PreparedStatement stmt = conn.prepareStatement(
                        "INSERT INTO loan "
                        + "(member_id, isbn, loan_days, loan_date, "
                        + "due_date, status) VALUES (?, ?, ?, ?, ?, 'ACTIVE')",
                        Statement.RETURN_GENERATED_KEYS)) {

                    stmt.setString(1, memberId);
                    stmt.setString(2, isbn);
                    stmt.setInt(3, days);
                    stmt.setDate(4, Date.valueOf(loanDate));
                    stmt.setDate(5, Date.valueOf(dueDate));

                    if (stmt.executeUpdate() != 1) {
                        throw new SQLException("Loan was not saved.");
                    }

                    try (ResultSet keys = stmt.getGeneratedKeys()) {
                        if (!keys.next()) {
                            throw new SQLException("Loan ID not generated.");
                        }
                        loanId = keys.getInt(1);
                    }
                }

                conn.commit();
                return loanId;

            } catch (SQLException | RuntimeException e) {
                try {
                    conn.rollback();
                } catch (SQLException rollbackError) {
                    e.addSuppressed(rollbackError);
                }
                throw e;
            }
        }
    }

    public double returnBook(int loanId) throws SQLException {

        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);

            try {
                String isbn;
                String memberType;
                LocalDate dueDate;

                String sql = "SELECT l.isbn, l.due_date, "
                        + "m.membership_type, l.status "
                        + "FROM loan l JOIN member m "
                        + "ON l.member_id = m.member_id "
                        + "WHERE l.loan_id = ? FOR UPDATE";

                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setInt(1, loanId);

                    try (ResultSet rs = stmt.executeQuery()) {
                        if (!rs.next()) {
                            throw new IllegalArgumentException(
                                    "Loan not found.");
                        }

                        if (!"ACTIVE".equals(rs.getString("status"))) {
                            throw new IllegalStateException(
                                    "Loan already returned.");
                        }

                        isbn = rs.getString("isbn");
                        memberType = rs.getString("membership_type");
                        dueDate = rs.getDate("due_date").toLocalDate();
                    }
                }

                LocalDate today = LocalDate.now();
                long lateDays = Math.max(0,
                        java.time.temporal.ChronoUnit.DAYS.between(
                                dueDate, today));

                double dailyFine = switch (memberType) {
                    case "STUDENT" -> 5.0;
                    case "FACULTY" -> 2.0;
                    case "GUEST" -> 10.0;
                    default -> throw new IllegalStateException(
                            "Invalid membership type.");
                };

                double fine = lateDays * dailyFine;

                try (PreparedStatement stmt = conn.prepareStatement(
                        "UPDATE loan SET status = 'RETURNED', "
                        + "return_date = ? WHERE loan_id = ? "
                        + "AND status = 'ACTIVE'")) {

                    stmt.setDate(1, Date.valueOf(today));
                    stmt.setInt(2, loanId);

                    if (stmt.executeUpdate() != 1) {
                        throw new SQLException("Loan return failed.");
                    }
                }

                try (PreparedStatement stmt = conn.prepareStatement(
                        "UPDATE book SET available_copies = "
                        + "available_copies + 1 WHERE isbn = ? "
                        + "AND available_copies < total_copies")) {

                    stmt.setString(1, isbn);

                    if (stmt.executeUpdate() != 1) {
                        throw new SQLException(
                                "Book availability update failed.");
                    }
                }

                conn.commit();
                return fine;

            } catch (SQLException | RuntimeException e) {
                try {
                    conn.rollback();
                } catch (SQLException rollbackError) {
                    e.addSuppressed(rollbackError);
                }
                throw e;
            }
        }
    }

    public List<String[]> getAllLoans() throws SQLException {
        List<String[]> loans = new ArrayList<>();

        String sql = "SELECT loan_id, member_id, isbn, loan_days, "
                + "due_date, status FROM loan ORDER BY loan_id";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                loans.add(new String[]{
                    rs.getString("loan_id"),
                    rs.getString("member_id"),
                    rs.getString("isbn"),
                    rs.getString("loan_days"),
                    rs.getString("due_date"),
                    rs.getString("status")
                });
            }
        }

        return loans;
    }
    
    public int reserveBook(String memberId, String isbn)
            throws SQLException {

        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);

            try {
                // Confirm member exists
                try (PreparedStatement stmt = conn.prepareStatement(
                        "SELECT member_id FROM member WHERE member_id = ?")) {
                    stmt.setString(1, memberId);
                    try (ResultSet rs = stmt.executeQuery()) {
                        if (!rs.next()) {
                            throw new IllegalArgumentException(
                                    "Member not found.");
                        }
                    }
                }

                // Confirm book exists
                try (PreparedStatement stmt = conn.prepareStatement(
                        "SELECT isbn FROM book WHERE isbn = ?")) {
                    stmt.setString(1, isbn);
                    try (ResultSet rs = stmt.executeQuery()) {
                        if (!rs.next()) {
                            throw new IllegalArgumentException(
                                    "Book not found.");
                        }
                    }
                }

                // Prevent duplicate pending reservations
                try (PreparedStatement stmt = conn.prepareStatement(
                        "SELECT reservation_id FROM reservation "
                        + "WHERE member_id = ? AND isbn = ? "
                        + "AND status = 'PENDING'")) {
                    stmt.setString(1, memberId);
                    stmt.setString(2, isbn);

                    try (ResultSet rs = stmt.executeQuery()) {
                        if (rs.next()) {
                            throw new IllegalStateException(
                                    "Member already has a pending reservation.");
                        }
                    }
                }

                int reservationId;

                try (PreparedStatement stmt = conn.prepareStatement(
                        "INSERT INTO reservation (member_id, isbn, status) "
                        + "VALUES (?, ?, 'PENDING')",
                        Statement.RETURN_GENERATED_KEYS)) {

                    stmt.setString(1, memberId);
                    stmt.setString(2, isbn);

                    if (stmt.executeUpdate() != 1) {
                        throw new SQLException("Reservation was not saved.");
                    }

                    try (ResultSet keys = stmt.getGeneratedKeys()) {
                        if (!keys.next()) {
                            throw new SQLException(
                                    "Reservation ID was not generated.");
                        }
                        reservationId = keys.getInt(1);
                    }
                }

                conn.commit();
                return reservationId;

            } catch (SQLException | RuntimeException e) {
                try {
                    conn.rollback();
                } catch (SQLException rollbackError) {
                    e.addSuppressed(rollbackError);
                }
                throw e;
            }
        }
    }

    public boolean cancelReservation(int reservationId)
            throws SQLException {

        String sql = "UPDATE reservation SET status = 'CANCELLED' "
                + "WHERE reservation_id = ? AND status = 'PENDING'";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, reservationId);
            return stmt.executeUpdate() == 1;
        }
    }
    
    public Integer processNextReservation() throws SQLException {

    try (Connection conn = DatabaseConnection.getConnection()) {
        conn.setAutoCommit(false);

        try {
            int reservationId;
            String isbn;
            String memberId;

            String selectSql =
                    "SELECT reservation_id, member_id, isbn "
                    + "FROM reservation "
                    + "WHERE status = 'PENDING' "
                    + "ORDER BY created_at ASC, reservation_id ASC "
                    + "LIMIT 1 FOR UPDATE";

            try (PreparedStatement stmt =
                    conn.prepareStatement(selectSql);
                 ResultSet rs = stmt.executeQuery()) {

                if (!rs.next()) {
                    conn.commit();
                    return null;
                }

                reservationId = rs.getInt("reservation_id");
                memberId = rs.getString("member_id");
                isbn = rs.getString("isbn");
            }

            String type;

            try (PreparedStatement stmt = conn.prepareStatement(
                    "SELECT membership_type FROM member "
                    + "WHERE member_id = ?")) {

                stmt.setString(1, memberId);

                try (ResultSet rs = stmt.executeQuery()) {
                    if (!rs.next()) {
                        throw new SQLException("Member not found.");
                    }
                    type = rs.getString(1);
                }
            }

            int available;

            try (PreparedStatement stmt = conn.prepareStatement(
                    "SELECT available_copies FROM book "
                    + "WHERE isbn = ? FOR UPDATE")) {

                stmt.setString(1, isbn);

                try (ResultSet rs = stmt.executeQuery()) {
                    if (!rs.next()) {
                        throw new SQLException("Book not found.");
                    }
                    available = rs.getInt(1);
                }
            }

            if (available < 1) {
                conn.commit();
                return null;
            }

            // Use the member's maximum borrowing period
            int days = maximumDays(type);

            LocalDate today = LocalDate.now();
            LocalDate dueDate = today.plusDays(days);

            // Create a loan for the reservation
            try (PreparedStatement stmt = conn.prepareStatement(
                    "INSERT INTO loan "
                    + "(member_id, isbn, loan_days, loan_date, "
                    + "due_date, status) "
                    + "VALUES (?, ?, ?, ?, ?, 'ACTIVE')")) {

                stmt.setString(1, memberId);
                stmt.setString(2, isbn);
                stmt.setInt(3, days);
                stmt.setDate(4, Date.valueOf(today));
                stmt.setDate(5, Date.valueOf(dueDate));

                if (stmt.executeUpdate() != 1) {
                    throw new SQLException("Loan creation failed.");
                }
            }

            // Decrease available copies
            try (PreparedStatement stmt = conn.prepareStatement(
                    "UPDATE book SET available_copies = "
                    + "available_copies - 1 "
                    + "WHERE isbn = ? AND available_copies > 0")) {

                stmt.setString(1, isbn);

                if (stmt.executeUpdate() != 1) {
                    throw new SQLException(
                            "Book availability update failed.");
                }
            }

            // Complete the reservation
            try (PreparedStatement stmt = conn.prepareStatement(
                    "UPDATE reservation SET status = 'PROCESSED' "
                    + "WHERE reservation_id = ? "
                    + "AND status = 'PENDING'")) {

                stmt.setInt(1, reservationId);

                if (stmt.executeUpdate() != 1) {
                    throw new SQLException(
                            "Reservation processing failed.");
                }
            }

            conn.commit();
            return reservationId;

        } catch (SQLException | RuntimeException e) {
            try {
                conn.rollback();
            } catch (SQLException rollbackError) {
                e.addSuppressed(rollbackError);
            }
            throw e;
        }
    }
}
    
    public List<String[]> getAllReservations() throws SQLException {

        List<String[]> reservations = new ArrayList<>();

        String sql = "SELECT reservation_id, member_id, isbn, status "
                + "FROM reservation ORDER BY reservation_id";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                reservations.add(new String[]{
                    rs.getString("reservation_id"),
                    rs.getString("member_id"),
                    rs.getString("isbn"),
                    rs.getString("status")
                });
            }
        }

        return reservations;
    }

}
