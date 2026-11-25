
package bookbridge.gui;

import bookbridge.dao.*;
import java.awt.*;
import java.util.List;
import java.util.concurrent.*;
import javax.swing.*;
import javax.swing.table.*;

public class BookBridgeGUI extends JFrame {

    private final BookBridgeDAO dao = new BookBridgeDAO();

    private final JTextField memberId = new JTextField(12);
    private final JTextField memberName = new JTextField(12);
    private final JComboBox<String> memberType = new JComboBox<>(
            new String[]{"STUDENT", "FACULTY", "GUEST"});

    private final JTextField isbn = new JTextField(12);
    private final JTextField title = new JTextField(12);
    private final JTextField copies = new JTextField("1", 12);
    private final JTextField days = new JTextField("7", 12);
    private final JTextField loanId = new JTextField(12);
    private final JTextField reservationId = new JTextField(12);

    private final JLabel status = new JLabel("Ready");

    private final DefaultTableModel memberModel = createModel(
            "Member ID", "Name", "Type");
    private final DefaultTableModel bookModel = createModel(
            "ISBN", "Title", "Total", "Available");
    private final DefaultTableModel loanModel = createModel(
            "Loan ID", "Member ID", "ISBN", "Days", "Due Date", "Status");
    private final DefaultTableModel reservationModel = createModel(
            "Reservation ID", "Member ID", "ISBN", "Status");

    private final JTable memberTable = new JTable(memberModel);
    private final JTable bookTable = new JTable(bookModel);
    private final JTable loanTable = new JTable(loanModel);
    private final JTable reservationTable = new JTable(reservationModel);

    private final JButton addMember = new JButton("Add Member");
    private final JButton addBook = new JButton("Add Book");
    private final JButton borrow = new JButton("Borrow");
    private final JButton returnBook = new JButton("Return");
    private final JButton reserve = new JButton("Reserve");
    private final JButton cancel = new JButton("Cancel Reservation");
    private final JButton process = new JButton("Process Next");
    private final JButton refresh = new JButton("Refresh");
    private final JButton summary = new JButton("Summary");

    private final JButton[] buttons = {
        addMember, addBook, borrow, returnBook,
        reserve, cancel, process, refresh, summary
    };

