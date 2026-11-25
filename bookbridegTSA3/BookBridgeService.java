package bookbridge.service;

import bookbridge.model.*;
import bookbridge.exception.*;

import java.util.*;
import java.time.LocalDate;

public class BookBridgeService {

    // Required TSA3 collections
    private final Map<String, LibraryBook> books = new HashMap<>();
    private final Map<String, Member> members = new HashMap<>();
    private final Map<String, LoanRecord> activeLoans = new HashMap<>();

    private final Queue<Reservation> pendingReservations =
            new ArrayDeque<>();

    private final Set<String> checkedOutISBNs = new HashSet<>();

    private final List<LoanRecord> loanHistory = new ArrayList<>();

    // Additional data needed for due-date sorting
    private final Map<String, LocalDate> dueDates = new HashMap<>();

    // Register a member
    public void registerMember(Member member)
            throws DuplicateEntityException {

        Objects.requireNonNull(member, "Member cannot be null.");

        String id = member.getMemberId();

        if (members.containsKey(id)) {
            throw new DuplicateEntityException(
                    "Member already exists: " + id);
        }

        members.put(id, member);
    }

    // Add a book
    public void addBook(LibraryBook book)
            throws DuplicateEntityException {

        Objects.requireNonNull(book, "Book cannot be null.");

        String isbn = book.getBookId();

        if (books.containsKey(isbn)) {
            throw new DuplicateEntityException(
                    "Book already exists: " + isbn);
        }

        books.put(isbn, book);
    }

    // Find operations
    public Member findMember(String memberId) {
        return members.get(memberId);
    }

    public LibraryBook findBook(String isbn) {
        return books.get(isbn);
    }

    public LoanRecord findActiveLoan(String loanId) {
        return activeLoans.get(loanId);
    }

    // Issue a loan
    public LoanRecord issueLoan(
            String memberId, String isbn, int days)
            throws ItemUnavailableException,
                   LoanLimitExceededException {

        Member member = members.get(memberId);
        LibraryBook book = books.get(isbn);

        if (member == null) {
            throw new IllegalArgumentException(
                    "Member not found: " + memberId);
        }

        if (book == null) {
            throw new IllegalArgumentException(
                    "Book not found: " + isbn);
        }

        if (days < 1 || days > member.getMaximumLoanDays()) {
            throw new LoanLimitExceededException(
                    "Loan exceeds membership limit.");
        }

        if (!book.isAvailable()
                || checkedOutISBNs.contains(isbn)) {
            throw new ItemUnavailableException(
                    "Book is currently unavailable.");
        }

        // LoanRecord constructor marks book as borrowed
        LoanRecord loan = new LoanRecord(member, book, days);

        String loanId = String.valueOf(loan.getLoanNumber());

        activeLoans.put(loanId, loan);
        checkedOutISBNs.add(isbn);
        loanHistory.add(loan);

        dueDates.put(loanId, LocalDate.now().plusDays(days));

        return loan;
    }

    // Return a loan
    public void returnBook(String loanId) {

        LoanRecord loan = activeLoans.get(loanId);

        if (loan == null) {
            throw new IllegalArgumentException(
                    "Active loan not found: " + loanId);
        }

        String isbn = loan.getBook().getBookId();

        // LoanRecord handles the availability change
        loan.markReturned();

        activeLoans.remove(loanId);
        checkedOutISBNs.remove(isbn);
    }

    // Reserve a book
    public Reservation reserveBook(
            String memberId, String isbn) {

        Member member = members.get(memberId);
        LibraryBook book = books.get(isbn);

        if (member == null || book == null) {
            throw new IllegalArgumentException(
                    "Member or book not found.");
        }

        for (Reservation reservation : pendingReservations) {
            if (reservation.isActive()
                    && reservation.getMember().getMemberId()
                            .equals(memberId)
                    && reservation.getBook().getBookId()
                            .equals(isbn)) {
                throw new IllegalStateException(
                        "Duplicate pending reservation.");
            }
        }

        Reservation reservation = new Reservation(member, book);
        pendingReservations.offer(reservation);

        return reservation;
    }

    // Process earliest pending reservation
    public LoanRecord processNextReservation()
            throws ItemUnavailableException,
                   LoanLimitExceededException {

        Reservation reservation = pendingReservations.peek();

        if (reservation == null) {
            return null;
        }

        Member member = reservation.getMember();
        LibraryBook book = reservation.getBook();

        if (!book.isAvailable()) {
            throw new ItemUnavailableException(
                    "Reserved book is not available.");
        }

        LoanRecord loan = issueLoan(
                member.getMemberId(),
                book.getBookId(),
                member.getMaximumLoanDays());

        pendingReservations.poll();
        reservation.cancel();

        return loan;
    }

