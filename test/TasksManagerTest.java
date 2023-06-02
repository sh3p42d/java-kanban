import Manager.TaskManager;
import Tasks.EpicTask;
import Tasks.StatusOfTask;
import Tasks.SubTask;
import Tasks.Task;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public abstract class TasksManagerTest <T extends TaskManager> {

    private final Task task1 = new Task("First Simple New",
            "task id = 1", StatusOfTask.NEW, "12.02.2024 12:00", 150);
    private final Task task2 = new Task("Second Simple In progress",
            "task id = 2", StatusOfTask.IN_PROGRESS, "12.03.2024 12:00", 150);
    private final Task task3 = new Task("Third Simple done",
            "task id = 3", StatusOfTask.DONE, "12.04.2024 12:00", 150);

    private final EpicTask epicTask1 = new EpicTask("First Epic",
            "epic id = 4");
    private final EpicTask epicTask2 = new EpicTask("Second Epic",
            "epic id = 5");
    private final EpicTask epicTask3 = new EpicTask("Third Epic",
            "epic id = 6");

    private final SubTask subTask1 = new SubTask("First Sub",
            "sub id = 7", StatusOfTask.NEW, -1, "10.01.2024 12:00", 150);
    private final SubTask subTask2 = new SubTask("Second Sub",
            "sub id = 8", StatusOfTask.IN_PROGRESS, -1, "11.01.2024 12:00", 150);
    private final SubTask subTask3 = new SubTask("Third Sub",
            "sub id = 9", StatusOfTask.DONE, -1);
    private final SubTask subTask4 = new SubTask("Fourth Sub",
            "sub id = 10", StatusOfTask.NEW, -1, "13.01.2024 12:00", 150);

    public void createAllTestTask(T manager) {
        // Создаем задачи
        manager.createTask(task1);
        manager.createTask(task2);
        manager.createTask(task3);
        manager.createEpicTask(epicTask1);
        manager.createEpicTask(epicTask2);
        manager.createEpicTask(epicTask3);
        subTask1.setEpicId(epicTask1.getTaskId());
        subTask2.setEpicId(epicTask1.getTaskId());
        subTask3.setEpicId(epicTask2.getTaskId());
        subTask4.setEpicId(epicTask2.getTaskId());
        manager.createSubTask(subTask1);
        manager.createSubTask(subTask2);
        manager.createSubTask(subTask3);
        manager.createSubTask(subTask4);
    }

    public void getHistoryAllTestTask(T manager) {
        // Выводим задачу по ID с записью в историю
        manager.getTask(1);
        manager.getTask(2);
        manager.getTask(3);
        manager.getEpic(4);
        manager.getEpic(5);
        manager.getEpic(6);
        manager.getSub(7);
        manager.getSub(8);
        manager.getSub(9);
        manager.getSub(10);
    }

    public Map<Integer, Task> getAllTasks() {
        Map<Integer, Task> taskMap = new HashMap<>();
        taskMap.put(1, task1);
        taskMap.put(2, task2);
        taskMap.put(3, task3);

        return taskMap;
    }

    public Map<Integer, Task> getAllEpics() {
        Map<Integer, Task> epicMap = new HashMap<>();
        epicMap.put(4, epicTask1);
        epicMap.put(5, epicTask2);
        epicMap.put(6, epicTask3);

        return epicMap;
    }

    public Map<Integer, Task> getAllSubs() {
        Map<Integer, Task> subMap = new HashMap<>();
        subMap.put(7, subTask1);
        subMap.put(8, subTask2);
        subMap.put(9, subTask3);
        subMap.put(10, subTask4);

        return subMap;
    }

    public List<Task> generateSortedSet() {
        List<Task> setTasks = new ArrayList<>();
        setTasks.add(epicTask1);
        setTasks.add(subTask2);
        setTasks.add(epicTask2);
        setTasks.add(task1);
        setTasks.add(task2);
        setTasks.add(task3);
        setTasks.add(epicTask3);
        return setTasks;
    }

    public Task getTestTask() {
        return task1;
    }

    public EpicTask getTestEpic() {
        return epicTask1;
    }

    public SubTask getTestSub() {
        return subTask1;
    }

    public Task createTestTask(T manager) {
        Task testTask = new Task("Task for test",
                "i love unit testing", StatusOfTask.NEW, "12.01.2024 12:00", 150);
        manager.createTask(testTask);

        return testTask;
    }

    public EpicTask createTestEpic(T manager) {
        EpicTask testEpic = new EpicTask("Epic for test", "let's drink beer");
        manager.createEpicTask(testEpic);

        return testEpic;
    }

    public SubTask createTestSub(T manager, EpicTask epic, StatusOfTask status) {
        SubTask testSub = new SubTask("Sub for test", "10 liters",
                status, epic.getTaskId(), "12.01.2024 12:00", 150);
        manager.createSubTask(testSub);

        return testSub;
    }

    public Task updateTestTask(Task task) {
        Task newTask = new Task(" ", " ",
                StatusOfTask.IN_PROGRESS, "15.10.2025 19:00", 250);

        newTask.setTaskName("New test Task name");
        newTask.setTaskDescription("New test Task description");
        newTask.setTaskId(task.getTaskId());

        return newTask;
    }

    public EpicTask updateTestEpic(EpicTask task) {
        EpicTask newEpic = new EpicTask("New test Epic name", "New test Epic description");
        newEpic.setTaskId(task.getTaskId());
        newEpic.setSubIds(task.getSubIds());
        newEpic.setTaskStatus(task.getTaskStatus());
        newEpic.setStartTime(task.getStartTime());
        newEpic.setDuration(task.getDuration());
        newEpic.setEndTime();

        return newEpic;
    }

    public SubTask updateTestSub(SubTask task) {
        SubTask newSub = new SubTask("New test Sub name", "New test Sub description",
                task.getTaskStatus(), task.getEpicId(), "12.01.2024 12:00", 150);
        newSub.setTaskId(task.getTaskId());

        return newSub;
    }

    public NullPointerException executeDeleteEpic(T manager, int id) {
        return assertThrows(
                NullPointerException.class,
                () -> manager.deleteEpicById(id)
        );
    }

    public NullPointerException executeDeleteSub(T manager, int id) {
        return assertThrows(
                NullPointerException.class,
                () -> manager.deleteSubById(id)
        );
    }

    public List<Task> generateHistoryWithDuplicates(T manager) {
        getHistoryAllTestTask(manager);
        getHistoryAllTestTask(manager);
        getHistoryAllTestTask(manager);

        List<Task> history = new ArrayList<>(manager.getTasks());
        history.addAll(manager.getEpicTasks());
        history.addAll(manager.getSubTasks());

        return history;
    }

    public List<Task> generateHistoryWithDeletions(T manager) {
        manager.deleteTaskById(1);
        manager.deleteEpicById(4);
        manager.deleteSubById(10);

        List<Task> history = new ArrayList<>(manager.getTasks());
        history.addAll(manager.getEpicTasks());
        history.addAll(manager.getSubTasks());

        return history;
    }

    public List<Task> generateHistoryWithDeletionsFromStartMidEnd(T manager) {
        manager.clearHistory();

        // Формируем контрольный список для проверки истории
        List<Task> history = new ArrayList<>();
        history.add(manager.getTask(3));
        history.add(manager.getTask(1));
        history.add(manager.getEpic(5));
        history.add(manager.getEpic(4));
        history.add(manager.getSub(9));
        history.add(manager.getSub(7));

        history.remove(manager.getTask(3));
        manager.deleteTaskById(3);


        history.remove(manager.getEpic(5));
        history.remove(manager.getSub(9));
        manager.deleteEpicById(5);

        history.remove(manager.getSub(7));
        manager.deleteSubById(7);

        return history;
    }

    public List<Task> generateHistoryAfterClear(T manager) {
        manager.clearHistory();

        List<Task> history = new ArrayList<>();
        history.add(manager.getTask(3));
        history.add(manager.getTask(1));
        history.add(manager.getEpic(5));
        history.add(manager.getEpic(4));
        history.add(manager.getSub(9));
        history.add(manager.getSub(7));

        return history;
    }


    public void generateShouldDeleteAllSubTasks(T manager) {
        manager.deleteSubTasks();

        for (EpicTask epic : manager.getEpicTasks()) {
            assertEquals(StatusOfTask.NEW, epic.getTaskStatus());
            assertTrue(epic.getSubIds().isEmpty());
        }
    }

    public void generateShouldGetTaskById(T manager) {
        // Позитивная проверка
        assertEquals(getTestTask(), manager.getTask(getTestTask().getTaskId()));

        // Негативные проверки
        assertNull(manager.getTask(0));
        assertNull(manager.getTask(4));

        manager.deleteTasks();
        assertNull(manager.getTask(getTestTask().getTaskId()));
    }

    public void generateShouldGetEpicTaskById(T manager) {
        assertEquals(getTestEpic().toString(), manager.getEpic(getTestEpic().getTaskId()).toString());
        assertEquals(StatusOfTask.IN_PROGRESS, manager.getEpic(getTestEpic().getTaskId()).getTaskStatus());

        assertNull(manager.getEpic(-3));
        assertNull(manager.getEpic(7));

        manager.deleteEpicTasks();
        assertNull(manager.getEpic(getTestEpic().getTaskId()));
    }

    public void generateShouldGetSubTaskById(T manager) {
        assertEquals(getTestSub().toString(), manager.getSub(getTestSub().getTaskId()).toString());
        assertEquals(4, manager.getSub(getTestSub().getTaskId()).getEpicId());

        assertNull(manager.getSub(6));
        assertNull(manager.getSub(+11));

        manager.deleteSubTasks();
        assertNull(manager.getSub(getTestSub().getTaskId()));
    }

    public void generateShouldGetSubTasksFromEpic(T manager) {
        assertEquals(List.of(manager.getSub(7), manager.getSub(8)), manager.getSubFromEpic(4));

        assertTrue(manager.getEpic(6).getSubIds().isEmpty());

        manager.deleteSubTasks();
        assertTrue(manager.getEpic(4).getSubIds().isEmpty());
    }

    public void generateShouldCreateTask(T manager) {
        Task testTask = createTestTask(manager);

        assertEquals(testTask, manager.getTask(testTask.getTaskId()));
        assertNull(manager.getEpic(testTask.getTaskId()));
        assertNull(manager.getSub(testTask.getTaskId()));
    }

    public void generateShouldCreateTaskWithEmptyTasksList(T manager) {
        manager.deleteTasks();
        Task testTask = createTestTask(manager);

        assertEquals(testTask, manager.getTask(testTask.getTaskId()));
        assertNull(manager.getEpic(testTask.getTaskId()));
        assertNull(manager.getSub(testTask.getTaskId()));
    }

    public void generateShouldCreateEpicTask(T manager) {
        EpicTask testEpic = createTestEpic(manager);
        createTestSub(manager, testEpic, StatusOfTask.IN_PROGRESS);

        assertEquals(testEpic, manager.getEpic(testEpic.getTaskId()));
        assertEquals(StatusOfTask.IN_PROGRESS, manager.getEpic(testEpic.getTaskId()).getTaskStatus());

        assertNull(manager.getTask(testEpic.getTaskId()));
        assertNull(manager.getSub(testEpic.getTaskId()));
    }

    public void generateShouldCreateEpicTaskWithEmptyEpicsList(T manager) {
        manager.deleteEpicTasks();
        EpicTask testEpic = createTestEpic(manager);
        createTestSub(manager, testEpic, StatusOfTask.IN_PROGRESS);

        assertEquals(testEpic, manager.getEpic(testEpic.getTaskId()));
        assertEquals(StatusOfTask.IN_PROGRESS, manager.getEpic(testEpic.getTaskId()).getTaskStatus());

        assertNull(manager.getTask(testEpic.getTaskId()));
        assertNull(manager.getSub(testEpic.getTaskId()));
    }

    public void shouldCreateSubTask(T manager) {
        EpicTask testEpic = createTestEpic(manager);
        SubTask testSub = createTestSub(manager, testEpic, StatusOfTask.DONE);

        assertEquals(testSub, manager.getSub(testSub.getTaskId()));
        assertEquals(testEpic.getTaskId(), manager.getSub(testSub.getTaskId()).getEpicId());

        assertNull(manager.getTask(testSub.getTaskId()));
        assertNull(manager.getEpic(testSub.getTaskId()));
    }

    public void generateShouldCreateSubTaskWithEmptySubsList(T manager) {
        manager.deleteSubTasks();
        EpicTask testEpic = createTestEpic(manager);
        SubTask testSub = createTestSub(manager, testEpic, StatusOfTask.DONE);

        assertEquals(testSub, manager.getSub(testSub.getTaskId()));
        assertEquals(testEpic.getTaskId(), manager.getSub(testSub.getTaskId()).getEpicId());

        assertNull(manager.getTask(testSub.getTaskId()));
        assertNull(manager.getEpic(testSub.getTaskId()));
    }

    public void generateShouldUpdateTask(T manager) {
        Task testTask = createTestTask(manager);
        assertEquals(testTask, manager.getTask(testTask.getTaskId()));

        Task updTask = updateTestTask(testTask);
        manager.updateTask(updTask);

        assertEquals(updTask, manager.getTask(updTask.getTaskId()));
        assertNotEquals(testTask, manager.getTask(updTask.getTaskId()));
    }


    public void generateShouldUpdateTaskWithEmptyTasksList(T manager) {
        manager.deleteTasks();
        Task testTask = createTestTask(manager);
        assertEquals(testTask, manager.getTask(testTask.getTaskId()));
        assertEquals(1, manager.getTasks().size());

        Task updTask = updateTestTask(testTask);
        manager.updateTask(updTask);

        assertEquals(updTask, manager.getTask(updTask.getTaskId()));
        assertNotEquals(testTask, manager.getTask(updTask.getTaskId()));
        assertEquals(1, manager.getTasks().size());
    }

    public void generateShouldUpdateEpicTask(T manager) {
        EpicTask testEpic = createTestEpic(manager);
        createTestSub(manager, testEpic, StatusOfTask.IN_PROGRESS);
        assertEquals(testEpic, manager.getEpic(testEpic.getTaskId()));
        assertEquals(StatusOfTask.IN_PROGRESS, manager.getEpic(testEpic.getTaskId()).getTaskStatus());

        EpicTask updEpic = updateTestEpic(testEpic);
        manager.updateEpicTask(updEpic);
        assertEquals(updEpic, manager.getEpic(updEpic.getTaskId()));
        assertNotEquals(testEpic, manager.getEpic(updEpic.getTaskId()));
        assertEquals(StatusOfTask.IN_PROGRESS, manager.getEpic(testEpic.getTaskId()).getTaskStatus());
    }

    public void generateShouldUpdateEpicTaskWithEmptyEpicsList(T manager) {
        manager.deleteEpicTasks();
        EpicTask testEpic = createTestEpic(manager);
        createTestSub(manager, testEpic, StatusOfTask.IN_PROGRESS);
        assertEquals(testEpic, manager.getEpic(testEpic.getTaskId()));
        assertEquals(StatusOfTask.IN_PROGRESS, manager.getEpic(testEpic.getTaskId()).getTaskStatus());
        assertEquals(1, manager.getEpicTasks().size());

        EpicTask updEpic = updateTestEpic(testEpic);
        manager.updateEpicTask(updEpic);
        assertEquals(updEpic, manager.getEpic(updEpic.getTaskId()));
        assertNotEquals(testEpic, manager.getEpic(updEpic.getTaskId()));
        assertEquals(StatusOfTask.IN_PROGRESS, manager.getEpic(testEpic.getTaskId()).getTaskStatus());
        assertEquals(1, manager.getEpicTasks().size());
    }

    public void generateShouldUpdateSubTask(T manager) {
        EpicTask testEpic = createTestEpic(manager);
        SubTask testSub = createTestSub(manager, testEpic, StatusOfTask.DONE);
        assertEquals(testSub, manager.getSub(testSub.getTaskId()));
        assertEquals(testEpic.getTaskId(), manager.getSub(testSub.getTaskId()).getEpicId());

        SubTask updSub = updateTestSub(testSub);
        manager.updateSubTask(updSub);
        assertEquals(updSub, manager.getSub(updSub.getTaskId()));
        assertNotEquals(testSub, manager.getSub(updSub.getTaskId()));
        assertEquals(testEpic.getTaskId(), manager.getSub(testSub.getTaskId()).getEpicId());
    }

    public void generateShouldUpdateSubTaskWithEmptySubsList(T manager) {
        manager.deleteSubTasks();
        EpicTask testEpic = createTestEpic(manager);
        SubTask testSub = createTestSub(manager, testEpic, StatusOfTask.DONE);
        assertEquals(testSub, manager.getSub(testSub.getTaskId()));
        assertEquals(testEpic.getTaskId(), manager.getSub(testSub.getTaskId()).getEpicId());
        assertEquals(1, manager.getSubTasks().size());


        SubTask updSub = updateTestSub(testSub);
        manager.updateSubTask(updSub);
        assertEquals(updSub, manager.getSub(updSub.getTaskId()));
        assertNotEquals(testSub, manager.getSub(updSub.getTaskId()));
        assertEquals(testEpic.getTaskId(), manager.getSub(testSub.getTaskId()).getEpicId());
        assertEquals(1, manager.getSubTasks().size());
    }

    public void generateShouldDeleteTaskById(T manager) {
        Task testTask = createTestTask(manager);
        int sizeBefore = manager.getTasks().size();
        manager.deleteTaskById(testTask.getTaskId());

        assertEquals(sizeBefore - 1, manager.getTasks().size());
        assertNull(manager.getTask(testTask.getTaskId()));
    }

    public void generateShouldDeleteTaskByIdWithEmptyTasksList(T manager) {
        manager.deleteTasks();

        Task testTask = createTestTask(manager);
        int sizeBefore = manager.getTasks().size();
        manager.deleteTaskById(testTask.getTaskId());

        assertEquals(sizeBefore - 1, manager.getTasks().size());
        assertNull(manager.getTask(testTask.getTaskId()));
    }

    public void generateShouldNotDeleteTaskByWrongId(T manager) {
        Task testTask = createTestTask(manager);
        int sizeBefore = manager.getTasks().size();

        manager.deleteTaskById(0);
        manager.deleteTaskById(-1);
        manager.deleteTaskById(9999);

        assertEquals(sizeBefore, manager.getTasks().size());
        assertNotNull(manager.getTask(testTask.getTaskId()));
    }

    public void generateShouldDeleteEpicTaskById(T manager) {
        EpicTask testEpic = createTestEpic(manager);
        createTestSub(manager, testEpic, StatusOfTask.IN_PROGRESS);
        int sizeBefore = manager.getEpicTasks().size();
        manager.deleteEpicById(testEpic.getTaskId());

        assertEquals(sizeBefore - 1, manager.getEpicTasks().size());
        assertNull(manager.getEpic(testEpic.getTaskId()));
    }

    public void generateShouldDeleteEpicTaskByIdWithEmptyEpicsList(T manager) {
        manager.deleteEpicTasks();

        EpicTask testEpic = createTestEpic(manager);
        createTestSub(manager, testEpic, StatusOfTask.IN_PROGRESS);
        int sizeBefore = manager.getEpicTasks().size();
        manager.deleteEpicById(testEpic.getTaskId());

        assertEquals(sizeBefore - 1, manager.getEpicTasks().size());
        assertNull(manager.getEpic(testEpic.getTaskId()));
    }

    public void generateShouldNotDeleteEpicByWrongId(T manager) {
        EpicTask testEpic = createTestEpic(manager);
        createTestSub(manager, testEpic, StatusOfTask.IN_PROGRESS);
        int sizeBefore = manager.getEpicTasks().size();

        assertEquals(NullPointerException.class, executeDeleteEpic(manager, 0).getClass());
        assertEquals(NullPointerException.class, executeDeleteEpic(manager, -1).getClass());
        assertEquals(NullPointerException.class, executeDeleteEpic(manager, 9999).getClass());

        assertEquals(sizeBefore, manager.getEpicTasks().size());
        assertNotNull(manager.getEpic(testEpic.getTaskId()));
    }

    public void generateShouldDeleteSubTaskById(T manager) {
        EpicTask testEpic = createTestEpic(manager);
        SubTask testSub = createTestSub(manager, testEpic, StatusOfTask.DONE);
        int sizeBefore = manager.getSubTasks().size();
        manager.deleteSubById(testSub.getTaskId());

        assertEquals(sizeBefore - 1, manager.getSubTasks().size());
        assertNull(manager.getSub(testSub.getTaskId()));
    }

    public void generateShouldDeleteSubTaskByIdWithEmptySubsList(T manager) {
        manager.deleteSubTasks();

        EpicTask testEpic = createTestEpic(manager);
        SubTask testSub = createTestSub(manager, testEpic, StatusOfTask.DONE);
        int sizeBefore = manager.getSubTasks().size();
        manager.deleteSubById(testSub.getTaskId());

        assertEquals(sizeBefore - 1, manager.getSubTasks().size());
        assertNull(manager.getSub(testSub.getTaskId()));
    }

    public void generateShouldNotDeleteSubByWrongId(T manager) {
        EpicTask testEpic = createTestEpic(manager);
        SubTask testSub = createTestSub(manager, testEpic, StatusOfTask.DONE);
        int sizeBefore = manager.getSubTasks().size();

        assertEquals(NullPointerException.class, executeDeleteSub(manager, 0).getClass());
        assertEquals(NullPointerException.class, executeDeleteSub(manager, -1).getClass());
        assertEquals(NullPointerException.class, executeDeleteSub(manager, 9999).getClass());

        assertEquals(sizeBefore, manager.getSubTasks().size());
        assertNotNull(manager.getSub(testSub.getTaskId()));
    }

    public void generateShouldNotCreateTasksWhenTimeIsBusy(T manager) {
        manager.deleteTasks();
        manager.deleteEpicTasks();
        manager.deleteSubTasks();

        Task testTask = createTestTask(manager);

        assertEquals(testTask, manager.getTask(testTask.getTaskId()));
        assertNull(manager.getEpic(testTask.getTaskId()));
        assertNull(manager.getSub(testTask.getTaskId()));

        createTestTask(manager);
    }
}