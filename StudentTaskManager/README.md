# Student Task Manager (Java, console)

A desktop (Swing) app to keep track of assignments. No database, no external libraries.

## Features
1. Add new assignment (name, subject, deadline, priority)
2. Edit assignment
3. Delete assignment (asks for confirmation)
4. View all assignments (shows priority and an overdue / due-soon status)
5. View overdue assignments
6. Show most urgent assignment (earliest deadline)
7. Show highest priority assignment
8. **Mark an assignment as completed** (tick the checkbox, or select a row and press *Mark Completed*; undo with *Mark Pending*)

Completed assignments are greyed out, never counted as overdue, and ignored by "most urgent" / "highest priority".

Priority: 1 = High, 2 = Medium, 3 = Low.
Deadlines use the format yyyy-MM-dd, for example 2026-12-31.

Data is saved automatically to `assignments.txt` after every change and loaded when the program starts.

## Data structures
- ArrayList: stores all assignments
- PriorityQueue (two heaps): gives the most urgent and the highest priority assignment in O(1)

## Run (JDK 17+)
    mkdir out
    javac -d out src/project/*.java        (Windows: javac -d out src\project\*.java)
    java -cp out project.Main          (GUI)
    java -cp out project.ConsoleMain   (old console version, now with option 8 = mark completed)

VS Code: open this folder, open `src/project/Main.java`, press Run.

## Files
- Assignment.java  - one assignment (also works out overdue / days left)
- TaskManager.java - ArrayList + heaps, add / edit / delete / overdue
- FileStorage.java - reads and writes assignments.txt
- Main.java        - Swing GUI
- ConsoleMain.java - console menu
