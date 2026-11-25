package campusconnect.service;

import campusconnect.model.*;
import campusconnect.exception.*;

import java.util.*;

public class CampusConnectService {

    // Main collection: ticket lookup
    private final Map<String, ServiceRequest> requests =
            new HashMap<>();

    // Pending requests
    private final Queue<ServiceRequest> pending =
            new PriorityQueue<>(
                    Comparator.comparingInt(
                            ServiceRequest::getPriority).reversed()
                    .thenComparingInt(ServiceRequest::estimateDays)
                    .thenComparingInt(ServiceRequest::getTicketNumber)
            );

    // Unique ticket IDs
    private final Set<String> ticketIds = new HashSet<>();

    private String key(ServiceRequest request) {
        return String.valueOf(request.getTicketNumber());
    }

    // Generic lookup helper
    public static <K, V> V findByKey(Map<K, V> map, K key) {
        return map.get(key);
    }

    public void addRequest(ServiceRequest request)
            throws DuplicateTicketException, InvalidRequestException {

        if (request == null) {
            throw new InvalidRequestException(
                    "Request cannot be null.");
        }

        String id = key(request);

        if (requests.containsKey(id) || ticketIds.contains(id)) {
            throw new DuplicateTicketException(
                    "Duplicate ticket: " + id);
        }

        if (request.getRequestStatus() != RequestStatus.PENDING) {
            throw new InvalidRequestException(
                    "Only pending requests can be added.");
        }

        requests.put(id, request);
        ticketIds.add(id);
        pending.offer(request);
    }

    public ServiceRequest findByTicket(String ticketId) {
        return findByKey(requests, ticketId);
    }

    public List<ServiceRequest> findByStudent(String studentId) {
        return requests.values().stream()
                .filter(r -> r.getStudent().getStudentId()
                        .equals(studentId))
                .toList();
    }

    public void updateStatus(String ticketId, RequestStatus newStatus)
            throws InvalidRequestException {

        ServiceRequest request = requests.get(ticketId);

        if (request == null) {
            throw new InvalidRequestException(
                    "Ticket not found: " + ticketId);
        }

        try {
            request.updateStatus(newStatus);
        } catch (IllegalStateException | IllegalArgumentException e) {
            throw new InvalidRequestException(
                    "Invalid status transition.", e);
        }

        if (request.getRequestStatus() != RequestStatus.PENDING) {
            pending.remove(request);
        }
    }

    public void removeRequest(String ticketId)
            throws InvalidRequestException {

        ServiceRequest request = requests.remove(ticketId);

        if (request == null) {
            throw new InvalidRequestException(
                    "Ticket not found: " + ticketId);
        }

        pending.remove(request);
        ticketIds.remove(ticketId);
    }

    public ServiceRequest processNext()
            throws InvalidRequestException {

        while (!pending.isEmpty()) {
            ServiceRequest request = pending.poll();

            if (request.getRequestStatus() == RequestStatus.PENDING
                    && requests.containsKey(key(request))) {

                try {
                    request.updateStatus(RequestStatus.IN_PROGRESS);
                    return request;
                } catch (IllegalStateException e) {
                    throw new InvalidRequestException(
                            "Unable to process request.", e);
                }
            }
        }

        return null;
    }

    public List<ServiceRequest> listAll() {
        List<ServiceRequest> result =
                new ArrayList<>(requests.values());

        result.sort(
                Comparator.comparingInt(
                        ServiceRequest::getPriority).reversed()
                .thenComparingInt(ServiceRequest::estimateDays)
                .thenComparingInt(ServiceRequest::getTicketNumber)
        );

        return List.copyOf(result);
    }

    public List<ServiceRequest> filterByStatus(RequestStatus status) {
        return requests.values().stream()
                .filter(r -> r.getRequestStatus() == status)
                .toList();
    }

    public List<ServiceRequest> filterByCategory(
            ServiceCategory category) {

        return requests.values().stream()
                .filter(r -> getCategory(r) == category)
                .toList();
    }

    private ServiceCategory getCategory(ServiceRequest request) {
        if (request instanceof EnrollmentRequest) {
            return ServiceCategory.ENROLLMENT;
        }
        if (request instanceof DocumentRequest) {
            return ServiceCategory.DOCUMENT;
        }
        if (request instanceof PaymentConcern) {
            return ServiceCategory.PAYMENT;
        }
        if (request instanceof AdvisingRequest) {
            return ServiceCategory.ADVISING;
        }
        throw new IllegalArgumentException(
                "Unknown request category.");
    }

    public Map<ServiceCategory, Long> countByCategory() {
        Map<ServiceCategory, Long> counts =
                new EnumMap<>(ServiceCategory.class);

        for (ServiceCategory category : ServiceCategory.values()) {
            counts.put(category, 0L);
        }

        for (ServiceRequest request : requests.values()) {
            ServiceCategory category = getCategory(request);
            counts.put(category, counts.get(category) + 1);
        }

        return Map.copyOf(counts);
    }

    public Map<RequestStatus, Long> countByStatus() {
        Map<RequestStatus, Long> counts =
                new EnumMap<>(RequestStatus.class);

        for (RequestStatus status : RequestStatus.values()) {
            counts.put(status, 0L);
        }

        for (ServiceRequest request : requests.values()) {
            RequestStatus status = request.getRequestStatus();
            counts.put(status, counts.get(status) + 1);
        }

        return Map.copyOf(counts);
    }

    public double averageEstimatedDays() {
        return requests.values().stream()
                .mapToInt(ServiceRequest::estimateDays)
                .average()
                .orElse(0);
    }

    public int totalRequests() {
        return requests.size();
    }

    public int pendingCount() {
        return pending.size();
    }
}
