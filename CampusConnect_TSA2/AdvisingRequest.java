
package campusconnect.model;

public class AdvisingRequest extends ServiceRequest {

    public AdvisingRequest(Student student, int priority, String concern) {
        super(student, priority, concern);
    }

    @Override
    public int estimateDays() {
        return adjustDays(4);
    }
}
