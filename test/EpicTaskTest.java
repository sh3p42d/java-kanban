import Manager.InMemoryTaskManager;
import Manager.Managers;
import Tasks.EpicTask;
import Tasks.StatusOfTask;
import Tasks.SubTask;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class EpicTaskTest {

    InMemoryTaskManager manager = (InMemoryTaskManager) Managers.getDefault();
    EpicTask epic = new EpicTask("Test epic", "Special epicTask for test");

    // Упрощаем создание подзадач с разными статусами
    private void createSubWithStatus(StatusOfTask status) {
        SubTask sub = new SubTask("Sub №" + manager.getNextId(), status.toString() + " status",
                status, epic.getTaskId(), "13.02.2024 12:00", 150);
        manager.createSubTask(sub);
    }

    @BeforeEach
    public void beforeEach() {
        manager.createEpicTask(epic);
    }

    @Test
    public void shouldBeNewStatusWithoutSubs() {
        assertEquals(epic.getTaskStatus(), StatusOfTask.NEW);
    }

    @Test
    public void shouldBeNewStatusWithSubs() {
        createSubWithStatus(StatusOfTask.NEW);

        assertEquals(epic.getTaskStatus(), StatusOfTask.NEW);
    }

    @Test
    public void shouldBeDoneStatusWithSubs() {
        createSubWithStatus(StatusOfTask.DONE);

        assertEquals(epic.getTaskStatus(), StatusOfTask.DONE);
    }

    @Test
    public void shouldBeInProgressStatusWithSubsInProgressStatuses() {
        createSubWithStatus(StatusOfTask.IN_PROGRESS);

        assertEquals(epic.getTaskStatus(), StatusOfTask.IN_PROGRESS);
    }
}