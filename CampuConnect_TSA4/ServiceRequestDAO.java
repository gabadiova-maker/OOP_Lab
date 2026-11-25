
package campusconnect.dao;

import campusconnect.model.ServiceRequest;
import campusconnect.model.EnrollmentRequest;
import campusconnect.model.DocumentRequest;
import campusconnect.model.PaymentConcern;
import campusconnect.model.AdvisingRequest;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ServiceRequestDAO {

    public void addRequest(ServiceRequest request) throws SQLException {

        String sql = "INSERT INTO service_request "
                + "(ticket_id, student_id, student_name, category, "
                + "priority, concern, estimated_days, status) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);

            try {
                int ticket;

                try (PreparedStatement stmt = conn.prepareStatement(
                        "SELECT next_ticket FROM ticket_sequence "
                        + "WHERE id = 1 FOR UPDATE");
                     ResultSet rs = stmt.executeQuery()) {

                    if (!rs.next()) {
                        throw new SQLException("Ticket sequence not initialized.");
                    }

                    ticket = rs.getInt("next_ticket");
                }

                String category;

                if (request instanceof EnrollmentRequest) {
                    category = "ENROLLMENT";
                } else if (request instanceof DocumentRequest) {
                    category = "DOCUMENT";
                } else if (request instanceof PaymentConcern) {
                    category = "PAYMENT";
                } else if (request instanceof AdvisingRequest) {
                    category = "ADVISING";
                } else {
                    throw new SQLException("Unknown request category.");
                }

                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setString(1, String.valueOf(ticket));
                    stmt.setString(2, request.getStudent().getStudentId());
                    stmt.setString(3, request.getStudent().getStudentName());
                    stmt.setString(4, category);
                    stmt.setInt(5, request.getPriority());
                    stmt.setString(6, request.getConcern());
                    stmt.setInt(7, request.estimateDays());
                    stmt.setString(8, request.getStatus());

                    if (stmt.executeUpdate() != 1) {
                        throw new SQLException("Insert failed.");
                    }
                }

                try (PreparedStatement stmt = conn.prepareStatement(
                        "UPDATE ticket_sequence SET next_ticket = ? WHERE id = 1")) {
                    stmt.setInt(1, ticket + 1);

                    if (stmt.executeUpdate() != 1) {
                        throw new SQLException("Ticket update failed.");
                    }
                }

                conn.commit();

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

    public List<String[]> findAll() throws SQLException {

        List<String[]> records = new ArrayList<>();

        String sql = "SELECT ticket_id, student_id, student_name, "
                + "category, priority, concern, estimated_days, status "
                + "FROM service_request ORDER BY ticket_id";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                records.add(toRow(rs));
            }
        }

        return records;
    }

    public String[] findById(String ticketId) throws SQLException {

        String sql = "SELECT ticket_id, student_id, student_name, "
                + "category, priority, concern, estimated_days, status "
                + "FROM service_request WHERE ticket_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, ticketId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return toRow(rs);
                }
            }
        }

        return null;
    }

    public boolean updateStatus(String ticketId, String newStatus)
            throws SQLException {

        if (!newStatus.equals("IN_PROGRESS")
                && !newStatus.equals("COMPLETED")) {
            throw new IllegalArgumentException("Invalid status.");
        }

        String sql = "UPDATE service_request SET status = ? "
                + "WHERE ticket_id = ? AND "
                + "((status = 'PENDING' AND ? = 'IN_PROGRESS') "
                + "OR (status = 'IN_PROGRESS' AND ? = 'COMPLETED'))";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, newStatus);
            stmt.setString(2, ticketId);
            stmt.setString(3, newStatus);
            stmt.setString(4, newStatus);

            return stmt.executeUpdate() == 1;
        }
    }

    public boolean deleteRequest(String ticketId) throws SQLException {

        String sql = "DELETE FROM service_request WHERE ticket_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, ticketId);
            return stmt.executeUpdate() == 1;
        }
    }

    public String[] processNext() throws SQLException {

        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);

            try {
                String ticketId = null;

                String selectSql = "SELECT ticket_id FROM service_request "
                        + "WHERE status = 'PENDING' "
                        + "ORDER BY priority DESC, estimated_days ASC, "
                        + "CAST(ticket_id AS UNSIGNED) ASC LIMIT 1 FOR UPDATE";

                try (PreparedStatement stmt = conn.prepareStatement(selectSql);
                     ResultSet rs = stmt.executeQuery()) {

                    if (rs.next()) {
                        ticketId = rs.getString("ticket_id");
                    }
                }

                if (ticketId == null) {
                    conn.commit();
                    return null;
                }

                try (PreparedStatement stmt = conn.prepareStatement(
                        "UPDATE service_request SET status = 'IN_PROGRESS' "
                        + "WHERE ticket_id = ? AND status = 'PENDING'")) {

                    stmt.setString(1, ticketId);

                    if (stmt.executeUpdate() != 1) {
                        throw new SQLException("Could not process request.");
                    }
                }

                String[] result = null;

                try (PreparedStatement stmt = conn.prepareStatement(
                        "SELECT ticket_id, student_id, student_name, "
                        + "category, priority, concern, estimated_days, status "
                        + "FROM service_request WHERE ticket_id = ?")) {

                    stmt.setString(1, ticketId);

                    try (ResultSet rs = stmt.executeQuery()) {
                        if (rs.next()) {
                            result = toRow(rs);
                        }
                    }
                }

                conn.commit();
                return result;

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

    private String[] toRow(ResultSet rs) throws SQLException {

        return new String[]{
            rs.getString("ticket_id"),
            rs.getString("student_id"),
            rs.getString("student_name"),
            rs.getString("category"),
            rs.getString("priority"),
            rs.getString("concern"),
            rs.getString("estimated_days"),
            rs.getString("status")
        };
    }
}
