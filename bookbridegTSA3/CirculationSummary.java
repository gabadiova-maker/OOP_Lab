package bookbridge.model;

public record CirculationSummary(
        int availableBooks,
        int checkedOutBooks,
        int activeLoans,
        int pendingReservations,
        double estimatedFines) {
}
