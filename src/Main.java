import Manager.Manager;
import Tasks.EpicTask;
import Tasks.SubTask;
import Tasks.Task;

import java.util.Scanner;

public class Main {

    // Для проверок
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        Manager manager = new Manager();

        Task task1 = new Task("First Simple", "First description", "NEW");
        Task task2 = new Task("Second Simple", "Second description", "DONE");
        EpicTask epicTask1 = new EpicTask("First Epic", "1st Epic");
        EpicTask epicTask2 = new EpicTask("Second Epic", "2nd Epic");
        // Т.к. создание подзадач вне основного цикла программы, ID эпика захардкодил
        SubTask subTask1 = new SubTask("First Sub", "1st Sub", "NEW", 4);
        SubTask subTask2 = new SubTask("Second Sub", "2nd Sub", "IN_PROGRESS", 4);
        SubTask subTask3 = new SubTask("Third Sub", "3rd Sub", "DONE", 4);

        while (true) {
            printMenu();
            int command = scanner.nextInt();

            if (command == 1) { // Создание задач менеджером
                System.out.println("Создана задача с ID = " + manager.createTask(task1));
                System.out.println("Создана задача с ID = " + manager.createTask(task2));
                System.out.println("Создана задача с ID = " + manager.createEpicTask(epicTask1));
                System.out.println("Создана задача с ID = " + manager.createEpicTask(epicTask2));
                subTask1.setEpicId(epicTask2.getTaskId());
                subTask2.setEpicId(epicTask2.getTaskId());
                subTask3.setEpicId(epicTask2.getTaskId());
                System.out.println("Создана задача с ID = " + manager.createSubTask(subTask1));
                System.out.println("Создана задача с ID = " + manager.createSubTask(subTask2));
                System.out.println("Создана задача с ID = " + manager.createSubTask(subTask3));
            } else if (command == 2) { // Выводим все задачи
                System.out.println(manager.getTasks());
                System.out.println(manager.getEpicTasks());
                System.out.println(manager.getSubTasks());
            } else if (command == 3) { // Выводим задачу по ID
                System.out.println("Введите ID задачи:");
                int id = scanner.nextInt();
                System.out.println(manager.getOneTask(id));
                System.out.println("Введите ID эпика:");
                id = scanner.nextInt();
                System.out.println(manager.getOneEpicTask(id));
                System.out.println("Введите ID подзадачи:");
                id = scanner.nextInt();
                System.out.println(manager.getOneSubTask(id));
            } else if (command == 4) { // Выводим подзадачи эпика
                System.out.println("Введите ID эпика:");
                int id = scanner.nextInt();
                System.out.println(manager.getSubFromEpic(id));
            } else if (command == 5) { // Обновляем задачи
                // По ТЗ менеджер не может напрямую менять поля, поэтому записываем новые значения сами и передаем
                task1.setTaskDescription("New");
                manager.updateTask(task1);

                epicTask1.setTaskName("Epic First");
                manager.updateEpicTask(epicTask1);

                subTask2.setTaskStatus("DONE");
                subTask2.setEpicId(epicTask1.getTaskId());
                manager.updateSubTask(subTask2);
            } else if (command == 6) { // Удаляем все задачи
                manager.deleteTasks();
                manager.deleteEpicTasks();
                manager.deleteSubTasks();
            } else if (command == 7) { // Удаляем задачи по ID
                System.out.println("Введите ID задачи:");
                int id = scanner.nextInt();
                System.out.println("Задачи с ID = " + manager.deleteTaskById(id) + " больше нет.");

                System.out.println("Введите ID эпика:");
                id = scanner.nextInt();
                System.out.println("Задачи с ID = " + manager.deleteEpicById(id) + " больше нет.");

                System.out.println("Введите ID подзадачи:");
                id = scanner.nextInt();
                System.out.println("Задачи с ID = " + manager.deleteSubById(id) + " больше нет.");
            } else if (command == 0) {
                System.out.println("Выход");
                break;
            } else {
                System.out.println("Такой команды нет.");
            }
        }
    }

    public static void printMenu() {
        System.out.println("Что вы хотите сделать? ");
        System.out.println("1 - Создать задачи");
        System.out.println("2 - Посмотреть все задачи");
        System.out.println("3 - Посмотреть одну задачу по ID");
        System.out.println("4 - Показать подзадачи эпика");
        System.out.println("5 - Обновить задачи");
        System.out.println("6 - Удалить задачи");
        System.out.println("7 - Удалить задачу по ID");
        System.out.println("0 - Выход");
    }
}
