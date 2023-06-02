import Manager.InMemoryTaskManager;
import Manager.Managers;
import Tasks.Task;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MemoryManagerTest extends TasksManagerTest<InMemoryTaskManager> {
    InMemoryTaskManager manager = (InMemoryTaskManager) Managers.getDefault();

    // Некоторые тесты выполнены в формате:
    // Позитивная проверка - нормальная работа метода
    // Негативная проверка 1 - передача несуществующих данных
    // Негативная проверка 2 - удаление и передача ранее существовавших данных

    @BeforeEach
    public void beforeEach() {
        manager = (InMemoryTaskManager) Managers.getDefault();
        createAllTestTask(manager);
        getHistoryAllTestTask(manager);
    }

    // Тесты приоритетов и пересечений
    @Test
    public void shouldGetSort() {
        manager.getPrioritizedTasks();
        assertEquals(generateSortedSet(), new ArrayList<>(manager.getSetTasks()));
    }

    @Test public void shouldNotCreateTasksWhenTimeIsBusy() {
        generateShouldNotCreateTasksWhenTimeIsBusy(manager);
        assertEquals(1, manager.getTaskMap().size());
    }


    // Тесты истории
    @Test
    public void shouldGetEmptyHistory() {
        manager.clearHistory();
        assertTrue(manager.getHistory().isEmpty());
    }

    @Test
    public void shouldNotDuplicateHistory() {
        List<Task> history = generateHistoryWithDuplicates(manager);

        assertEquals(10, manager.getHistory().size());
        assertEquals(history, manager.getHistory());
    }

    @Test
    public void shouldDeleteFromHistory() {
        List<Task> history = generateHistoryWithDeletions(manager);

        assertEquals(5, manager.getHistory().size());
        assertEquals(history, manager.getHistory());
    }

    @Test
    public void shouldDeleteStartMidEndFromHistory() {
        List<Task> history = generateHistoryWithDeletionsFromStartMidEnd(manager);

        assertEquals(2, manager.getHistory().size());
        assertEquals(history, manager.getHistory());
    }

    @Test
    public void shouldAddToHistoryAfterClear() {
        List<Task> history = generateHistoryAfterClear(manager);

        assertEquals(6, manager.getHistory().size());
        assertEquals(history, manager.getHistory());
    }

    // Получение списка всех задач каждого типа
    @Test
    public void shouldGetAllTypeTasks() {
        assertEquals(getAllTasks(), manager.getTaskMap());
        assertEquals(getAllEpics(), manager.getEpicMap());
        assertEquals(getAllSubs(), manager.getSubMap());
    }

    // Удаление всех задач каждого типа
    @Test
    public void shouldDeleteAllTypeTasks() {
        manager.deleteTasks();
        assertTrue(manager.getTaskMap().isEmpty());

        manager.deleteEpicTasks();
        assertTrue(manager.getEpicMap().isEmpty());

        generateShouldDeleteAllSubTasks(manager);
        assertTrue(manager.getSubMap().isEmpty());
    }

    // Получение задачи каждого типа по ID
    @Test
    public void shouldGetTaskById() {
        generateShouldGetTaskById(manager);
    }

    @Test
    public void shouldGetEpicTaskById() {
        generateShouldGetEpicTaskById(manager);
    }

    @Test
    public void shouldGetSubTaskById() {
        generateShouldGetSubTaskById(manager);
    }

    // Получение списка всех подзадач эпика по ID
    @Test
    public void shouldGetSubTasksFromEpic() {
        generateShouldGetSubTasksFromEpic(manager);
    }

    // Создание каждого типа задач
    @Test
    public void shouldCreateTask() {
        generateShouldCreateTask(manager);
    }

    @Test
    public void shouldCreateTaskWithEmptyTasksList() {
        generateShouldCreateTaskWithEmptyTasksList(manager);
    }

    @Test
    public void shouldCreateEpicTask() {
        generateShouldCreateEpicTask(manager);
    }

    @Test
    public void shouldCreateEpicTaskWithEmptyEpicsList() {
        generateShouldCreateEpicTaskWithEmptyEpicsList(manager);
    }

    @Test
    public void shouldCreateSubTask() {
        shouldCreateSubTask(manager);
    }

    @Test
    public void shouldCreateSubTaskWithEmptySubsList() {
        generateShouldCreateSubTaskWithEmptySubsList(manager);
    }


    // Обновление задач каждого типа
    @Test
    public void shouldUpdateTask() {
        generateShouldUpdateTask(manager);
    }

    @Test
    public void shouldUpdateTaskWithEmptyTasksList() {
        generateShouldUpdateTaskWithEmptyTasksList(manager);
    }

    @Test
    public void shouldUpdateEpicTask() {
        generateShouldUpdateEpicTask(manager);
    }

    @Test
    public void shouldUpdateEpicTaskWithEmptyEpicsList() {
        generateShouldUpdateEpicTaskWithEmptyEpicsList(manager);
    }

    @Test
    public void shouldUpdateSubTask() {
        generateShouldUpdateSubTask(manager);
    }

    @Test
    public void shouldUpdateSubTaskWithEmptySubsList() {
        generateShouldUpdateSubTaskWithEmptySubsList(manager);
    }

    // Удаление задачи по ID
    @Test
    public void shouldDeleteTaskById() {
        generateShouldDeleteTaskById(manager);
    }

    @Test
    public void shouldDeleteTaskByIdWithEmptyTasksList() {
        generateShouldDeleteTaskByIdWithEmptyTasksList(manager);
    }

    @Test
    public void shouldNotDeleteTaskByWrongId() {
        generateShouldNotDeleteTaskByWrongId(manager);
    }

    @Test
    public void shouldDeleteEpicTaskById() {
        generateShouldDeleteEpicTaskById(manager);
    }

    @Test
    public void shouldDeleteEpicTaskByIdWithEmptyEpicsList() {
        generateShouldDeleteEpicTaskByIdWithEmptyEpicsList(manager);
    }

    @Test
    public void shouldNotDeleteEpicByWrongId() {
        generateShouldNotDeleteEpicByWrongId(manager);
    }

    @Test
    public void shouldDeleteSubTaskById() {
        generateShouldDeleteSubTaskById(manager);
    }

    @Test
    public void shouldDeleteSubTaskByIdWithEmptySubsList() {
        generateShouldDeleteSubTaskByIdWithEmptySubsList(manager);
    }

    @Test
    public void shouldNotDeleteSubByWrongId() {
        generateShouldNotDeleteSubByWrongId(manager);
    }
}