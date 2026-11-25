
package campusconnect.model;

public class PaymentConcern extends ServiceRequest {

    public PaymentConcern(Student student, int priority, String concern) {
        super(student, priority, concern);
    }

    @Override
    public int estimateDays() {
        return adjustDays(1);
    }
}
