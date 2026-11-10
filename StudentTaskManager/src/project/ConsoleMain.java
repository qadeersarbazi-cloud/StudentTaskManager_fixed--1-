package project;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Scanner;

/** Console version (the GUI is in Main). Run: java -cp out project.ConsoleMain */
public class ConsoleMain {
    private static final Scanner in = new Scanner(System.in);
    private static final TaskManager manager = new TaskManager();
    private static final FileStorage storage = new FileStorage("assignments.txt");

    public static void main(String[] args) {
        try {
            runMenu();
        } catch (java.util.NoSuchElementException | IllegalStateException e) {
            System.out.println("\nNo keyboard input available. Run this in a real terminal, or use the GUI: java -cp out project.Main");
        }
    }

    private static void runMenu() {
        try {
            int n = storage.load(manager);
            System.out.println("Loaded " + n + " assignment(s) from " + storage.path());
        } catch (IOException e) {
            System.out.println("Could not read file: " + e.getMessage());
        }

        boolean running = true;
        while (running) {
            printMenu();
            switch (readInt("Choose: ")) {
                case 1: addAssignment(); break;
                case 2: editAssignment(); break;
                case 3: deleteAssignment(); break;
                case 4: show(manager.getAll()); break;
                case 5: showOverdue(); break;
                case 6: showOne("Most urgent assignment (earliest deadline)", manager.mostUrgent()); break;
                case 7: showOne("Highest priority assignment", manager.highestPriority()); break;
                case 8: completeAssignment(); break;
                case 9: Main.main(new String[0]); System.out.println("GUI opened in a new window."); break;
                case 0: running = false; break;
                default: System.out.println("Invalid choice.");
            }
        }
        System.out.println("Goodbye!");
    }

    private static void printMenu() {
        System.out.println("\n===== Student Task Manager =====");
        System.out.println("1. Add new assignment");
        System.out.println("2. Edit assignment");
        System.out.println("3. Delete assignment");
        System.out.println("4. View all assignments");
        System.out.println("5. View overdue assignments");
        System.out.println("6. Show most urgent assignment");
        System.out.println("7. Show highest priority assignment");
        System.out.println("8. Mark assignment as completed / pending");
        System.out.println("9. Open the GUI window");
        System.out.println("0. Exit");
    }

    // ---------- add / edit / delete ----------

    private static void addAssignment() {
        String name = readText("Name: ");
        String subject = readText("Subject: ");
        LocalDate deadline = readDate("Deadline (yyyy-MM-dd): ");
        int priority = readPriority();
        Assignment a = manager.add(name, subject, deadline, priority);
        save();
        System.out.println("Added with ID " + a.getId());
    }

    private static void editAssignment() {
        if (manager.size() == 0) { System.out.println("Nothing to edit yet."); return; }
        show(manager.getAll());
        int id = readInt("ID to edit: ");
        Assignment a = manager.find(id);
        if (a == null) { System.out.println("No assignment with ID " + id); return; }

        System.out.println("(press Enter to keep the current value)");
        String name = readOptional("Name [" + a.getName() + "]: ", a.getName());
        String subject = readOptional("Subject [" + a.getSubject() + "]: ", a.getSubject());

        LocalDate deadline = a.getDeadline();
        String d = readOptional("Deadline [" + a.getDeadline() + "]: ", "");
        if (!d.isEmpty()) {
            try { deadline = LocalDate.parse(d); }
            catch (DateTimeParseException e) { System.out.println("Bad date, keeping the old one."); }
        }

        int priority = a.getPriority();
        String p = readOptional("Priority 1-3 [" + a.getPriority() + "]: ", "");
        if (p.equals("1") || p.equals("2") || p.equals("3")) priority = Integer.parseInt(p);

        manager.update(id, name, subject, deadline, priority);
        save();
        System.out.println("Updated.");
    }

    private static void deleteAssignment() {
        if (manager.size() == 0) { System.out.println("Nothing to delete yet."); return; }
        show(manager.getAll());
        int id = readInt("ID to delete: ");
        Assignment a = manager.find(id);
        if (a == null) { System.out.println("No assignment with ID " + id); return; }
        System.out.print("Delete \"" + a.getName() + "\"? (y/n): ");
        if (in.nextLine().trim().equalsIgnoreCase("y")) {
            manager.delete(id);
            save();
            System.out.println("Deleted.");
        } else {
            System.out.println("Cancelled.");
        }
    }

    private static void completeAssignment() {
        if (manager.size() == 0) { System.out.println("Nothing here yet."); return; }
        show(manager.getAll());
        int id = readInt("ID to mark: ");
        Assignment a = manager.find(id);
        if (a == null) { System.out.println("No assignment with ID " + id); return; }
        manager.setCompleted(id, !a.isCompleted());
        save();
        System.out.println(a.isCompleted() ? "Marked as COMPLETED." : "Moved back to pending.");
    }

    private static void showOverdue() {
        List<Assignment> overdue = manager.getOverdue();
        if (overdue.isEmpty()) System.out.println("No overdue assignments. Well done!");
        else {
            System.out.println(overdue.size() + " overdue assignment(s):");
            show(overdue);
        }
    }

    // ---------- file ----------

    private static void save() {
        try {
            storage.save(manager.getAll());
        } catch (IOException e) {
            System.out.println("Could not save: " + e.getMessage());
        }
    }

    // ---------- display helpers ----------

    private static void show(List<Assignment> items) {
        if (items.isEmpty()) { System.out.println("(no assignments)"); return; }
        System.out.println(Assignment.header());
        System.out.println(new String(new char[95]).replace('\0', '-'));
        for (Assignment a : items) System.out.println(a);
    }

    private static void showOne(String title, Assignment a) {
        if (a == null) { System.out.println("No assignments yet."); return; }
        System.out.println(title + ":");
        System.out.println(Assignment.header());
        System.out.println(a);
    }

    // ---------- input helpers ----------

    private static String readText(String prompt) {
        while (true) {
            System.out.print(prompt);
            String s = in.nextLine().trim();
            if (!s.isEmpty()) return s;
            System.out.println("Cannot be empty.");
        }
    }

    private static String readOptional(String prompt, String fallback) {
        System.out.print(prompt);
        String s = in.nextLine().trim();
        return s.isEmpty() ? fallback : s;
    }

    private static int readInt(String prompt) {
        while (true) {
            System.out.print(prompt);
            try { return Integer.parseInt(in.nextLine().trim()); }
            catch (NumberFormatException e) { System.out.println("Enter a whole number."); }
        }
    }

    private static int readPriority() {
        while (true) {
            int p = readInt("Priority (1 = High, 2 = Medium, 3 = Low): ");
            if (p >= 1 && p <= 3) return p;
            System.out.println("Enter 1, 2 or 3.");
        }
    }

    private static LocalDate readDate(String prompt) {
        while (true) {
            System.out.print(prompt);
            try { return LocalDate.parse(in.nextLine().trim()); }
            catch (DateTimeParseException e) { System.out.println("Use format yyyy-MM-dd, e.g. 2026-12-31."); }
        }
    }
}
