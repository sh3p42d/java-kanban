import Manager.FileBackedTasksManager;
import Manager.Managers;
import Tasks.EpicTask;
import Tasks.Task;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static Manager.FileBackedTasksManager.loadFromFile;
import static org.junit.jupiter.api.Assertions.*;

class FileBackedTasksManagerTest extends TasksManagerTest<FileBackedTasksManager> {
    String filePath = "resources/test_tasks.csv";
    FileBackedTasksManager manager = (FileBackedTasksManager) Managers.getDefaultFileManager(filePath);

    String firstStringOfCsv = "id,type,name,status,description,start,duration,epic\n";
    String onlyTestEpic = "1,EPIC,Epic for test,NEW,let's drink beer\n\n";

    @BeforeEach
    void beforeEach() {
        manager = (FileBackedTasksManager) Managers.getDefaultFileManager(filePath);
        createAllTestTask(manager);
        getHistoryAllTestTask(manager);
    }

    // Уникальные тесты для файлового менеджера
    @Test
    public void shouldNotGetAnything() throws IOException {
        manager = (FileBackedTasksManager) Managers.getDefaultFileManager(filePath);
        manager.deleteTasks(); // вызываем метод save() через любой метод

        // Проверить исключение вида:
        // assertThrows(ManagerSaveException.class, () -> manager.deleteTasks());
        // нельзя, т.к. в методе save() есть блок try-catch,
        // который не дает выбросить исключение тесту

        assertTrue(manager.getTasks().isEmpty());
        assertTrue(manager.getEpicTasks().isEmpty());
        assertTrue(manager.getSubTasks().isEmpty());
        assertTrue(manager.getManagerHistory().getHistory().isEmpty());

        String content = new String(Files.readAllBytes(Paths.get(filePath)));
        assertEquals(firstStringOfCsv, content);
    }

    @Test
    public void shouldGetEpicWithoutSubsWithHistory() throws IOException {
        manager = (FileBackedTasksManager) Managers.getDefaultFileManager(filePath);
        EpicTask testEpic = createTestEpic(manager);
        manager.getEpic(testEpic.getTaskId());
        manager.deleteSubTasks();

        assertTrue(manager.getTasks().isEmpty());
        assertEquals(1, manager.getEpicTasks().size());
        assertTrue(manager.getSubTasks().isEmpty());
        assertEquals(1, manager.getHistory().size());

        String content = new String(Files.readAllBytes(Paths.get(filePath)));
        assertEquals(firstStringOfCsv + onlyTestEpic + "1", content);
    }

    @Test
    public void shouldGetEpicWithoutSubsWithoutHistory() throws IOException {
        manager = (FileBackedTasksManager) Managers.getDefaultFileManager(filePath);
        createTestEpic(manager);
        manager.deleteSubTasks();

        assertTrue(manager.getTasks().isEmpty());
        assertEquals(1, manager.getEpicTasks().size());
        assertTrue(manager.getSubTasks().isEmpty());
        assertTrue(manager.getManagerHistory().getHistory().isEmpty());

        String content = new String(Files.readAllBytes(Paths.get(filePath)));
        assertEquals(firstStringOfCsv + onlyTestEpic, content);
    }

    @Test
    public void shouldLoadFromFile() {
        manager = (FileBackedTasksManager) Managers.getDefaultFileManager(filePath);
        manager = loadFromFile(new File(filePath));

        assertEquals(3, manager.getTasks().size());
        assertEquals(3, manager.getEpicTasks().size());
        assertEquals(4, manager.getSubTasks().size());
        assertEquals(10, manager.getHistory().size());
    }

    @Test
    public void shouldLoadFromEmptyFile() {
        manager.deleteTasks();
        manager.deleteEpicTasks();
        manager.deleteSubTasks();

        manager = (FileBackedTasksManager) Managers.getDefaultFileManager(filePath);
        manager = loadFromFile(new File(filePath));

        assertTrue(manager.getTasks().isEmpty());
        assertTrue(manager.getEpicTasks().isEmpty());
        assertTrue(manager.getSubTasks().isEmpty());
        assertTrue(manager.getManagerHistory().getHistory().isEmpty());
    }

    @Test
    public void shouldLoadFromFileWithoutSubs() throws IOException {
        manager.deleteTasks();
        manager.deleteEpicTasks();
        manager.deleteSubTasks();

        manager = (FileBackedTasksManager) Managers.getDefaultFileManager(filePath);
        EpicTask testEpic = createTestEpic(manager);
        manager.getEpic(testEpic.getTaskId());
        manager.deleteSubTasks();
        manager = loadFromFile(new File(filePath));

        assertTrue(manager.getTasks().isEmpty());
        assertEquals(1, manager.getEpicTasks().size());
        assertTrue(manager.getSubTasks().isEmpty());
        assertEquals(1, manager.getHistory().size());

        String content = new String(Files.readAllBytes(Paths.get(filePath)));
        assertEquals(firstStringOfCsv + onlyTestEpic + "1", content);
    }

    @Test
    public void shouldLoadFromFileWithoutHistory() {
        manager.clearHistory();
        manager = (FileBackedTasksManager) Managers.getDefaultFileManager(filePath);
        manager = loadFromFile(new File(filePath));

        assertEquals(3, manager.getTasks().size());
        assertEquals(3, manager.getEpicTasks().size());
        assertEquals(4, manager.getSubTasks().size());
        assertTrue(manager.getManagerHistory().getHistory().isEmpty());
    }

    // Тесты приоритетов
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
    private String getHistoryStringFromFile() throws IOException {
        BufferedReader input = new BufferedReader(new FileReader(filePath));
        String historyString = "", line;

        while ((line = input.readLine()) != null) {
            historyString = line;
        }
        return historyString;
    }

    @Test
    public void shouldGetEmptyHistory() {
        manager.clearHistory();
        assertTrue(manager.getHistory().isEmpty());
    }

    @Test
    public void shouldNotDuplicateHistory() throws IOException {
        List<Task> history = generateHistoryWithDuplicates(manager);

        assertEquals(10, manager.getHistory().size());
        assertEquals(history, manager.getHistory());
        assertEquals("1,2,3,4,5,6,7,8,9,10", getHistoryStringFromFile());
    }

    @Test
    public void shouldDeleteFromHistory() throws IOException {
        List<Task> history = generateHistoryWithDeletions(manager);

        assertEquals(5, manager.getHistory().size());
        assertEquals(history, manager.getHistory());
        assertEquals("2,3,5,6,9", getHistoryStringFromFile());
    }

    @Test
    public void shouldDeleteStartMidEndFromHistory() throws IOException {
        List<Task> history = generateHistoryWithDeletionsFromStartMidEnd(manager);

        assertEquals(2, manager.getHistory().size());
        assertEquals(history, manager.getHistory());
        assertEquals("1,4", getHistoryStringFromFile());
    }

    @Test
    public void shouldAddToHistoryAfterClear() throws IOException {
        List<Task> history = generateHistoryAfterClear(manager);

        assertEquals(6, manager.getHistory().size());
        assertEquals(history, manager.getHistory());
        assertEquals("3,1,5,4,9,7", getHistoryStringFromFile());
    }

    // Получение списка всех задач каждого типа
    // без записи в историю
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