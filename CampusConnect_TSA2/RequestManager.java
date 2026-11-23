
package campusconnect.model;

import java.util.ArrayList;
import java.util.List;

public class RequestManager {

    private final List<ServiceRequest> requests = new ArrayList<>();

    public void addRequest(ServiceRequest request) {
        if (request == null) {
            throw new IllegalArgumentException(
                    "Request cannot be null.");
        }

        requests.add(request);
    }

    public void displayRequests() {
        System.out.println("=== CampusConnect Requests ===");

        for (ServiceRequest request : requests) {
            System.out.println(
                    "Ticket: " + request.getTicketNumber()
                    + " | Student: " + request.getStudent().getStudentName()
                    + " | Category: " + request.getClass().getSimpleName()
                    + " | Priority: " + request.getPriority()
                    + " | Estimated Days: " + request.estimateDays()
                    + " | Status: " + request.getStatus()
            );
        }
    }

    public void displaySummary() {
        int total = requests.size();
        int pending = 0;
        int completed = 0;

        for (ServiceRequest request : requests) {
            if (request.getStatus().equals("PENDING")) {
                pending++;
            } else if (request.getStatus().equals("COMPLETED")) {
                completed++;
            }
        }

        System.out.println("\n=== Request Summary ===");
        System.out.println("Total Requests: " + total);
        System.out.println("Pending: " + pending);
        System.out.println("Completed: " + completed);
    }
}
