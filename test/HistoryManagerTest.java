import Manager.HistoryManager;
import Manager.Managers;
import Tasks.EpicTask;
import Tasks.StatusOfTask;
import Tasks.SubTask;
import Tasks.Task;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

public class HistoryManagerTest {
    private HistoryManager managerHistory = Managers.getDefaultHistory();

    private final Task task1 = new Task("First Simple New",
            "task id = 1", StatusOfTask.NEW, "12.02.2024 12:00", 150);

    private final EpicTask epicTask1 = new EpicTask("First Epic",
            "epic id = 2");

    private final SubTask subTask1 = new SubTask("First Sub",
            "sub id = 3", StatusOfTask.NEW, 2, "10.01.2024 12:00", 150);
    private final SubTask subTask2 = new SubTask("Second Sub",
            "sub id = 4", StatusOfTask.IN_PROGRESS, 2, "11.01.2024 12:00", 150);

    @BeforeEach
    public void beforeEachInit() {
        managerHistory = Managers.getDefaultHistory();
        task1.setTaskId(1);
        epicTask1.setTaskId(2);
        subTask1.setTaskId(3);
        subTask2.setTaskId(4);
    }

    @Test
    public void addToEmptyHistory() {
        assertEquals(0, managerHistory.getHistory().size());
        managerHistory.add(task1);
        assertEquals(1, managerHistory.getHistory().size());
        assertEquals(List.of(task1), managerHistory.getHistory());
    }

    @Test
    public void duplicateToHistory() {
        managerHistory.add(task1);
        assertEquals(1, managerHistory.getHistory().size());

        managerHistory.add(task1);
        managerHistory.add(task1);
        assertEquals(1, managerHistory.getHistory().size());
        assertEquals(List.of(task1), managerHistory.getHistory());
    }

    @Test
    public void addAndDuplicateManyTask() {
        addTasksToHistory();

        assertEquals(4, managerHistory.getHistory().size());
        assertEquals(List.of(task1, epicTask1, subTask1, subTask2), managerHistory.getHistory());

        managerHistory.add(subTask1);
        managerHistory.add(task1);
        assertEquals(4, managerHistory.getHistory().size());
        assertEquals(List.of(epicTask1, subTask2, subTask1, task1), managerHistory.getHistory());
    }

    @Test
    public void deleteFromStartMidEndHistory() {
        addTasksToHistory();
        assertEquals(4, managerHistory.getHistory().size());
        assertEquals(List.of(task1, epicTask1, subTask1, subTask2), managerHistory.getHistory());

        managerHistory.removeOneTaskHistory(1);
        assertEquals(3, managerHistory.getHistory().size());
        assertEquals(List.of(epicTask1, subTask1, subTask2), managerHistory.getHistory());

        managerHistory.removeOneTaskHistory(3);
        assertEquals(2, managerHistory.getHistory().size());
        assertEquals(List.of(epicTask1, subTask2), managerHistory.getHistory());

        managerHistory.removeOneTaskHistory(4);
        assertEquals(1, managerHistory.getHistory().size());
        assertEquals(List.of(epicTask1), managerHistory.getHistory());
    }

    private void addTasksToHistory() {
        managerHistory.add(task1);
        managerHistory.add(epicTask1);
        managerHistory.add(subTask1);
        managerHistory.add(subTask2);
    }
}
