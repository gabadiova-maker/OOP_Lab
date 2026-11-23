
package bookbridge.model;

import java.util.ArrayList;
import java.util.List;

public class LibraryService {

    private final List<LoanRecord> loans = new ArrayList<>();
    private final List<Reservation> reservations = new ArrayList<>();

    public LoanRecord borrowBook(Member member, LibraryBook book, int days) {
        LoanRecord loan = new LoanRecord(member, book, days);
        loans.add(loan);
        return loan;
    }

    public void returnBook(LoanRecord loan) {
        if (!loans.contains(loan)) {
            throw new IllegalArgumentException(
                    "Loan does not belong to this library.");
        }

        loan.markReturned();
    }

    public Reservation reserveBook(Member member, LibraryBook book) {
        if (member == null || book == null) {
            throw new IllegalArgumentException(
                    "Member and book cannot be null.");
        }

        Reservation reservation = new Reservation(member, book);
        reservations.add(reservation);
        return reservation;
    }

    public void displaySummary() {
        int activeLoans = 0;
        int activeReservations = 0;

        for (LoanRecord loan : loans) {
            if (!loan.isReturned()) {
                activeLoans++;
            }
        }

        for (Reservation reservation : reservations) {
            if (reservation.isActive()) {
                activeReservations++;
            }
        }

        System.out.println("\n=== BookBridge Summary ===");
        System.out.println("Total Loans: " + loans.size());
        System.out.println("Active Loans: " + activeLoans);
        System.out.println("Active Reservations: " + activeReservations);
    }
    
    
    public String classifyRequest(Member member,
                                  LibraryBook book,
                                  int requestedDays) {

        if (member == null || book == null) {
            throw new IllegalArgumentException(
                    "Member and book cannot be null.");
        }

        if (requestedDays < 1 || requestedDays > 30) {
            throw new IllegalArgumentException(
                    "Requested days must be between 1 and 30.");
        }

        if (!book.isAvailable()) {
            return "RESERVED";
        }

        if (requestedDays > member.getMaximumLoanDays()) {
            return "DENIED";
        }

        return "APPROVED";
    }

}
