
package campusconnect.model;

public class Student {

    private final String studentId;
    private final String studentName;

    public Student(String studentId, String studentName) {
        if (studentId == null || studentId.isBlank()
                || studentName == null || studentName.isBlank()) {
            throw new IllegalArgumentException(
                    "Student ID and name cannot be blank.");
        }

        this.studentId = studentId;
        this.studentName = studentName;
    }

    public String getStudentId() {
        return studentId;
    }

    public String getStudentName() {
        return studentName;
    }
}
