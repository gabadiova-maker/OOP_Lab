
package campusconnect.model;

public class DocumentRequest extends ServiceRequest {

    public DocumentRequest(Student student, int priority, String concern) {
        super(student, priority, concern);
    }

    @Override
    public int estimateDays() {
        return adjustDays(2);
    }
}
