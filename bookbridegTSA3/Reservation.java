
package bookbridge.model;

public class Reservation {

    private final Member member;
    private final LibraryBook book;
    private boolean active;

    public Reservation(Member member, LibraryBook book) {
        if (member == null || book == null) {
            throw new IllegalArgumentException(
                    "Member and book cannot be null.");
        }

        this.member = member;
        this.book = book;
        this.active = true;
    }

    public Member getMember() {
        return member;
    }

    public LibraryBook getBook() {
        return book;
    }

    public boolean isActive() {
        return active;
    }

    public void cancel() {
        if (!active) {
            throw new IllegalStateException(
                    "Reservation is already cancelled.");
        }

        active = false;
    }
}
