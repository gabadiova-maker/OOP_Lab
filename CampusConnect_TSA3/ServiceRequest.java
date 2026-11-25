package campusconnect.model;

public abstract class ServiceRequest implements Prioritizable {

    private static int nextTicket = 1001;

    private final int ticketNumber;
    private final Student student;
    private final int priority;
    private final String concern;

    private RequestStatus status;

    public ServiceRequest(Student student, int priority, String concern) {

        if (student == null) {
            throw new IllegalArgumentException(
                    "Student cannot be null.");
        }

        if (priority < 1 || priority > 5) {
            throw new IllegalArgumentException(
                    "Priority must be 1 to 5.");
        }

        if (concern == null || concern.isBlank()) {
            throw new IllegalArgumentException(
                    "Concern cannot be blank.");
        }

        this.ticketNumber = nextTicket++;
        this.student = student;
        this.priority = priority;
        this.concern = concern;
        this.status = RequestStatus.PENDING;
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

    // Preserves compatibility with TSA2 code
    public String getStatus() {
        return status.name();
    }

    // Enum-based status getter for TSA3
    public RequestStatus getRequestStatus() {
        return status;
    }

    // Validated status transitions
    public void updateStatus(RequestStatus newStatus) {

        if (newStatus == null) {
            throw new IllegalArgumentException(
                    "New status cannot be null.");
        }

        boolean valid = switch (status) {

            case PENDING ->
                newStatus == RequestStatus.IN_PROGRESS
                || newStatus == RequestStatus.CANCELLED
                || newStatus == RequestStatus.COMPLETED;

            case IN_PROGRESS ->
                newStatus == RequestStatus.COMPLETED
                || newStatus == RequestStatus.CANCELLED;

            case COMPLETED, CANCELLED -> false;
        };

        if (!valid) {
            throw new IllegalStateException(
                    "Invalid status transition: "
                    + status + " to " + newStatus);
        }

        status = newStatus;
    }

    // Original TSA2 method
    public void markCompleted() {

        if (status != RequestStatus.PENDING) {
            throw new IllegalStateException(
                    "Only pending requests can be completed.");
        }

        updateStatus(RequestStatus.COMPLETED);
    }

    protected int adjustDays(int baseDays) {

        if (priority == 5 && baseDays > 1) {
            return baseDays - 1;
        }

        return baseDays;
    }

    public abstract int estimateDays();
}
