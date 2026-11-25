
package bookbridge.model;

public class LoanRecord implements Renewable {

    private static int nextLoanNumber = 1001;

    private final int loanNumber;
    private final Member member;
    private final LibraryBook book;
    private int requestedDays;
    private boolean returned;

    public LoanRecord(Member member, LibraryBook book, int requestedDays) {
        if (member == null || book == null) {
            throw new IllegalArgumentException(
                    "Member and book cannot be null.");
        }

        if (requestedDays <= 0
                || requestedDays > member.getMaximumLoanDays()) {
            throw new IllegalArgumentException(
                    "Invalid requested loan days.");
        }

        if (!book.isAvailable()) {
            throw new IllegalStateException(
                    "Book is not available.");
        }

        this.loanNumber = nextLoanNumber++;
        this.member = member;
        this.book = book;
        this.requestedDays = requestedDays;
        this.returned = false;

        book.markBorrowed();
    }

    public int getLoanNumber() {
        return loanNumber;
    }

    public Member getMember() {
        return member;
    }

    public LibraryBook getBook() {
        return book;
    }

    public int getRequestedDays() {
        return requestedDays;
    }

    public boolean isReturned() {
        return returned;
    }

    public double calculateFine(int lateDays) {
        if (lateDays < 0) {
            throw new IllegalArgumentException(
                    "Late days cannot be negative.");
        }

        return lateDays * member.getDailyFine();
    }

    @Override
    public void renew(int additionalDays) {
        if (returned) {
            throw new IllegalStateException(
                    "Returned loans cannot be renewed.");
        }

        if (additionalDays <= 0
                || additionalDays > member.getMaximumLoanDays() - requestedDays) {
            throw new IllegalArgumentException(
                    "Renewal exceeds the member's loan limit.");
        }

        requestedDays += additionalDays;
    }

    public void markReturned() {
        if (returned) {
            throw new IllegalStateException(
                    "Loan has already been returned.");
        }

        book.markReturned();
        returned = true;
    }
}
