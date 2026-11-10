package project;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/** Saves / loads assignments in a plain text file (one assignment per line, tab separated). */
public class FileStorage {
    private final Path file;

    public FileStorage(String fileName) {
        this.file = Paths.get(fileName);
    }

    public String path() { return file.toAbsolutePath().toString(); }

    public void save(List<Assignment> items) throws IOException {
        List<String> lines = new ArrayList<>();
        for (Assignment a : items) {
            lines.add(a.getId() + "\t" + esc(a.getName()) + "\t" + esc(a.getSubject()) + "\t"
                    + a.getDeadline() + "\t" + a.getPriority() + "\t" + a.isCompleted());
        }
        Path tmp = file.resolveSibling(file.getFileName() + ".tmp");
        Files.write(tmp, lines, StandardCharsets.UTF_8);
        try {
            Files.move(tmp, file, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (AtomicMoveNotSupportedException e) {
            Files.move(tmp, file, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    /** Loads the file into the manager. Returns how many assignments were loaded. */
    public int load(TaskManager manager) throws IOException {
        if (!Files.exists(file)) return 0;
        int count = 0;
        for (String line : Files.readAllLines(file, StandardCharsets.UTF_8)) {
            if (line.trim().isEmpty()) continue;
            try {
                String[] p = line.split("\t", -1);
                manager.addWithId(Integer.parseInt(p[0]), unesc(p[1]), unesc(p[2]),
                        LocalDate.parse(p[3]), Integer.parseInt(p[4]),
                        p.length > 5 && Boolean.parseBoolean(p[5].trim()));
                count++;
            } catch (RuntimeException e) {
                System.err.println("Skipping bad line: " + line);
            }
        }
        return count;
    }

    private static String esc(String s) {
        return s.replace("\\", "\\\\").replace("\t", "\\t").replace("\r", "").replace("\n", "\\n");
    }

    private static String unesc(String s) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '\\' && i + 1 < s.length()) {
                char n = s.charAt(++i);
                sb.append(n == 't' ? '\t' : n == 'n' ? '\n' : n);
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }
}
