package project;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.font.TextAttribute;
import java.io.IOException;
import java.time.*;
import java.util.*;
import java.util.List;

/** Student Task Manager - Swing GUI. Run: java -cp out project.Main */
public class Main extends JFrame {

    // ---------- palette ----------
    static final Color BG = new Color(0xF3F5F9);
    static final Color CARD = Color.WHITE;
    static final Color BORDER = new Color(0xE3E7EF);
    static final Color INK = new Color(0x1F2937);
    static final Color MUTED = new Color(0x6B7280);
    static final Color ACCENT = new Color(0x4F46E5);
    static final Color ACCENT_DARK = new Color(0x3730A3);
    static final Color GREEN = new Color(0x15803D);
    static final Color RED = new Color(0xDC2626);
    static final Color AMBER = new Color(0xB45309);
    static final Color ROW_ALT = new Color(0xF9FAFC);
    static final Color SELECT = new Color(0xE0E7FF);

    static Font font(int style, float size) { return new Font(Font.SANS_SERIF, style, 0).deriveFont(style, size); }

    private final TaskManager manager = new TaskManager();
    private final FileStorage storage = new FileStorage("assignments.txt");

    private final TaskTableModel model = new TaskTableModel();
    private final JTable table = new JTable(model);
    private final TableRowSorter<TaskTableModel> sorter = new TableRowSorter<>(model);
    private final JTextField search = new JTextField();
    private final JComboBox<String> filter =
            new JComboBox<>(new String[]{"All", "Pending", "Completed", "Overdue", "Due soon"});
    private final JLabel totalV = new JLabel(), pendingV = new JLabel(), doneV = new JLabel(), overdueV = new JLabel();
    private final JLabel urgentV = new JLabel(), priorityV = new JLabel();
    private final JProgressBar progress = new JProgressBar(0, 100);
    private final JLabel statusBar = new JLabel(" ");
    private final JButton completeBtn = new JButton("Mark Completed");
    private final JButton editBtn = new JButton("Edit");
    private final JButton deleteBtn = new JButton("Delete");
    private final CardLayout cards = new CardLayout();
    private final JPanel center = new JPanel(cards);
    private final JLabel emptyLabel = new JLabel("", SwingConstants.CENTER);

    public Main() {
        super("Student Task Manager");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        getContentPane().setBackground(BG);
        setLayout(new BorderLayout());

        add(buildHeader(), BorderLayout.NORTH);

        JPanel body = new JPanel(new BorderLayout(0, 14));
        body.setOpaque(false);
        body.setBorder(new EmptyBorder(16, 24, 8, 24));
        body.add(buildTop(), BorderLayout.NORTH);
        body.add(buildTableCard(), BorderLayout.CENTER);
        add(body, BorderLayout.CENTER);

        statusBar.setFont(font(Font.PLAIN, 12));
        statusBar.setForeground(MUTED);
        statusBar.setBorder(new EmptyBorder(6, 24, 10, 24));
        add(statusBar, BorderLayout.SOUTH);

        loadData();
        refresh();
        wireEvents();

        setMinimumSize(new Dimension(900, 620));
        setSize(1100, 720);
        setLocationRelativeTo(null);
    }

    // ================= UI construction =================

    private JComponent buildHeader() {
        JPanel h = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setPaint(new GradientPaint(0, 0, new Color(0x312E81), getWidth(), 0, new Color(0x4F46E5)));
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        };
        h.setBorder(new EmptyBorder(18, 24, 18, 24));

        JPanel titles = new JPanel();
        titles.setOpaque(false);
        titles.setLayout(new BoxLayout(titles, BoxLayout.Y_AXIS));
        JLabel t = new JLabel("Student Task Manager");
        t.setFont(font(Font.BOLD, 22));
        t.setForeground(Color.WHITE);
        JLabel s = new JLabel("Plan it. Finish it. Tick it off.");
        s.setFont(font(Font.PLAIN, 13));
        s.setForeground(new Color(0xC7D2FE));
        titles.add(t);
        titles.add(Box.createVerticalStrut(2));
        titles.add(s);
        h.add(titles, BorderLayout.WEST);

