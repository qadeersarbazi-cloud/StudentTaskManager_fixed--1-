package project;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/** One assignment: ID, name, subject, deadline and priority (1 = High, 2 = Medium, 3 = Low). */
public class Assignment {
    private final int id;
    private String name;
    private String subject;
    private LocalDate deadline;
    private int priority;
    private boolean completed;

    public Assignment(int id, String name, String subject, LocalDate deadline, int priority) {
        this.id = id;
        this.name = name;
        this.subject = subject;
        this.deadline = deadline;
        this.priority = priority;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public String getSubject() { return subject; }
    public LocalDate getDeadline() { return deadline; }
    public int getPriority() { return priority; }
    public boolean isCompleted() { return completed; }

    public void setName(String name) { this.name = name; }
    public void setSubject(String subject) { this.subject = subject; }
    public void setDeadline(LocalDate deadline) { this.deadline = deadline; }
    public void setPriority(int priority) { this.priority = priority; }
    public void setCompleted(boolean completed) { this.completed = completed; }

    public String priorityName() {
        return priority == 1 ? "High" : priority == 2 ? "Medium" : "Low";
    }

    /** Days from today until the deadline (negative = already passed). */
    public long daysLeft() {
        return ChronoUnit.DAYS.between(LocalDate.now(), deadline);
    }

    /** A completed assignment is never overdue. */
    public boolean isOverdue() {
        return !completed && daysLeft() < 0;
    }

    /** Pending and due within the next 3 days (including today). */
    public boolean isDueSoon() {
        return !completed && daysLeft() >= 0 && daysLeft() <= 3;
    }

    public String statusText() {
        if (completed) return "Completed";
        long d = daysLeft();
        if (d < 0) return "OVERDUE by " + (-d) + (d == -1 ? " day" : " days");
        if (d == 0) return "Due today";
        if (d == 1) return "Due tomorrow";
        return "Due in " + d + " days";
    }

    public static String header() {
        return String.format("%-5s %-28s %-20s %-12s %-9s %s",
                "ID", "Name", "Subject", "Deadline", "Priority", "Status");
    }

    @Override
    public String toString() {
        return String.format("%-5d %-28s %-20s %-12s %-9s %s",
                id, name, subject, deadline, priorityName(), statusText());
    }
}