    // Defensive views
    public List<Member> listMembers() {
        return List.copyOf(members.values());
    }

    public List<LibraryBook> listBooks() {
        return List.copyOf(books.values());
    }

    public List<LoanRecord> listLoanHistory() {
        return List.copyOf(loanHistory);
    }

    public List<Reservation> listPendingReservations() {
        return List.copyOf(pendingReservations);
    }

    // Search books by title keyword
    public List<LibraryBook> searchByTitle(String keyword) {

        String search = keyword == null
                ? "" : keyword.toLowerCase(Locale.ROOT);

        return books.values().stream()
                .filter(book -> book.getTitle()
                        .toLowerCase(Locale.ROOT)
                        .contains(search))
                .toList();
    }

    // Filter members by membership type
    public List<Member> filterMembers(MembershipType type) {

        return members.values().stream()
                .filter(member -> {
                    if (type == MembershipType.STUDENT) {
                        return member instanceof StudentMember;
                    }
                    if (type == MembershipType.FACULTY) {
                        return member instanceof FacultyMember;
                    }
                    return member instanceof GuestMember;
                })
                .toList();
    }

    // Filter loans by status
    public List<LoanRecord> filterLoans(LoanStatus status) {

        return loanHistory.stream()
                .filter(loan -> {
                    if (status == LoanStatus.RETURNED) {
                        return loan.isReturned();
                    }

                    if (status == LoanStatus.ACTIVE) {
                        return !loan.isReturned();
                    }

                    return !loan.isReturned()
                            && LocalDate.now().isAfter(
                                    dueDates.get(String.valueOf(
                                            loan.getLoanNumber())));
                })
                .toList();
    }

    // Required multi-key sorting
    public List<LoanRecord> getSortedLoans() {

        List<LoanRecord> sorted =
                new ArrayList<>(loanHistory);

        sorted.sort(
                Comparator.<LoanRecord, Boolean>comparing(
                        loan -> !isOverdue(loan))
                .thenComparing(loan ->
                        dueDates.get(String.valueOf(
                                loan.getLoanNumber())))
                .thenComparingInt(LoanRecord::getLoanNumber)
        );

        return List.copyOf(sorted);
    }

    private boolean isOverdue(LoanRecord loan) {
        if (loan.isReturned()) {
            return false;
        }

        LocalDate due = dueDates.get(
                String.valueOf(loan.getLoanNumber()));

        return due != null && LocalDate.now().isAfter(due);
    }

    // Aggregate 1: Available and checked-out counts
    public int countAvailableBooks() {
        return (int) books.values().stream()
                .filter(LibraryBook::isAvailable)
                .count();
    }

    public int countCheckedOutBooks() {
        return checkedOutISBNs.size();
    }

    // Aggregate 2: Active loans per membership type
    public Map<MembershipType, Long> activeLoansByType() {

        Map<MembershipType, Long> result =
                new EnumMap<>(MembershipType.class);

        for (MembershipType type : MembershipType.values()) {
            result.put(type, 0L);
        }

        for (LoanRecord loan : activeLoans.values()) {
            MembershipType type;

            if (loan.getMember() instanceof StudentMember) {
                type = MembershipType.STUDENT;
            } else if (loan.getMember() instanceof FacultyMember) {
                type = MembershipType.FACULTY;
            } else {
                type = MembershipType.GUEST;
            }

            result.put(type, result.get(type) + 1);
        }

        return Map.copyOf(result);
    }

    // Aggregate 3: Estimated fines
    public double totalEstimatedFines() {

        double total = 0;

        for (LoanRecord loan : activeLoans.values()) {
            LocalDate due = dueDates.get(
                    String.valueOf(loan.getLoanNumber()));

            long lateDays = Math.max(0,
                    java.time.temporal.ChronoUnit.DAYS.between(
                            due, LocalDate.now()));

            total += loan.calculateFine((int) lateDays);
        }

        return total;
    }
    
    public CirculationSummary getCirculationSummary() {

        return new CirculationSummary(
                countAvailableBooks(),
                countCheckedOutBooks(),
                activeLoans.size(),
                pendingReservations.size(),
                totalEstimatedFines()
        );
    }

}
