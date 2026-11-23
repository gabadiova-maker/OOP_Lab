
package campusconnect.app;

import campusconnect.model.Student;
import campusconnect.model.ServiceRequest;
import campusconnect.model.DocumentRequest;
import campusconnect.model.PaymentConcern;
import campusconnect.model.AdvisingRequest;
import campusconnect.model.EnrollmentRequest;
import campusconnect.model.RequestManager;

public class CampusConnectDemo {

    public static void main(String[] args) {

        Student student1 = new Student("2026-001", "Ana Cruz");
        Student student2 = new Student("2026-002", "Ben Lim");

        RequestManager manager = new RequestManager();

        ServiceRequest[] requests = {
            new EnrollmentRequest(student1, 3, "Enrollment inquiry"),
            new DocumentRequest(student1, 5, "Transcript request"),
            new PaymentConcern(student2, 2, "Payment verification"),
            new AdvisingRequest(student2, 5, "Academic advising")
        };

        System.out.println("=== CampusConnect TSA2 Demo ===\n");

        for (ServiceRequest request : requests) {
            manager.addRequest(request);
        }

        manager.displayRequests();

        System.out.println("\n=== Polymorphism Tests ===");

        System.out.println("Test 1: Document priority 5");
        System.out.println("Estimated days: " + requests[1].estimateDays());

        System.out.println("\nTest 2: Advising priority 5");
        System.out.println("Estimated days: " + requests[3].estimateDays());

        requests[0].markCompleted();

        manager.displaySummary();
    }
}
