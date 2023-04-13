package Manager;

import Tasks.EpicTask;
import Tasks.SubTask;
import Tasks.Task;
import java.util.ArrayList;

public interface TaskManager {
    // Получение списка задач каждого типа
    ArrayList<Task> getTasks();
    ArrayList<EpicTask> getEpicTasks();
    ArrayList<SubTask> getSubTasks();

    // Удаление всех задач каждого типа
    void deleteTasks();
    void deleteEpicTasks();
    void deleteSubTasks();

    // Получение задачи каждого типа по ID
    Task getTask(Integer id);
    EpicTask getEpic(Integer id);
    SubTask getSub(Integer id);
    // Получение списка всех подзадач эпика по ID
    ArrayList<SubTask> getSubFromEpic(Integer epicId);

    // Создание каждого типа задач
    int createTask(Task task);
    int createEpicTask(EpicTask epicTask);
    int createSubTask(SubTask subTask);

    // Обновление задач каждого типа
    void updateTask(Task task);
    void updateEpicTask(EpicTask epicTask);
    void updateSubTask(SubTask subTask);

    // Удаление задачи по ID
    Integer deleteTaskById(Integer id);
    Integer deleteEpicById(Integer id);
    Integer deleteSubById(Integer id);
}
