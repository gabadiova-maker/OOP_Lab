
package bookbridge.model;

public class LibraryBook {

    private final String bookId;
    private final String title;
    private boolean available;

    public LibraryBook(String bookId, String title) {
        if (bookId == null || bookId.isBlank()
                || title == null || title.isBlank()) {
            throw new IllegalArgumentException(
                    "Book ID and title cannot be blank.");
        }

        this.bookId = bookId;
        this.title = title;
        this.available = true;
    }

    public String getBookId() {
        return bookId;
    }

    public String getTitle() {
        return title;
    }

    public boolean isAvailable() {
        return available;
    }

    public void markBorrowed() {
        if (!available) {
            throw new IllegalStateException(
                    "Book is already borrowed.");
        }
        available = false;
    }

    public void markReturned() {
        if (available) {
            throw new IllegalStateException(
                    "Book is not currently borrowed.");
        }
        available = true;
    }
}
