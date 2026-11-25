
package campusconnect.model;

public class EnrollmentRequest extends ServiceRequest {

    public EnrollmentRequest(Student student, int priority, String concern) {
        super(student, priority, concern);
    }

    @Override
    public int estimateDays() {
        return adjustDays(3);
    }
}
