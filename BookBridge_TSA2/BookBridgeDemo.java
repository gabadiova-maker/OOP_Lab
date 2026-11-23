
package bookbridge.app;

import bookbridge.model.Member;
import bookbridge.model.StudentMember;
import bookbridge.model.FacultyMember;
import bookbridge.model.GuestMember;
import bookbridge.model.LibraryBook;
import bookbridge.model.LoanRecord;
import bookbridge.model.Reservation;
import bookbridge.model.LibraryService;

public class BookBridgeDemo {

    public static void main(String[] args) {

        System.out.println("=== BookBridge TSA2 Demo ===");

        Member student = new StudentMember("M001", "Ana Cruz");
        Member faculty = new FacultyMember("M002", "Ben Lim");
        Member guest = new GuestMember("M003", "Cara Reyes");

        Member[] members = {student, faculty, guest};

        System.out.println("\n=== Membership Polymorphism ===");

        for (Member member : members) {
            System.out.println(
                    member.getClass().getSimpleName()
                    + " | Name: " + member.getMemberName()
                    + " | Max Days: " + member.getMaximumLoanDays()
                    + " | Daily Fine: PHP " + member.getDailyFine());
        }

        LibraryService library = new LibraryService();

        LibraryBook book1 = new LibraryBook("B001", "Java Programming");
        LibraryBook book2 = new LibraryBook("B002", "Database Systems");

        System.out.println("\n=== Borrowing Test ===");

        LoanRecord loan = library.borrowBook(student, book1, 5);

        System.out.println("Loan Number: " + loan.getLoanNumber());
        System.out.println("Borrowed: " + book1.getTitle());
        System.out.println("Available: " + book1.isAvailable());

        System.out.println("\n=== Fine Calculation ===");
        System.out.println("Student fine for 2 late days: PHP "
                + loan.calculateFine(2));

        System.out.println("\n=== Renewal Test ===");
        loan.renew(2);
        System.out.println("Total loan days: " + loan.getRequestedDays());

        System.out.println("\n=== Reservation Test ===");
        Reservation reservation = library.reserveBook(faculty, book2);
        System.out.println("Reserved: " + reservation.getBook().getTitle());
        System.out.println("Active: " + reservation.isActive());

        System.out.println("\n=== Return Test ===");
        library.returnBook(loan);
        System.out.println("Book available: " + book1.isAvailable());

        
        System.out.println("\n=== TSA1 Regression Tests ===");

        Member testStudent = new StudentMember("001", "Ana");
        Member testFaculty = new FacultyMember("002", "Ben");
        Member testGuest = new GuestMember("003", "Cara");

        System.out.println("Test 1 - Normal Approval: "
                + library.classifyRequest(testStudent,
                        new LibraryBook("ISBN001", "Java Basics"), 10));

        LibraryBook unavailable =
                new LibraryBook("ISBN002", "Database");
        unavailable.markBorrowed();

        System.out.println("Test 2 - Unavailable Reservation: "
                + library.classifyRequest(testFaculty, unavailable, 20));

        System.out.println("Test 3 - Over-Limit Denial: "
                + library.classifyRequest(testGuest,
                        new LibraryBook("ISBN003", "Networks"), 10));

        System.out.println("Test 4 - Student Boundary (14 days): "
                + library.classifyRequest(testStudent,
                        new LibraryBook("ISBN004", "OOP"), 14));

        System.out.println("Test 5 - Faculty Maximum (30 days): "
                + library.classifyRequest(testFaculty,
                        new LibraryBook("ISBN005", "Algorithms"), 30));
        
        library.displaySummary();
    }
}
