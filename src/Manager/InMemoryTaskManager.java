package Manager;

import Tasks.EpicTask;
import Tasks.StatusOfTask;
import Tasks.SubTask;
import Tasks.Task;
import java.util.ArrayList;
import java.util.HashMap;

public class InMemoryTaskManager implements TaskManager {
    private int nextId = 1;
    private final HashMap<Integer, Task> taskMap = new HashMap<>();
    private final HashMap<Integer, EpicTask> epicMap = new HashMap<>();
    private final HashMap<Integer, SubTask> subMap = new HashMap<>();

    public InMemoryHistoryManager managerHistory = (InMemoryHistoryManager) Managers.getDefaultHistory();

    // Получение списка задач каждого типа
    @Override
    public ArrayList<Task> getTasks() {
        return new ArrayList<>(taskMap.values());
    }

    @Override
    public ArrayList<EpicTask> getEpicTasks() {
        return new ArrayList<>(epicMap.values());
    }

    @Override
    public ArrayList<SubTask> getSubTasks() {
        return new ArrayList<>(subMap.values());
    }

    // Удаление всех задач каждого типа
    @Override
    public void deleteTasks() {
        taskMap.clear();
    }

    @Override
    public void deleteEpicTasks() {
        // Удаляем подзадачи
        subMap.clear();
        // Удаляем эпики
        epicMap.clear();
    }

    @Override
    public void deleteSubTasks() {
        subMap.clear();
        // У всех эпиков будет статус "NEW"
        for (EpicTask epic : epicMap.values()) {
            ArrayList<Integer> epicSubIds = epic.getSubIds();
            epicSubIds.clear();
            epic.setSubIds(epicSubIds);
            epic.setTaskStatus(StatusOfTask.NEW);
        }
    }

    // Получение задачи каждого типа по ID
    // Запись в историю просмотров
    @Override
    public Task getTask(Integer id) {
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
        // добавляем подзадачу в список
        subTask.setTaskId(nextId);
        nextId++;
        subMap.put(subTask.getTaskId(), subTask);

        int epicId = subTask.getEpicId();
        EpicTask epicTask = epicMap.get(epicId);
        // добавляем подзадачу к эпику
        ArrayList<Integer> epicSubIds = epicTask.getSubIds();
        epicSubIds.add(subTask.getTaskId());
        epicTask.setSubIds(epicSubIds);
        // обновляем статус эпика
        updateEpicStatus(epicTask);

        return subTask.getTaskId();
    }

    // Обновление задач каждого типа
    @Override
    public void updateTask(Task task) {
        taskMap.put(task.getTaskId(), task);
    }

    // Разбил метод для эпиков на два
    @Override
    public void updateEpicTask(EpicTask epicTask) {
        epicMap.put(epicTask.getTaskId(), epicTask);
    }

    protected void updateEpicStatus(EpicTask epicTask) {
        // Рассчитаем изменение статуса для эпика
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
        // Подключаемся к текущему эпику подзадачи
        int currentEpicId = subTask.getEpicId();
        EpicTask epicTask = epicMap.get(currentEpicId);

        // Записываем обновленную подзадачу в HashMap подзадач
        subMap.put(subTask.getTaskId(), subTask);

        // Записываем новый статус для эпика
        updateEpicStatus(epicTask);
    }

    // Удаление задачи по ID
    @Override
    public Integer deleteTaskById(Integer id) {
        taskMap.remove(id);
        return id;
    }

    @Override
    public Integer deleteEpicById(Integer id) {
        // Удаляем подзадачи, если они есть
        EpicTask epic = epicMap.get(id);
        ArrayList<Integer> subIds = new ArrayList<>(epic.getSubIds());
        for (Integer subId : subIds) {
            subMap.remove(subId);
        }
        // Удаляем эпик из списка
        epicMap.remove(id);
        return id;
    }

    @Override
    public Integer deleteSubById(Integer id) {
        // Удаляем подзадачу из эпика
        int currentEpicId = subMap.get(id).getEpicId();
        EpicTask epic = epicMap.get(currentEpicId);
        ArrayList<Integer> epicSubIds = epic.getSubIds();
        if (epicSubIds.contains(id)) {
            epicSubIds.remove(id);
            epic.setSubIds(epicSubIds);
            // и обновляем эпик
            updateEpicStatus(epic);
        }
        // Удаляем подзадачу из списка
        subMap.remove(id);
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
}
