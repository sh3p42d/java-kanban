package Tasks;

import Manager.FileBackedTasksManager;

// Генератор задач для тестирования
public class TasksForTest {
    private static final Task task1 = new Task("First Simple",
            "task id = 1", StatusOfTask.NEW);
    private static final Task task2 = new Task("Second Simple",
            "task id = 2", StatusOfTask.DONE);
    private static final EpicTask epicTask1 = new EpicTask("First Epic",
            "epic id = 3");
    private static final EpicTask epicTask2 = new EpicTask("Second Epic",
            "epic id = 4");
    private static final SubTask subTask1 = new SubTask("First Sub",
            "sub id = 5", StatusOfTask.NEW, 4);
    private static final SubTask subTask2 = new SubTask("Second Sub",
            "sub id = 6", StatusOfTask.IN_PROGRESS, 4);
    private static final SubTask subTask3 = new SubTask("Third Sub",
            "sub id = 7", StatusOfTask.DONE, 3);
    private static final Task task3 = new Task("подумать про сортировку",
            "task id = 8", StatusOfTask.NEW);
    private static final Task task4 = new Task("what am i doing",
            "task id = 9", StatusOfTask.DONE);
    private static final EpicTask epicTask3 = new EpicTask("Третий Epic",
            "epic id = 10");
    private static final SubTask subTask4 = new SubTask("Четвертый Sub",
            "sub id = 11", StatusOfTask.NEW, 4);

    public static void crudTasks(FileBackedTasksManager manager) {
        // Создаем задачи
        manager.createTask(task1);
        manager.createTask(task2);
        manager.createEpicTask(epicTask1);
        manager.createEpicTask(epicTask2);
        subTask1.setEpicId(epicTask2.getTaskId());
        subTask2.setEpicId(epicTask2.getTaskId());
        subTask3.setEpicId(epicTask1.getTaskId());
        manager.createSubTask(subTask1);
        manager.createSubTask(subTask2);
        manager.createSubTask(subTask3);
        // Задачи на которые не будем смотреть
        manager.createTask(task3);
        manager.createTask(task4);
        manager.createEpicTask(epicTask3);
        manager.createSubTask(subTask4);

        // Выводим задачу по ID с записью в историю
        manager.getTask(1);
        manager.getTask(2);
        manager.getTask(1);
        manager.getTask(1);
        manager.getEpic(3);
        manager.getEpic(3);
        manager.getEpic(4);
        manager.getEpic(4);
        manager.getSub(5);
        manager.getSub(5);
        manager.getSub(6);
        manager.getSub(7);
        manager.getSub(6);

        // Обновляем задачи
        task2.setTaskName("изменил имя у task2");
        manager.updateTask(task2);
        epicTask1.setTaskDescription("изменил описание у epicTask1");
        manager.updateEpicTask(epicTask1);

        // Удаление задач
        manager.deleteTaskById(1);
        manager.deleteSubById(5);
        manager.deleteEpicById(4);
    }
}
