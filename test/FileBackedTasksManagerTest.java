import Manager.FileBackedTasksManager;
import Manager.Managers;
import Tasks.EpicTask;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static Manager.FileBackedTasksManager.loadFromFile;
import static org.junit.jupiter.api.Assertions.*;

class FileBackedTasksManagerTest extends TasksManagerTest<FileBackedTasksManager> {
    private final String filePath = "resources/test_tasks.csv";

    @BeforeEach
    public void beforeEachInit() {
        manager = (FileBackedTasksManager) Managers.getDefaultFileManager(filePath);
        createAllTestTask(manager);
        getHistoryAllTestTask(manager);
    }

    String firstStringOfCsv = "id,type,name,status,description,start,duration,epic\n";
    String onlyTestEpic = "1,EPIC,Epic for test,NEW,let's drink beer\n\n";

    // Работа с файлом
    @Test
    public void shouldNotGetAnything() throws IOException {
        manager = (FileBackedTasksManager) Managers.getDefaultFileManager(filePath);
        manager.deleteTasks(); // вызываем метод save()

        assertTrue(manager.getTasks().isEmpty());
        assertTrue(manager.getEpicTasks().isEmpty());
        assertTrue(manager.getSubTasks().isEmpty());
        assertTrue(manager.getHistory().isEmpty());

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
        assertTrue(manager.getHistory().isEmpty());

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
        assertTrue(manager.getHistory().isEmpty());
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
        assertTrue(manager.getHistory().isEmpty());
    }
}