package campusconnect.gui;

import campusconnect.service.CampusConnectService;
import campusconnect.model.*;
import java.awt.*;
import java.util.List;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.concurrent.*;
import javax.swing.*;
import javax.swing.table.*;

public class CampusConnectGUI extends JFrame {

    private final JTextField studentIdField = new JTextField(12);
    private final JTextField studentNameField = new JTextField(12);
    private final JTextField concernField = new JTextField(12);
    private final JTextField ticketField = new JTextField(8);

    private final JComboBox<String> categoryBox = new JComboBox<>(
            new String[]{"ENROLLMENT", "DOCUMENT", "PAYMENT", "ADVISING"});

    private final JComboBox<Integer> priorityBox = new JComboBox<>(
            new Integer[]{1, 2, 3, 4, 5});

    private final JComboBox<String> statusBox = new JComboBox<>(
            new String[]{"IN_PROGRESS", "COMPLETED"});

    private final JComboBox<String> filterCategory = new JComboBox<>(
            new String[]{"ALL", "ENROLLMENT", "DOCUMENT", "PAYMENT", "ADVISING"});

    private final JComboBox<String> filterStatus = new JComboBox<>(
            new String[]{"ALL", "PENDING", "IN_PROGRESS", "COMPLETED", "CANCELLED"});

    private final DefaultTableModel tableModel = new DefaultTableModel(
            new String[]{"Ticket", "Student ID", "Name", "Category",
                "Priority", "Concern", "Days", "Status"}, 0) {

        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };

    private final JTable table = new JTable(tableModel);
    private final JLabel statusLabel = new JLabel("Ready");

    private final JButton addButton = new JButton("Add");
    private final JButton searchButton = new JButton("Search");
    private final JButton updateButton = new JButton("Update Status");
    private final JButton deleteButton = new JButton("Delete");
    private final JButton refreshButton = new JButton("Refresh");
    private final JButton processButton = new JButton("Process Next");
    private final JButton reportButton = new JButton("Summary Report");

    private final CampusConnectService service =
        new CampusConnectService();

    private List<String[]> allRecords = new ArrayList<>();

    public CampusConnectGUI() {
        setTitle("CampusConnect - Student Service Management");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1150, 700);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        JPanel fields = new JPanel(new GridLayout(7, 2, 8, 8));

        fields.add(new JLabel("Student ID:"));
        fields.add(studentIdField);

        fields.add(new JLabel("Student Name:"));
        fields.add(studentNameField);

        fields.add(new JLabel("Category:"));
        fields.add(categoryBox);

        fields.add(new JLabel("Priority:"));
        fields.add(priorityBox);

        fields.add(new JLabel("Concern:"));
        fields.add(concernField);

        fields.add(new JLabel("Ticket ID:"));
        fields.add(ticketField);

        fields.add(new JLabel("New Status:"));
        fields.add(statusBox);

        JPanel buttons = new JPanel(new FlowLayout());
        buttons.add(addButton);
        buttons.add(searchButton);
        buttons.add(updateButton);
        buttons.add(deleteButton);
        buttons.add(refreshButton);
        buttons.add(processButton);
        buttons.add(reportButton);

        JPanel filters = new JPanel(new FlowLayout());
        filters.add(new JLabel("Filter Category:"));
        filters.add(filterCategory);
        filters.add(new JLabel("Filter Status:"));
        filters.add(filterStatus);

        JPanel controls = new JPanel(new GridLayout(2, 1));
        controls.add(buttons);
        controls.add(filters);

        JPanel top = new JPanel(new BorderLayout(8, 8));
        top.add(fields, BorderLayout.NORTH);
        top.add(controls, BorderLayout.SOUTH);

        add(top, BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);
        add(statusLabel, BorderLayout.SOUTH);

