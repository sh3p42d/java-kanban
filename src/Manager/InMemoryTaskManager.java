package Manager;

import Tasks.EpicTask;
import Tasks.StatusOfTask;
import Tasks.SubTask;
import Tasks.Task;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InMemoryTaskManager implements TaskManager {
    private int nextId = 1;
    private final Map<Integer, Task> taskMap = new HashMap<>();
    private final Map<Integer, EpicTask> epicMap = new HashMap<>();
    private final Map<Integer, SubTask> subMap = new HashMap<>();
    private final HistoryManager managerHistory = Managers.getDefaultHistory();

    // Получение списка задач каждого типа
    @Override
    public List<Task> getTasks() {
        return new ArrayList<>(taskMap.values());
    }

    @Override
    public List<EpicTask> getEpicTasks() {
        return new ArrayList<>(epicMap.values());
    }

    @Override
    public List<SubTask> getSubTasks() {
        return new ArrayList<>(subMap.values());
    }

    // Удаление всех задач каждого типа
    @Override
    public void deleteTasks() {
        for (Task task : taskMap.values()) {
            managerHistory.removeOneTaskHistory(task.getTaskId());
        }
        taskMap.clear();
    }

    @Override
    public void deleteEpicTasks() {
        for (SubTask subTask : subMap.values()) {
            managerHistory.removeOneTaskHistory(subTask.getTaskId());
        }

        for (EpicTask epicTask : epicMap.values()) {
            managerHistory.removeOneTaskHistory(epicTask.getTaskId());
        }

        subMap.clear();
        epicMap.clear();
    }

    @Override
    public void deleteSubTasks() {
        for (SubTask subTask : subMap.values()) {
            managerHistory.removeOneTaskHistory(subTask.getTaskId());
        }

        subMap.clear();
        for (EpicTask epic : epicMap.values()) {
            epic.setSubIds(new ArrayList<>());
            epic.setTaskStatus(StatusOfTask.NEW);
        }
    }

    // Получение задачи каждого типа по ID с записью в историю просмотров
    @Override
    public Task getTask(Integer id) {
        // Не совсем понял как вынести получение задачи в одну переменную, если у меня три разных мапы
        managerHistory.add(taskMap.get(id));
        return taskMap.get(id);
    }

    @Override
    public EpicTask getEpic(Integer id) {
        managerHistory.add(epicMap.get(id));
        return epicMap.get(id);
    }

    @Override
    public SubTask getSub(Integer id) {
        managerHistory.add(subMap.get(id));
        return subMap.get(id);
    }

    // Создание каждого типа задач
    @Override
    public int createTask(Task task) {
        task.setTaskId(nextId);
        nextId++;
        taskMap.put(task.getTaskId(), task);
        return task.getTaskId();
    }

    @Override
    public int createEpicTask(EpicTask epicTask) {
        epicTask.setTaskId(nextId);
        nextId++;
        epicMap.put(epicTask.getTaskId(), epicTask);
        return epicTask.getTaskId();
    }

    @Override
    public int createSubTask(SubTask subTask) {
        subTask.setTaskId(nextId);
        nextId++;
        subMap.put(subTask.getTaskId(), subTask);

        int epicId = subTask.getEpicId();
        EpicTask epicTask = epicMap.get(epicId);
        epicTask.addSub(subTask.getTaskId());
        updateEpicStatus(epicTask);

        return subTask.getTaskId();
    }

    // Обновление задач каждого типа
    @Override
    public void updateTask(Task task) {
        taskMap.put(task.getTaskId(), task);
    }

    @Override
    public void updateEpicTask(EpicTask epicTask) {
        epicMap.put(epicTask.getTaskId(), epicTask);
    }

    private void updateEpicStatus(EpicTask epicTask) {
        ArrayList<StatusOfTask> statuses = new ArrayList<>();
        for (Integer subId : epicTask.getSubIds()) {
            SubTask anySub = subMap.get(subId);
            statuses.add(anySub.getTaskStatus());
        }
        int newStatus = 0;
        int doneStatus = 0;

        for (StatusOfTask status : statuses) {
            if (status.equals(StatusOfTask.NEW)) {
                newStatus++;
            } else if (status.equals(StatusOfTask.DONE)) {
                doneStatus++;
            } else {
                newStatus = 0;
                doneStatus = 0;
                break;
            }
        }

        if (newStatus == statuses.size()) {
            epicTask.setTaskStatus(StatusOfTask.NEW);
        } else if (doneStatus == statuses.size()) {
            epicTask.setTaskStatus(StatusOfTask.DONE);
        } else {
            epicTask.setTaskStatus(StatusOfTask.IN_PROGRESS);
        }
    }

    @Override
    public void updateSubTask(SubTask subTask) {
        int currentEpicId = subTask.getEpicId();
        EpicTask epicTask = epicMap.get(currentEpicId);
        subMap.put(subTask.getTaskId(), subTask);
        updateEpicStatus(epicTask);
    }

    // Удаление задачи по ID
    @Override
    public Integer deleteTaskById(Integer id) {
        removeTaskHistory(id);
        taskMap.remove(id);
        return id;
    }

    @Override
    public Integer deleteEpicById(Integer id) {
        EpicTask epic = epicMap.remove(id);
        ArrayList<Integer> subIds = new ArrayList<>(epic.getSubIds());
        for (Integer subId : subIds) {
            removeTaskHistory(subId);
            subMap.remove(subId);
        }
        removeTaskHistory(id);
        return id;
    }

    @Override
    public Integer deleteSubById(Integer id) {
        int currentEpicId = subMap.remove(id).getEpicId();
        EpicTask epic = epicMap.get(currentEpicId);
        epic.removeSub(id);
        updateEpicStatus(epic);
        removeTaskHistory(id);
        return id;
    }

    // Получение списка всех подзадач эпика по ID
    @Override
    public ArrayList<SubTask> getSubFromEpic(Integer epicId) {
        ArrayList<SubTask> subTasks = new ArrayList<>();
        EpicTask epicTask = epicMap.get(epicId);
        ArrayList<Integer> subTasksIds = epicTask.getSubIds();

        for (int subTaskId : subTasksIds) {
            subTasks.add(subMap.get(subTaskId));
        }
        return subTasks;
    }

    // История просмотров
    public List<Task> getHistory() {
        return managerHistory.getHistory();
    }

    public void removeTaskHistory(Integer id) {
        managerHistory.removeOneTaskHistory(id);
    }
}
