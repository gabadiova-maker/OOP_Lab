
package campusconnect.model;

public abstract class ServiceRequest implements Prioritizable {

    private static int nextTicket = 1001;

    private final int ticketNumber;
    private final Student student;
    private final int priority;
    private final String concern;
    private String status;

    public ServiceRequest(Student student, int priority, String concern) {
        if (student == null) {
            throw new IllegalArgumentException("Student cannot be null.");
        }

        if (priority < 1 || priority > 5) {
            throw new IllegalArgumentException("Priority must be 1 to 5.");
        }

        if (concern == null || concern.isBlank()) {
            throw new IllegalArgumentException("Concern cannot be blank.");
        }

        this.ticketNumber = nextTicket++;
        this.student = student;
        this.priority = priority;
        this.concern = concern;
        this.status = "PENDING";
    }

    public int getTicketNumber() {
        return ticketNumber;
    }

    public Student getStudent() {
        return student;
    }

    @Override
    public int getPriority() {
        return priority;
    }

    public String getConcern() {
        return concern;
    }

    public String getStatus() {
        return status;
    }

    public void markCompleted() {
        if (!status.equals("PENDING")) {
            throw new IllegalStateException(
                    "Only pending requests can be completed.");
        }
        status = "COMPLETED";
    }

    protected int adjustDays(int baseDays) {
        if (priority == 5 && baseDays > 1) {
            return baseDays - 1;
        }
        return baseDays;
    }

    public abstract int estimateDays();
}