    private static DefaultTableModel createModel(String... columns) {
        return new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };
    }

    public BookBridgeGUI() {
        setTitle("BookBridge - Library Management System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1150, 750);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(8, 8));

        JPanel fields = new JPanel(new GridLayout(9, 2, 8, 5));

        fields.add(new JLabel("Member ID:"));
        fields.add(memberId);
        fields.add(new JLabel("Member Name:"));
        fields.add(memberName);
        fields.add(new JLabel("Membership Type:"));
        fields.add(memberType);
        fields.add(new JLabel("Book ISBN:"));
        fields.add(isbn);
        fields.add(new JLabel("Book Title:"));
        fields.add(title);
        fields.add(new JLabel("Total Copies:"));
        fields.add(copies);
        fields.add(new JLabel("Loan Days:"));
        fields.add(days);
        fields.add(new JLabel("Loan ID:"));
        fields.add(loanId);
        fields.add(new JLabel("Reservation ID:"));
        fields.add(reservationId);

        JPanel controls = new JPanel(new FlowLayout());
        for (JButton button : buttons) {
            controls.add(button);
        }

        JPanel top = new JPanel(new BorderLayout());
        top.add(fields, BorderLayout.CENTER);
        top.add(controls, BorderLayout.SOUTH);

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Members", new JScrollPane(memberTable));
        tabs.addTab("Books", new JScrollPane(bookTable));
        tabs.addTab("Loans", new JScrollPane(loanTable));
        tabs.addTab("Reservations", new JScrollPane(reservationTable));

        for (JTable table : new JTable[]{
                memberTable, bookTable, loanTable, reservationTable}) {
            table.setAutoCreateRowSorter(true);
            table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        }

        memberTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && memberTable.getSelectedRow() >= 0) {
                int row = memberTable.convertRowIndexToModel(
                        memberTable.getSelectedRow());
                memberId.setText(memberModel.getValueAt(row, 0).toString());
            }
        });

        bookTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && bookTable.getSelectedRow() >= 0) {
                int row = bookTable.convertRowIndexToModel(
                        bookTable.getSelectedRow());
                isbn.setText(bookModel.getValueAt(row, 0).toString());
            }
        });

        loanTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && loanTable.getSelectedRow() >= 0) {
                int row = loanTable.convertRowIndexToModel(
                        loanTable.getSelectedRow());
                loanId.setText(loanModel.getValueAt(row, 0).toString());
            }
        });

        reservationTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()
                    && reservationTable.getSelectedRow() >= 0) {
                int row = reservationTable.convertRowIndexToModel(
                        reservationTable.getSelectedRow());
                reservationId.setText(
                        reservationModel.getValueAt(row, 0).toString());
            }
        });

        addMember.addActionListener(e -> runTask("Adding member...", () -> {
            dao.addMember(memberId.getText().trim(),
                    memberName.getText().trim(),
                    (String) memberType.getSelectedItem());
            return "Member saved.";
        }, true));

        addBook.addActionListener(e -> runTask("Adding book...", () -> {
            dao.addBook(isbn.getText().trim(), title.getText().trim(),
                    Integer.parseInt(copies.getText().trim()));
            return "Book saved.";
        }, true));

        borrow.addActionListener(e -> runTask("Borrowing...", () -> {
            int id = dao.borrowBook(memberId.getText().trim(),
                    isbn.getText().trim(),
                    Integer.parseInt(days.getText().trim()));
            return "Loan created: " + id;
        }, true));

        returnBook.addActionListener(e -> runTask("Returning...", () -> {
            double fine = dao.returnBook(
                    Integer.parseInt(loanId.getText().trim()));
            return String.format("Returned. Fine: PHP %.2f", fine);
        }, true));

        reserve.addActionListener(e -> runTask("Reserving...", () -> {
            int id = dao.reserveBook(memberId.getText().trim(),
                    isbn.getText().trim());
            return "Reservation created: " + id;
        }, true));

        cancel.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(this,
                    "Cancel reservation " + reservationId.getText() + "?",
                    "Confirm Cancellation", JOptionPane.YES_NO_OPTION);

            if (confirm == JOptionPane.YES_OPTION) {
                runTask("Cancelling...", () ->
                        dao.cancelReservation(Integer.parseInt(
                                reservationId.getText().trim()))
                                ? "Reservation cancelled."
                                : "Reservation not found or already processed.",
                        true);
            }
        });

        process.addActionListener(e -> runTask("Processing queue...", () -> {
            Integer id = dao.processNextReservation();
            return id == null
                    ? "No processable reservation."
                    : "Processed reservation " + id;
        }, true));

        refresh.addActionListener(e -> refreshTables(null));

        summary.addActionListener(e -> runTask("Preparing summary...", () -> {
            List<String[]> books = dao.getAllBooks();
            List<String[]> members = dao.getAllMembers();
            List<String[]> loans = dao.getAllLoans();
            List<String[]> reservations = dao.getAllReservations();

            long active = loans.stream()
                    .filter(r -> "ACTIVE".equals(r[5])).count();
            long returned = loans.stream()
                    .filter(r -> "RETURNED".equals(r[5])).count();
            long pending = reservations.stream()
                    .filter(r -> "PENDING".equals(r[3])).count();

            return "Members: " + members.size()
                    + " | Books: " + books.size()
                    + " | Active Loans: " + active
                    + " | Returned Loans: " + returned
                    + " | Pending Reservations: " + pending;
        }, false));

        add(top, BorderLayout.NORTH);
        add(tabs, BorderLayout.CENTER);
        add(status, BorderLayout.SOUTH);

        refreshTables(null);
    }

    private void setBusy(boolean busy) {
        for (JButton button : buttons) {
            button.setEnabled(!busy);
        }
    }

    private void runTask(String message, Callable<String> task,
            boolean refreshAfter) {

        setBusy(true);
        status.setText(message);

        new SwingWorker<String, Void>() {
            @Override
            protected String doInBackground() throws Exception {
                return task.call();
            }

            @Override
            protected void done() {
                try {
                    String result = get();

                    if (refreshAfter) {
                        refreshTables(result);
                    } else {
                        status.setText(result);
                        setBusy(false);
                    }

                } catch (Exception ex) {
                    setBusy(false);
                    Throwable cause = ex instanceof ExecutionException
                            ? ex.getCause() : ex;

                    status.setText("Operation failed.");
                    JOptionPane.showMessageDialog(
                            BookBridgeGUI.this,
                            cause.getMessage(),
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    private void refreshTables(String finalMessage) {
        setBusy(true);
        status.setText("Loading records...");

        new SwingWorker<List<List<String[]>>, Void>() {
            @Override
            protected List<List<String[]>> doInBackground()
                    throws Exception {
                return List.of(
                        dao.getAllMembers(),
                        dao.getAllBooks(),
                        dao.getAllLoans(),
                        dao.getAllReservations()
                );
            }

            @Override
            protected void done() {
                try {
                    List<List<String[]>> data = get();

                    DefaultTableModel[] models = {
                        memberModel, bookModel, loanModel, reservationModel
                    };

                    for (int i = 0; i < models.length; i++) {
                        models[i].setRowCount(0);
                        for (String[] row : data.get(i)) {
                            models[i].addRow(row);
                        }
                    }

                    status.setText(finalMessage == null
                            ? "Records loaded successfully."
                            : finalMessage);

                } catch (Exception ex) {
                    Throwable cause = ex instanceof ExecutionException
                            ? ex.getCause() : ex;

                    status.setText("Unable to load records.");
                    JOptionPane.showMessageDialog(
                            BookBridgeGUI.this,
                            cause.getMessage(),
                            "Database Error", JOptionPane.ERROR_MESSAGE);

                } finally {
                    setBusy(false);
                }
            }
        }.execute();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() ->
                new BookBridgeGUI().setVisible(true));
    }
}
