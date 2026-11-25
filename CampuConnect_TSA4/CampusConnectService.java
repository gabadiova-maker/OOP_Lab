
package campusconnect.service;

import campusconnect.dao.ServiceRequestDAO;
import campusconnect.model.ServiceRequest;

import java.sql.SQLException;
import java.util.List;

public class CampusConnectService {

    private final ServiceRequestDAO dao = new ServiceRequestDAO();

    public void addRequest(ServiceRequest request)
            throws SQLException {

        if (request == null) {
            throw new IllegalArgumentException(
                    "Request cannot be null.");
        }

        dao.addRequest(request);
    }

    public String[] findById(String ticketId)
            throws SQLException {

        if (ticketId == null || ticketId.isBlank()) {
            throw new IllegalArgumentException(
                    "Ticket ID cannot be blank.");
        }

        return dao.findById(ticketId.trim());
    }

    public List<String[]> findAll() throws SQLException {
        return dao.findAll();
    }

    public boolean updateStatus(String ticketId, String status)
            throws SQLException {

        if (ticketId == null || ticketId.isBlank()) {
            throw new IllegalArgumentException(
                    "Ticket ID cannot be blank.");
        }

        if (!"IN_PROGRESS".equals(status)
                && !"COMPLETED".equals(status)) {
            throw new IllegalArgumentException(
                    "Invalid request status.");
        }

        return dao.updateStatus(ticketId.trim(), status);
    }

    public boolean deleteRequest(String ticketId)
            throws SQLException {

        if (ticketId == null || ticketId.isBlank()) {
            throw new IllegalArgumentException(
                    "Ticket ID cannot be blank.");
        }

        return dao.deleteRequest(ticketId.trim());
    }

    public String[] processNext() throws SQLException {
        return dao.processNext();
    }
}
