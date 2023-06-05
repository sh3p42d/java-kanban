package Manager;

import Tasks.EpicTask;
import Tasks.SubTask;
import Tasks.Task;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface TaskManager {
    Map<Integer, Task> getTaskMap();
    Map<Integer, EpicTask> getEpicMap();
    Map<Integer, SubTask> getSubMap();

    // Получение списка задач каждого типа
    List<Task> getTasks();
    List<EpicTask> getEpicTasks();
    List<SubTask> getSubTasks();

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

    // Очистка истории
    void clearHistory();

    // Получение списка задач по приоритетам
    Set<Task> getPrioritizedTasks();

    // Получение истории
    List<Task> getHistory();
}