        JLabel date = new JLabel(LocalDate.now().getDayOfWeek().toString().charAt(0)
                + LocalDate.now().getDayOfWeek().toString().substring(1).toLowerCase()
                + ", " + LocalDate.now().getDayOfMonth() + " "
                + LocalDate.now().getMonth().toString().charAt(0)
                + LocalDate.now().getMonth().toString().substring(1).toLowerCase()
                + " " + LocalDate.now().getYear());
        date.setFont(font(Font.PLAIN, 13));
        date.setForeground(Color.WHITE);
        h.add(date, BorderLayout.EAST);
        return h;
    }

    private JComponent buildTop() {
        JPanel top = new JPanel();
        top.setOpaque(false);
        top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));

        // stat cards
        JPanel stats = new JPanel(new GridLayout(1, 4, 14, 0));
        stats.setOpaque(false);
        stats.add(statCard("TOTAL", totalV, ACCENT));
        stats.add(statCard("PENDING", pendingV, AMBER));
        stats.add(statCard("COMPLETED", doneV, GREEN));
        stats.add(statCard("OVERDUE", overdueV, RED));
        top.add(stats);
        top.add(Box.createVerticalStrut(12));

        // insight card with progress
        JPanel insight = card(new BorderLayout(20, 0));
        insight.setBorder(new CompoundBorder(new LineBorder(BORDER, 1, true), new EmptyBorder(12, 16, 12, 16)));
        JPanel left = new JPanel(new GridLayout(2, 1, 0, 4));
        left.setOpaque(false);
        left.add(insightRow("Most urgent", urgentV));
        left.add(insightRow("Highest priority", priorityV));
        insight.add(left, BorderLayout.CENTER);

        JPanel pr = new JPanel(new BorderLayout(0, 4));
        pr.setOpaque(false);
        pr.setPreferredSize(new Dimension(260, 40));
        JLabel pl = new JLabel("Overall progress");
        pl.setFont(font(Font.BOLD, 12));
        pl.setForeground(MUTED);
        progress.setStringPainted(true);
        progress.setFont(font(Font.BOLD, 11));
        progress.setForeground(GREEN);
        progress.setBackground(new Color(0xE5E7EB));
        progress.setBorder(BorderFactory.createEmptyBorder());
        progress.setPreferredSize(new Dimension(100, 18));
        pr.add(pl, BorderLayout.NORTH);
        pr.add(progress, BorderLayout.CENTER);
        insight.add(pr, BorderLayout.EAST);
        top.add(insight);
        top.add(Box.createVerticalStrut(12));

        // toolbar
        JPanel bar = new JPanel(new BorderLayout(10, 0));
        bar.setOpaque(false);

        JPanel leftBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        leftBar.setOpaque(false);
        JButton add = primary("+  Add Assignment");
        add.addActionListener(e -> addAssignment());
        styleSecondary(completeBtn, GREEN);
        styleSecondary(editBtn, ACCENT);
        styleSecondary(deleteBtn, RED);
        completeBtn.addActionListener(e -> toggleSelected());
        editBtn.addActionListener(e -> editSelected());
        deleteBtn.addActionListener(e -> deleteSelected());
        leftBar.add(add);
        leftBar.add(completeBtn);
        leftBar.add(editBtn);
        leftBar.add(deleteBtn);
        bar.add(leftBar, BorderLayout.WEST);

        JPanel rightBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        rightBar.setOpaque(false);
        search.setPreferredSize(new Dimension(190, 34));
        search.setFont(font(Font.PLAIN, 13));
        search.setBorder(new CompoundBorder(new LineBorder(BORDER, 1, true), new EmptyBorder(0, 10, 0, 10)));
        search.putClientProperty("JTextField.placeholderText", "Search name or subject");
        search.setToolTipText("Search by name or subject");
        filter.setFont(font(Font.PLAIN, 13));
        filter.setPreferredSize(new Dimension(120, 34));
        rightBar.add(search);
        rightBar.add(filter);
        bar.add(rightBar, BorderLayout.EAST);
        top.add(bar);
        return top;
    }

    private JComponent buildTableCard() {
        table.setRowHeight(40);
        table.setShowVerticalLines(false);
        table.setShowHorizontalLines(true);
        table.setGridColor(new Color(0xEEF0F5));
        table.setIntercellSpacing(new Dimension(0, 1));
        table.setFont(font(Font.PLAIN, 13));
        table.setSelectionBackground(SELECT);
        table.setSelectionForeground(INK);
        table.setFillsViewportHeight(true);
        table.setRowSorter(sorter);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setFocusable(true);

        JTableHeader th = table.getTableHeader();
        th.setReorderingAllowed(false);
        th.setPreferredSize(new Dimension(0, 38));
        th.setDefaultRenderer(new HeaderRenderer());

        CellRenderer r = new CellRenderer();
        for (int i = 0; i < table.getColumnCount(); i++) table.getColumnModel().getColumn(i).setCellRenderer(r);
        table.getColumnModel().getColumn(0).setCellRenderer(new CheckRenderer());
        int[] widths = {50, 55, 280, 160, 110, 90, 160};
        for (int i = 0; i < widths.length; i++) table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        table.getColumnModel().getColumn(0).setMaxWidth(56);
        table.getColumnModel().getColumn(1).setMaxWidth(60);

        JScrollPane sp = new JScrollPane(table);
        sp.setBorder(BorderFactory.createEmptyBorder());
        sp.getViewport().setBackground(CARD);

        emptyLabel.setFont(font(Font.PLAIN, 15));
        emptyLabel.setForeground(MUTED);
        JPanel empty = new JPanel(new BorderLayout());
        empty.setBackground(CARD);
        empty.add(emptyLabel, BorderLayout.CENTER);

        center.setBackground(CARD);
        center.add(sp, "table");
        center.add(empty, "empty");

        JPanel c = card(new BorderLayout());
        c.add(center, BorderLayout.CENTER);
        return c;
    }

    private JPanel statCard(String title, JLabel value, Color accent) {
        JPanel p = card(new BorderLayout());
        p.setBorder(new CompoundBorder(new LineBorder(BORDER, 1, true), new EmptyBorder(12, 16, 12, 16)));
        JPanel bar = new JPanel();
        bar.setBackground(accent);
        bar.setPreferredSize(new Dimension(4, 10));
        JLabel t = new JLabel(title);
        t.setFont(font(Font.BOLD, 11));
        t.setForeground(MUTED);
        value.setFont(font(Font.BOLD, 28));
        value.setForeground(accent);
        JPanel inner = new JPanel(new BorderLayout());
        inner.setOpaque(false);
        inner.setBorder(new EmptyBorder(0, 12, 0, 0));
        inner.add(t, BorderLayout.NORTH);
        inner.add(value, BorderLayout.CENTER);
        p.add(bar, BorderLayout.WEST);
        p.add(inner, BorderLayout.CENTER);
        return p;
    }

    private JPanel insightRow(String label, JLabel value) {
        JPanel p = new JPanel(new BorderLayout(10, 0));
        p.setOpaque(false);
        JLabel l = new JLabel(label);
        l.setFont(font(Font.BOLD, 12));
        l.setForeground(MUTED);
        l.setPreferredSize(new Dimension(110, 18));
        value.setFont(font(Font.PLAIN, 13));
        value.setForeground(INK);
        p.add(l, BorderLayout.WEST);
        p.add(value, BorderLayout.CENTER);
        return p;
    }

    private static JPanel card(LayoutManager lm) {
        JPanel p = new JPanel(lm);
        p.setBackground(CARD);
        p.setBorder(new LineBorder(BORDER, 1, true));
        return p;
    }

    private static JButton primary(String text) {
        JButton b = new JButton(text);
        b.setFont(font(Font.BOLD, 13));
        b.setForeground(Color.WHITE);
        b.setBackground(ACCENT);
        b.setOpaque(true);
        b.setBorder(new EmptyBorder(9, 18, 9, 18));
        b.setFocusPainted(false);
        b.setContentAreaFilled(true);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { b.setBackground(ACCENT_DARK); }
            public void mouseExited(MouseEvent e) { b.setBackground(ACCENT); }
        });
        return b;
    }

    private static void styleSecondary(JButton b, Color c) {
        b.setFont(font(Font.BOLD, 13));
        b.setForeground(c);
        b.setBackground(Color.WHITE);
        b.setOpaque(true);
        b.setContentAreaFilled(true);
        b.setBorder(new CompoundBorder(new LineBorder(BORDER, 1, true), new EmptyBorder(8, 16, 8, 16)));
        b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    // ================= events =================

    private void wireEvents() {
        table.getSelectionModel().addListSelectionListener(e -> updateButtons());
        table.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && table.columnAtPoint(e.getPoint()) != 0
                        && table.rowAtPoint(e.getPoint()) >= 0) editSelected();
            }
        });
        table.getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), "del");
        table.getActionMap().put("del", new AbstractAction() {
            public void actionPerformed(ActionEvent e) { deleteSelected(); }
        });
        javax.swing.event.DocumentListener dl = new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { applyFilter(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { applyFilter(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { applyFilter(); }
        };
        search.getDocument().addDocumentListener(dl);
        filter.addActionListener(e -> applyFilter());
        updateButtons();
    }

    private void applyFilter() {
        final String q = search.getText().trim().toLowerCase();
        final String f = (String) filter.getSelectedItem();
        sorter.setRowFilter(new RowFilter<TaskTableModel, Integer>() {
            @Override public boolean include(Entry<? extends TaskTableModel, ? extends Integer> en) {
                Assignment a = model.get(en.getIdentifier());
                if (!q.isEmpty() && !a.getName().toLowerCase().contains(q)
                        && !a.getSubject().toLowerCase().contains(q)) return false;
                switch (f) {
                    case "Pending": return !a.isCompleted();
                    case "Completed": return a.isCompleted();
                    case "Overdue": return a.isOverdue();
                    case "Due soon": return a.isDueSoon();
                    default: return true;
                }
            }
        });
        updateEmptyState();
    }

    private Assignment selected() {
        int r = table.getSelectedRow();
        return r < 0 ? null : model.get(table.convertRowIndexToModel(r));
    }

    private void updateButtons() {
        Assignment a = selected();
        completeBtn.setEnabled(a != null);
        editBtn.setEnabled(a != null);
        deleteBtn.setEnabled(a != null);
        completeBtn.setText(a != null && a.isCompleted() ? "Mark Pending" : "Mark Completed");
    }

    private void addAssignment() {
        AssignmentDialog d = new AssignmentDialog(this, null, subjects());
        d.setVisible(true);
        if (d.ok) {
            Assignment a = manager.add(d.name(), d.subject(), d.deadline(), d.priority());
            persist();
            refresh();
            selectById(a.getId());
            flash("Added \"" + a.getName() + "\"");
        }
    }

    private void editSelected() {
        Assignment a = selected();
        if (a == null) return;
        AssignmentDialog d = new AssignmentDialog(this, a, subjects());
        d.setVisible(true);
        if (d.ok) {
            manager.update(a.getId(), d.name(), d.subject(), d.deadline(), d.priority());
            persist();
            refresh();
            selectById(a.getId());
            flash("Updated \"" + d.name() + "\"");
        }
    }

    private void toggleSelected() {
        Assignment a = selected();
        if (a == null) return;
        toggle(a);
    }

    private void toggle(Assignment a) {
        boolean nowDone = !a.isCompleted();
        manager.setCompleted(a.getId(), nowDone);
        persist();
        refresh();
        selectById(a.getId());
        flash(nowDone ? "Great job! \"" + a.getName() + "\" marked as completed."
                      : "\"" + a.getName() + "\" moved back to pending.");
    }

    private void deleteSelected() {
        Assignment a = selected();
        if (a == null) return;
        int ans = JOptionPane.showConfirmDialog(this, "Delete \"" + a.getName() + "\"?\nThis cannot be undone.",
                "Delete assignment", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (ans == JOptionPane.YES_OPTION) {
            manager.delete(a.getId());
            persist();
            refresh();
            flash("Deleted \"" + a.getName() + "\"");
        }
    }

    private List<String> subjects() {
        TreeSet<String> s = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        for (Assignment a : manager.getAll()) s.add(a.getSubject());
        return new ArrayList<>(s);
    }

    // ================= data =================

    private void loadData() {
        try {
            int n = storage.load(manager);
            statusBar.setText("Loaded " + n + " assignment(s) from " + storage.path());
        } catch (IOException e) {
            statusBar.setText("Could not read file: " + e.getMessage());
        }
    }

    private void persist() {
        try {
            storage.save(manager.getAll());
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Could not save: " + e.getMessage(), "Save error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void flash(String msg) { statusBar.setText(msg); }

    private void selectById(int id) {
        for (int i = 0; i < table.getRowCount(); i++) {
            if (model.get(table.convertRowIndexToModel(i)).getId() == id) {
                table.setRowSelectionInterval(i, i);
                table.scrollRectToVisible(table.getCellRect(i, 0, true));
                return;
            }
        }
        table.clearSelection();
    }

    private void refresh() {
        int selId = selected() == null ? -1 : selected().getId();
        List<Assignment> sorted = new ArrayList<>(manager.getAll());
        // pending first (by deadline), completed at the bottom
        sorted.sort(Comparator.comparing(Assignment::isCompleted)
                .thenComparing(Assignment::getDeadline)
                .thenComparingInt(Assignment::getPriority));
        model.setData(sorted);
        applyFilter();

        int total = manager.size(), done = manager.countCompleted();
        totalV.setText(String.valueOf(total));
        doneV.setText(String.valueOf(done));
        pendingV.setText(String.valueOf(total - done));
        overdueV.setText(String.valueOf(manager.getOverdue().size()));
        progress.setValue(total == 0 ? 0 : Math.round(100f * done / total));
        progress.setString(done + " of " + total + " done");

        Assignment u = manager.mostUrgent(), p = manager.highestPriority();
        urgentV.setText(u == null ? "-" : u.getName() + "  •  " + u.getSubject() + "  •  " + u.statusText());
        priorityV.setText(p == null ? "-" : p.getName() + "  •  " + p.getSubject() + "  •  "
                + p.priorityName() + " priority");
        if (selId >= 0) selectById(selId);
        updateButtons();
    }

    private void updateEmptyState() {
        if (table.getRowCount() == 0) {
            emptyLabel.setText(manager.size() == 0
                    ? "<html><div style='text-align:center'>No assignments yet<br><span style='font-size:11px'>"
                      + "Click \"Add Assignment\" to get started.</span></div></html>"
                    : "No assignments match your search / filter.");
            cards.show(center, "empty");
        } else {
            cards.show(center, "table");
        }
    }

    // ================= table model & renderers =================

    private class TaskTableModel extends AbstractTableModel {
        private final String[] cols = {"✓", "ID", "Assignment", "Subject", "Deadline", "Priority", "Status"};
        private List<Assignment> rows = new ArrayList<>();

        void setData(List<Assignment> r) { rows = r; fireTableDataChanged(); }
        Assignment get(int i) { return rows.get(i); }
        public int getRowCount() { return rows.size(); }
        public int getColumnCount() { return cols.length; }
        public String getColumnName(int c) { return cols[c]; }
        @Override public Class<?> getColumnClass(int c) { return c == 0 ? Boolean.class : String.class; }
        @Override public boolean isCellEditable(int r, int c) { return c == 0; }

        public Object getValueAt(int r, int c) {
            Assignment a = rows.get(r);
            switch (c) {
                case 0: return a.isCompleted();
                case 1: return String.valueOf(a.getId());
                case 2: return a.getName();
                case 3: return a.getSubject();
                case 4: return a.getDeadline().toString();
                case 5: return a.priorityName();
                default: return a.statusText();
            }
        }

        @Override public void setValueAt(Object v, int r, int c) {
            if (c != 0) return;
            Assignment a = rows.get(r);
            SwingUtilities.invokeLater(() -> toggle(a));
        }
    }

    private static class HeaderRenderer extends DefaultTableCellRenderer {
        HeaderRenderer() { setHorizontalAlignment(LEFT); }
        @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int r, int c) {
            super.getTableCellRendererComponent(t, v, false, false, r, c);
            setFont(font(Font.BOLD, 12));
            setForeground(MUTED);
            setBackground(new Color(0xF8F9FC));
            setHorizontalAlignment(c == 0 ? CENTER : LEFT);
            setBorder(new CompoundBorder(new MatteBorder(0, 0, 1, 0, BORDER), new EmptyBorder(0, 12, 0, 8)));
            return this;
        }
    }

    private class CellRenderer extends DefaultTableCellRenderer {
        @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foc, int row, int col) {
            super.getTableCellRendererComponent(t, v, sel, false, row, col);
            Assignment a = model.get(t.convertRowIndexToModel(row));
            setBorder(new EmptyBorder(0, 12, 0, 8));
            setBackground(sel ? SELECT : (row % 2 == 0 ? CARD : ROW_ALT));
            Font base = font(Font.PLAIN, 13);
            Color fg = a.isCompleted() ? new Color(0x9CA3AF) : INK;

            Map<TextAttribute, Object> attrs = new HashMap<>();
            if (col == 2) {
                base = font(Font.BOLD, 13);
                if (a.isCompleted()) attrs.put(TextAttribute.STRIKETHROUGH, TextAttribute.STRIKETHROUGH_ON);
            }
            if (col == 1) fg = a.isCompleted() ? fg : MUTED;
            if (col == 5) {
                String sym = a.priorityName();
                setText("●  " + sym);
                if (!a.isCompleted()) fg = a.getPriority() == 1 ? RED : a.getPriority() == 2 ? AMBER : GREEN;
                base = font(Font.BOLD, 12);
            }
            if (col == 6) {
                base = font(Font.BOLD, 12);
                if (a.isCompleted()) { fg = GREEN; setText("✔  Completed"); }
                else if (a.isOverdue()) fg = RED;
                else if (a.isDueSoon()) fg = AMBER;
                else fg = MUTED;
            }
            setFont(attrs.isEmpty() ? base : base.deriveFont(attrs));
            setForeground(fg);
            setToolTipText(col == 2 ? a.getName() : null);
            return this;
        }
    }

    private class CheckRenderer extends JCheckBox implements javax.swing.table.TableCellRenderer {
        CheckRenderer() { setHorizontalAlignment(CENTER); setOpaque(true); setBorderPainted(false); }
        public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foc, int row, int col) {
            setSelected(Boolean.TRUE.equals(v));
            setBackground(sel ? SELECT : (row % 2 == 0 ? CARD : ROW_ALT));
            setToolTipText("Tick to mark as completed");
            return this;
        }
    }

    // ================= add / edit dialog =================

    static class AssignmentDialog extends JDialog {
        boolean ok = false;
        private final JTextField nameF = new JTextField();
        private final JComboBox<String> subjectF = new JComboBox<>();
        private final JSpinner dateF;
        private final JComboBox<String> prioF = new JComboBox<>(new String[]{"High", "Medium", "Low"});
        private final JLabel error = new JLabel(" ");

        AssignmentDialog(JFrame owner, Assignment a, List<String> subjects) {
            super(owner, a == null ? "Add Assignment" : "Edit Assignment", true);
            JPanel root = new JPanel(new BorderLayout(0, 14));
            root.setBackground(CARD);
            root.setBorder(new EmptyBorder(20, 24, 18, 24));

            JLabel title = new JLabel(a == null ? "New assignment" : "Edit assignment");
            title.setFont(font(Font.BOLD, 18));
            title.setForeground(INK);
            root.add(title, BorderLayout.NORTH);

            JPanel form = new JPanel(new GridBagLayout());
            form.setOpaque(false);

            subjectF.setEditable(true);
            for (String s : subjects) subjectF.addItem(s);
            SpinnerDateModel dm = new SpinnerDateModel(
                    java.sql.Date.valueOf(a == null ? LocalDate.now().plusDays(7) : a.getDeadline()),
                    null, null, Calendar.DAY_OF_MONTH);
            dateF = new JSpinner(dm);
            dateF.setEditor(new JSpinner.DateEditor(dateF, "yyyy-MM-dd"));

            if (a != null) {
                nameF.setText(a.getName());
                subjectF.setSelectedItem(a.getSubject());
                prioF.setSelectedIndex(a.getPriority() - 1);
            } else {
                subjectF.setSelectedItem("");
                prioF.setSelectedIndex(1);
            }

            addRow(form, 0, "Assignment name", nameF);
            addRow(form, 1, "Subject", subjectF);
            addRow(form, 2, "Deadline (use ▲▼ or type yyyy-MM-dd)", dateF);
            addRow(form, 3, "Priority", prioF);
            for (JComponent c : new JComponent[]{nameF, subjectF, dateF, prioF}) {
                c.setFont(font(Font.PLAIN, 14));
                c.setPreferredSize(new Dimension(340, 34));
            }
            nameF.setBorder(new CompoundBorder(new LineBorder(BORDER, 1, true), new EmptyBorder(0, 8, 0, 8)));
            root.add(form, BorderLayout.CENTER);

            error.setForeground(RED);
            error.setFont(font(Font.PLAIN, 12));
            JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
            btns.setOpaque(false);
            JButton cancel = new JButton("Cancel");
            styleSecondary(cancel, MUTED);
            JButton save = primary(a == null ? "Add" : "Save changes");
            cancel.addActionListener(e -> dispose());
            save.addActionListener(e -> submit());
            btns.add(cancel);
            btns.add(save);
            JPanel south = new JPanel(new BorderLayout(0, 8));
            south.setOpaque(false);
            south.add(error, BorderLayout.NORTH);
            south.add(btns, BorderLayout.CENTER);
            root.add(south, BorderLayout.SOUTH);

            setContentPane(root);
            getRootPane().setDefaultButton(save);
            getRootPane().registerKeyboardAction(e -> dispose(), KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                    JComponent.WHEN_IN_FOCUSED_WINDOW);
            pack();
            setResizable(false);
            setLocationRelativeTo(owner);
        }

        private void addRow(JPanel p, int row, String label, JComponent field) {
            GridBagConstraints g = new GridBagConstraints();
            g.gridx = 0; g.gridy = row * 2; g.anchor = GridBagConstraints.WEST;
            g.insets = new Insets(row == 0 ? 0 : 10, 0, 4, 0);
            JLabel l = new JLabel(label);
            l.setFont(font(Font.BOLD, 12));
            l.setForeground(MUTED);
            p.add(l, g);
            g = new GridBagConstraints();
            g.gridx = 0; g.gridy = row * 2 + 1; g.fill = GridBagConstraints.HORIZONTAL; g.weightx = 1;
            p.add(field, g);
        }

        private void submit() {
            try { dateF.commitEdit(); } catch (java.text.ParseException ex) {
                error.setText("Date must look like 2026-12-31"); return;
            }
            if (nameF.getText().trim().isEmpty()) { error.setText("Please enter an assignment name."); nameF.requestFocus(); return; }
            if (subject().isEmpty()) { error.setText("Please enter a subject."); return; }
            ok = true;
            dispose();
        }

        String name() { return nameF.getText().trim(); }
        String subject() {
            Object o = subjectF.getEditor().getItem();
            return o == null ? "" : o.toString().trim();
        }
        LocalDate deadline() {
            return new java.sql.Date(((Date) dateF.getValue()).getTime()).toLocalDate();
        }
        int priority() { return prioF.getSelectedIndex() + 1; }
    }

    // ================= main =================

    public static void main(String[] args) {
        // default cross-platform look keeps our custom button colours
        SwingUtilities.invokeLater(() -> new Main().setVisible(true));
    }
}