        table.setAutoCreateRowSorter(true);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()
                    && table.getSelectedRow() >= 0) {

                int row = table.convertRowIndexToModel(
                        table.getSelectedRow());

                ticketField.setText(
                        tableModel.getValueAt(row, 0).toString());
            }
        });

        addButton.addActionListener(e -> addRequest());
        searchButton.addActionListener(e -> searchRequest());
        updateButton.addActionListener(e -> updateStatus());
        deleteButton.addActionListener(e -> deleteRequest());
        refreshButton.addActionListener(e -> refreshTable());
        processButton.addActionListener(e -> processNext());
        reportButton.addActionListener(e -> showReport());

        filterCategory.addActionListener(e -> applyFilters());
        filterStatus.addActionListener(e -> applyFilters());

        refreshTable();
    }

    private void setButtonsEnabled(boolean enabled) {
        addButton.setEnabled(enabled);
        searchButton.setEnabled(enabled);
        updateButton.setEnabled(enabled);
        deleteButton.setEnabled(enabled);
        refreshButton.setEnabled(enabled);
        processButton.setEnabled(enabled);
        reportButton.setEnabled(enabled);
    }

    private void runDatabaseTask(
            String busyMessage,
            Callable<String> task,
            boolean refreshAfter) {

        setButtonsEnabled(false);
        statusLabel.setText(busyMessage);

        new SwingWorker<String, Void>() {

            @Override
            protected String doInBackground() throws Exception {
                return task.call();
            }

            @Override
            protected void done() {
                try {
                    String message = get();

                    if (refreshAfter) {
                        refreshTable(message);
                    } else {
                        statusLabel.setText(message);
                        setButtonsEnabled(true);
                    }

                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    statusLabel.setText("Operation interrupted.");
                    setButtonsEnabled(true);

                } catch (ExecutionException ex) {
                    Throwable cause = ex.getCause();
                    statusLabel.setText("Operation failed.");
                    setButtonsEnabled(true);

                    JOptionPane.showMessageDialog(
                            CampusConnectGUI.this,
                            cause == null ? ex.getMessage()
                                    : cause.getMessage(),
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    private void addRequest() {
        String id = studentIdField.getText().trim();
        String name = studentNameField.getText().trim();
        String concern = concernField.getText().trim();

        if (id.isEmpty() || name.isEmpty() || concern.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Complete all required fields.");
            return;
        }

        Student student = new Student(id, name);
        int priority = (Integer) priorityBox.getSelectedItem();
        String category = (String) categoryBox.getSelectedItem();

        ServiceRequest request = switch (category) {
            case "ENROLLMENT" ->
                new EnrollmentRequest(student, priority, concern);
            case "DOCUMENT" ->
                new DocumentRequest(student, priority, concern);
            case "PAYMENT" ->
                new PaymentConcern(student, priority, concern);
            case "ADVISING" ->
                new AdvisingRequest(student, priority, concern);
            default ->
                throw new IllegalArgumentException("Invalid category.");
        };

        runDatabaseTask("Saving request...", () -> {
            service.addRequest(request);
            return "Request saved successfully.";
        }, true);
    }

    private void searchRequest() {
        String ticket = ticketField.getText().trim();

        if (ticket.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Enter a ticket ID.");
            return;
        }

        runDatabaseTask("Searching...", () -> {
            String[] row = service.findById(ticket);

            return row == null
                    ? "Ticket not found."
                    : "Found ticket " + row[0] + " for "
                    + row[2] + " (" + row[7] + ")";
        }, false);
    }

    private void updateStatus() {
        String ticket = ticketField.getText().trim();
        String status = (String) statusBox.getSelectedItem();

        if (ticket.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Enter a ticket ID.");
            return;
        }

        runDatabaseTask("Updating...", () ->
                service.updateStatus(ticket, status)
                        ? "Ticket status updated."
                        : "Ticket not found or invalid status transition.",
                true);
    }

    private void deleteRequest() {
        String ticket = ticketField.getText().trim();

        if (ticket.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Enter a ticket ID.");
            return;
        }

        int choice = JOptionPane.showConfirmDialog(
                this, "Delete ticket " + ticket + "?",
                "Confirm Delete", JOptionPane.YES_NO_OPTION);

        if (choice != JOptionPane.YES_OPTION) {
            return;
        }

        runDatabaseTask("Deleting...", () ->
                service.deleteRequest(ticket)
                        ? "Ticket deleted."
                        : "Ticket not found.", true);
    }

    private void processNext() {
        runDatabaseTask("Processing next...", () -> {
            String[] row = service.processNext();

            return row == null
                    ? "No pending requests."
                    : "Processing ticket " + row[0];
        }, true);
    }

    private void refreshTable() {
        refreshTable(null);
    }

    private void refreshTable(String finalMessage) {
        setButtonsEnabled(false);
        statusLabel.setText("Loading records...");

        new SwingWorker<List<String[]>, Void>() {

            @Override
            protected List<String[]> doInBackground() throws Exception {
                return service.findAll();
            }

            @Override
            protected void done() {
                try {
                    allRecords = get();
                    applyFilters();

                    if (finalMessage != null) {
                        statusLabel.setText(finalMessage);
                    }

                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    statusLabel.setText("Refresh interrupted.");

                } catch (ExecutionException ex) {
                    Throwable cause = ex.getCause();
                    statusLabel.setText("Database error.");

                    JOptionPane.showMessageDialog(
                            CampusConnectGUI.this,
                            cause == null ? ex.getMessage()
                                    : cause.getMessage(),
                            "Database Error", JOptionPane.ERROR_MESSAGE);

                } finally {
                    setButtonsEnabled(true);
                }
            }
        }.execute();
    }

    private void applyFilters() {
        String category = (String) filterCategory.getSelectedItem();
        String status = (String) filterStatus.getSelectedItem();

        if (category == null || status == null) {
            return;
        }

        List<String[]> filtered = new ArrayList<>();

        for (String[] row : allRecords) {
            boolean categoryMatch = category.equals("ALL")
                    || category.equals(row[3]);

            boolean statusMatch = status.equals("ALL")
                    || status.equals(row[7]);

            if (categoryMatch && statusMatch) {
                filtered.add(row);
            }
        }

        filtered.sort(
                Comparator.<String[]>comparingInt(
                        row -> Integer.parseInt(row[4])).reversed()
                .thenComparingInt(row -> Integer.parseInt(row[6]))
                .thenComparingLong(row -> Long.parseLong(row[0]))
        );

        tableModel.setRowCount(0);

        for (String[] row : filtered) {
            tableModel.addRow(row);
        }

        statusLabel.setText("Showing " + filtered.size()
                + " of " + allRecords.size() + " requests.");
    }

    private void showReport() {
        if (allRecords.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No records to report.");
            return;
        }

        int pending = 0;
        int inProgress = 0;
        int completed = 0;
        int cancelled = 0;

        int enrollment = 0;
        int document = 0;
        int payment = 0;
        int advising = 0;

        int totalDays = 0;

        for (String[] row : allRecords) {
            switch (row[7]) {
                case "PENDING" -> pending++;
                case "IN_PROGRESS" -> inProgress++;
                case "COMPLETED" -> completed++;
                case "CANCELLED" -> cancelled++;
            }

            switch (row[3]) {
                case "ENROLLMENT" -> enrollment++;
                case "DOCUMENT" -> document++;
                case "PAYMENT" -> payment++;
                case "ADVISING" -> advising++;
            }

            totalDays += Integer.parseInt(row[6]);
        }

        double average = (double) totalDays / allRecords.size();

        String report = "=== CampusConnect Summary ===\n"
                + "Total Requests: " + allRecords.size() + "\n"
                + "Pending: " + pending + "\n"
                + "In Progress: " + inProgress + "\n"
                + "Completed: " + completed + "\n"
                + "Cancelled: " + cancelled + "\n\n"
                + "Enrollment: " + enrollment + "\n"
                + "Document: " + document + "\n"
                + "Payment: " + payment + "\n"
                + "Advising: " + advising + "\n\n"
                + String.format("Average Estimated Days: %.2f", average);

        JOptionPane.showMessageDialog(
                this, report, "Summary Report",
                JOptionPane.INFORMATION_MESSAGE);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() ->
                new CampusConnectGUI().setVisible(true));
    }
}
