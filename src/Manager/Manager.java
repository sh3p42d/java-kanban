package Manager;

import Tasks.EpicTask;
import Tasks.SubTask;
import Tasks.Task;

import java.util.ArrayList;
import java.util.HashMap;

public class Manager {
    private int nextId = 1;
    private HashMap<Integer, Task> taskMap = new HashMap<>();
    private HashMap<Integer, EpicTask> epicMap = new HashMap<>();
    private HashMap<Integer, SubTask> subMap = new HashMap<>();

    // Получение списка задач каждого типа
    public ArrayList<Task> getTasks() {
        ArrayList<Task> allTasks = new ArrayList<>();
        allTasks.addAll(taskMap.values());
        return allTasks;
    }

    public ArrayList<EpicTask> getEpicTasks() {
        ArrayList<EpicTask> allEpicTasks = new ArrayList<>();
        allEpicTasks.addAll(epicMap.values());
        return allEpicTasks;
    }

    public ArrayList<SubTask> getSubTasks() {
        ArrayList<SubTask> allSubTasks = new ArrayList<>();
        allSubTasks.addAll(subMap.values());
        return allSubTasks;
    }

    // Удаление всех задач каждого типа
    public void deleteTasks() {
        taskMap.clear();
    }

    public void deleteEpicTasks() {
        // Удаляем подзадачи
        subMap.clear();
        // Удаляем эпики
        epicMap.clear();
    }

    public void deleteSubTasks() {
        subMap.clear();
        // У всех эпиков будет статус "NEW"
        for (EpicTask epic : epicMap.values()) {
            ArrayList<Integer> epicSubIds = epic.getSubIds();
            epicSubIds.clear();
            epic.setSubIds(epicSubIds);
            epic.setTaskStatus("NEW");
        }
    }

    // Получение задачи каждого типа по ID
    public Task getOneTask(Integer id) {
        if (taskMap.containsKey(id)) {
            return taskMap.get(id);
        } else {
            // Заглушка на случай попытки вернуть задачу, когда их вообще нет
            System.out.println(" ");
            return null;
        }
    }

    public Task getOneEpicTask(Integer id) {
        if (epicMap.containsKey(id)) {
            return epicMap.get(id);
        } else {
            // Заглушка на случай попытки вернуть задачу, когда их вообще нет
            System.out.println(" ");
            return null;
        }
    }

    public Task getOneSubTask(Integer id) {
        if (taskMap.containsKey(id)) {
            return taskMap.get(id);
        } else if (epicMap.containsKey(id)) {
            return epicMap.get(id);
        } else if (subMap.containsKey(id)){
            return subMap.get(id);
        } else {
            // Заглушка на случай попытки вернуть задачу, когда их вообще нет
            System.out.println(" ");
            return null;
        }
    }

    // Создание каждого типа задач
    public int createTask(Task task) {
        task.setTaskId(nextId);
        nextId++;
        taskMap.put(task.getTaskId(), task);
        return task.getTaskId();
    }

    public int createEpicTask(EpicTask epicTask) {
        epicTask.setTaskId(nextId);
        nextId++;
        epicMap.put(epicTask.getTaskId(), epicTask);
        return epicTask.getTaskId();
    }

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
    public void updateTask(Task task) {
        taskMap.put(task.getTaskId(), task);
    }

    // Разбил метод для эпиков на два
    public void updateEpicTask(EpicTask epicTask) {
        epicMap.put(epicTask.getTaskId(), epicTask);
    }

    private void updateEpicStatus(EpicTask epicTask) {
        // Рассчитаем изменение статуса для эпика
        ArrayList<String> statuses = new ArrayList<>();
        for (Integer subId : epicTask.getSubIds()) {
            SubTask anySub = subMap.get(subId);
            statuses.add(anySub.getTaskStatus());
        }
        int newStatus = 0;
        int doneStatus = 0;

        for (String status : statuses) {
            if (status == "NEW") {
                newStatus++;
            } else if (status == "DONE") {
                doneStatus++;
            } else {
                newStatus = 0;
                doneStatus = 0;
                break;
            }
        }

        if (newStatus == statuses.size()) {
            epicTask.setTaskStatus("NEW");
        } else if (doneStatus == statuses.size()) {
            epicTask.setTaskStatus("DONE");
        } else {
            epicTask.setTaskStatus("IN_PROGRESS");
        }
        epicMap.put(epicTask.getTaskId(), epicTask);
    }

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
    public Integer deleteTaskById(Integer id) {
        if (taskMap.containsKey(id)) {
            taskMap.remove(id);
        } else {
            // Заглушка на случай попытки удалить задачу, когда их вообще нет
            System.out.println(" ");
        }
        return id;
    }

    public Integer deleteEpicById(Integer id) {
        if (epicMap.containsKey(id)) {
            // Удаляем подзадачи, если они есть
            EpicTask epic = epicMap.get(id);
            ArrayList<Integer> subIds = new ArrayList<Integer>(epic.getSubIds());
            for (Integer subId : subIds) {
                deleteSubById(subId);
            }
            // Удаляем эпик из списка
            epicMap.remove(id);
        } else {
            // Заглушка на случай попытки удалить задачу, когда их вообще нет
            System.out.println(" ");
        }
        return id;
    }

    public Integer deleteSubById(Integer id) {
        if (subMap.containsKey(id)){
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
        } else {
            // Заглушка на случай попытки удалить задачу, когда их вообще нет
            System.out.println(" ");
        }
        return id;
    }

    // Получение списка всех подзадач эпика по ID
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
