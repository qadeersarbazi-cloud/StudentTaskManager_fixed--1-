package project;

import java.time.LocalDate;
import java.util.*;

/**
 * Core logic.
 *  - ArrayList     : main storage (add, edit, delete, display, overdue)
 *  - PriorityQueue : two heaps, so the most urgent and the highest priority assignment
 *                    can be read in O(1) without sorting the whole list
 */
public class TaskManager {

    /** Heap 1: earliest deadline first (ties: higher priority, then ID). */
    private static final Comparator<Assignment> URGENCY =
            Comparator.comparing(Assignment::getDeadline)
                      .thenComparingInt(Assignment::getPriority)
                      .thenComparingInt(Assignment::getId);

    /** Heap 2: highest priority first (ties: earliest deadline, then ID). */
    private static final Comparator<Assignment> PRIORITY =
            Comparator.comparingInt(Assignment::getPriority)
                      .thenComparing(Assignment::getDeadline)
                      .thenComparingInt(Assignment::getId);
    private final List<Assignment> list = new ArrayList<>();
    private final PriorityQueue<Assignment> urgentHeap = new PriorityQueue<>(URGENCY);
    private final PriorityQueue<Assignment> priorityHeap = new PriorityQueue<>(PRIORITY);
    private int nextId = 1;

    // ---------- add / edit / delete ----------

    public Assignment add(String name, String subject, LocalDate deadline, int priority) {
        return addWithId(nextId, name, subject, deadline, priority);
    }

    /** Used by add() and by file loading, so IDs stored in the file are kept. */
    public Assignment addWithId(int id, String name, String subject, LocalDate deadline, int priority) {
        return addWithId(id, name, subject, deadline, priority, false);
    }

    public Assignment addWithId(int id, String name, String subject, LocalDate deadline, int priority,
                                boolean completed) {
        if (find(id) != null) throw new IllegalArgumentException("Duplicate ID: " + id);
        Assignment a = new Assignment(id, name, subject, deadline, priority);
        a.setCompleted(completed);
        list.add(a);
        if (!completed) {          // heaps only hold PENDING assignments
            urgentHeap.offer(a);
            priorityHeap.offer(a);
        }
        nextId = Math.max(nextId, id + 1);
        return a;
    }

    public boolean update(int id, String name, String subject, LocalDate deadline, int priority) {
        Assignment a = find(id);
        if (a == null) return false;
        // Remove from the heaps BEFORE changing fields, otherwise the heap order breaks.
        urgentHeap.remove(a);
        priorityHeap.remove(a);
        a.setName(name);
        a.setSubject(subject);
        a.setDeadline(deadline);
        a.setPriority(priority);
        if (!a.isCompleted()) {
            urgentHeap.offer(a);
            priorityHeap.offer(a);
        }
        return true;
    }

    /** Marks an assignment as completed (or back to pending). Returns false if the ID does not exist. */
    public boolean setCompleted(int id, boolean completed) {
        Assignment a = find(id);
        if (a == null) return false;
        if (a.isCompleted() == completed) return true;
        a.setCompleted(completed);
        if (completed) {
            urgentHeap.remove(a);
            priorityHeap.remove(a);
        } else {
            urgentHeap.offer(a);
            priorityHeap.offer(a);
        }
        return true;
    }

    public boolean delete(int id) {
        Assignment a = find(id);
        if (a == null) return false;
        list.remove(a);
        urgentHeap.remove(a);
        priorityHeap.remove(a);
        return true;
    }

    /** Looks an assignment up by ID (needed so edit and delete know which one you mean). */
    public Assignment find(int id) {
        for (Assignment a : list) {
            if (a.getId() == id) return a;
        }
        return null;
    }

    // ---------- views ----------

    public List<Assignment> getAll() { return Collections.unmodifiableList(list); }

    public int size() { return list.size(); }

    /** All assignments whose deadline has already passed. */
    public List<Assignment> getOverdue() {
        List<Assignment> result = new ArrayList<>();
        for (Assignment a : list) {
            if (a.isOverdue()) result.add(a);
        }
        return result;
    }

    public int countCompleted() {
        int c = 0;
        for (Assignment a : list) if (a.isCompleted()) c++;
        return c;
    }

    /** Most urgent pending assignment = earliest deadline. peek() is O(1). */
    public Assignment mostUrgent() { return urgentHeap.peek(); }

    /** Highest priority pending assignment (1 = High). peek() is O(1). */
    public Assignment highestPriority() { return priorityHeap.peek(); }
}
