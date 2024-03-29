import Manager.*;
import Tasks.EpicTask;
import Tasks.StatusOfTask;
import Tasks.SubTask;
import Tasks.Task;

import java.io.IOException;

import static Manager.HttpTaskManager.loadFromClient;

public class Main {
    public static void main(String[] args) throws IOException {
        new KVServer().start();
        HttpTaskManager manager = (HttpTaskManager) Managers.getDefault("http://localhost:8078");

        Task task1 = new Task("First Simple", "First description", StatusOfTask.NEW);
        Task task2 = new Task("Second Simple", "Second description", StatusOfTask.DONE);
        EpicTask epicTask1 = new EpicTask("First Epic", "1st Epic");
        EpicTask epicTask2 = new EpicTask("Second Epic", "2nd Epic");
        SubTask subTask1 = new SubTask("First Sub", "1st Sub", StatusOfTask.NEW, 4);
        SubTask subTask2 = new SubTask("Second Sub", "2nd Sub", StatusOfTask.IN_PROGRESS, 4);
        SubTask subTask3 = new SubTask("Third Sub", "3rd Sub", StatusOfTask.DONE, 4);

        // Создание задач менеджером
        System.out.println("Создана задача с ID = " + manager.createTask(task1));
        System.out.println("Создана задача с ID = " + manager.createTask(task2));
        System.out.println("Создан эпик с ID = " + manager.createEpicTask(epicTask1));
        System.out.println("Создан эпик с ID = " + manager.createEpicTask(epicTask2));
        subTask1.setEpicId(epicTask2.getTaskId());
        subTask2.setEpicId(epicTask2.getTaskId());
        subTask3.setEpicId(epicTask2.getTaskId());
        System.out.println("Создана подзадача с ID = " + manager.createSubTask(subTask1));
        System.out.println("Создана подзадача с ID = " + manager.createSubTask(subTask2));
        System.out.println("Создана подзадача с ID = " + manager.createSubTask(subTask3));

        // Выводим задачу по ID с записью в историю
        System.out.println("Посмотрим на задачи");
        manager.getTask(1);
        manager.getTask(2);
        manager.getTask(1);
        manager.getTask(1);

        System.out.println("Посмотрим на эпики");
        manager.getEpic(3);
        manager.getEpic(3);
        manager.getEpic(4);
        manager.getEpic(4);

        System.out.println("Посмотрим на подзадачи");
        manager.getSub(5);
        manager.getSub(5);
        manager.getSub(6);
        manager.getSub(7);
        manager.getSub(6);

        // Удаление задач
        System.out.println("Удаляем задачу с id = " + manager.deleteTaskById(1));
        System.out.println("Удаляем подзадачу с id = " +  manager.deleteSubById(5));
        System.out.println("Удаляем эпик с id = " + manager.deleteEpicById(4));

        System.out.println(manager.getTasks());
        System.out.println(manager.getEpicTasks());
        System.out.println(manager.getSubTasks());
        System.out.println(manager.getHistory());

        manager = loadFromClient("http://localhost:8078");
        manager.createTask(task1);
        manager.createTask(task2);
        manager.createEpicTask(epicTask1);

        System.out.println(manager.getTasks());
        System.out.println(manager.getEpicTasks());
        System.out.println(manager.getSubTasks());
        System.out.println(manager.getHistory());
    }
}