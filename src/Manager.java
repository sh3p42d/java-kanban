import java.util.ArrayList;
import java.util.HashMap;

public class Manager {
    private int nextId = 1;
    private HashMap<Integer, Task> taskMap = new HashMap<>();
    private HashMap<Integer, EpicTask> epicMap = new HashMap<>();
    private HashMap<Integer, SubTask> subMap = new HashMap<>();

    // Получение списка задач каждого типа
    public ArrayList<String> getTasks() {
        ArrayList<String> allTasks = new ArrayList<>();
        allTasks.add(taskMap.values().toString());
        return allTasks;
    }

    public ArrayList<String> getEpicTasks() {
        ArrayList<String> allEpicTasks = new ArrayList<>();
        allEpicTasks.add(epicMap.values().toString());
        return allEpicTasks;
    }

    public ArrayList<String> getSubTasks() {
        ArrayList<String> allSubTasks = new ArrayList<>();
        allSubTasks.add(subMap.values().toString());
        return allSubTasks;
    }

    // Удаление всех задач каждого типа
    public void deleteTasks() {
        taskMap.clear();
    }

    public void deleteEpicTasks() {
        // Удаляем подзадачи из эпиков
        deleteSubTasks();
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
            updateEpicTask(epic);
        }
    }

    // Получение задачи каждого типа по ID
    public Task getOneTask(Integer id) {
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
        subTask.setTaskId(nextId);
        nextId++;
        subMap.put(subTask.getTaskId(), subTask);
        updateSubTask(subTask);

        return subTask.getTaskId();
    }

    // Обновление задач каждого типа
    public void updateTask(Task task) {
        taskMap.put(task.getTaskId(), task);
    }

    public void updateEpicTask(EpicTask epicTask) {
        // Рассчитаем изменение статуса для эпика
        int status = 0;
        for (Integer subId : epicTask.getSubIds()) {
            SubTask anySub = subMap.get(subId);

            if (anySub.getTaskStatus().equals("IN_PROGRESS")) {
                // Если есть хотя бы одна подзадача в IN_PROGRESS - эпик тоже IN_PROGRESS
                status = 0;
                break;
            } else if (anySub.getTaskStatus().equals("DONE")) {
                status++;
            } else {
                status--;
            }
        }

        // Если имеем одинаковое количество задач NEW и DONE, то status == 0, а эпик считается IN_PROGRESS
        if (status == 0 && epicTask.subIds.size() != 0) {
            epicTask.setTaskStatus("IN_PROGRESS");
        } else if (status > 0) {
            epicTask.setTaskStatus("DONE");
        } else {
            // Именно NEW в else, т.к. эпик может быть без подзадач (epicTask.subIds = [])
            epicTask.setTaskStatus("NEW");
        }

        epicMap.put(epicTask.getTaskId(), epicTask);
    }

    public void updateSubTask(SubTask subTask) {
        // Подключаемся к текущему эпику подзадачи
        int currentEpicId = subTask.getEpicId();
        EpicTask epicTask = epicMap.get(currentEpicId);
        int subId = subTask.getTaskId();

        // Проверяем была ли эта подзадача в другом эпике
        for (EpicTask epic : epicMap.values()) {
            if (epic.getSubIds().contains(subId) && epic.getTaskId() != currentEpicId) {
                // Если была, то удаляем
                ArrayList<Integer> epicSubIds = epic.getSubIds();
                epicSubIds.remove(Integer.valueOf(subId));
                epic.setSubIds(epicSubIds);
                // Записываем новый статус для эпика
                updateEpicTask(epic);
            }
        }

        // Записываем обновленную подзадачу в HashMap подзадач
        subMap.put(subTask.getTaskId(), subTask);

        // Если это первая подзадача или она сменила эпик, то связываем их
        if (!epicTask.getSubIds().contains(subId)) {
            ArrayList<Integer> epicSubIds = epicTask.getSubIds();
            epicSubIds.add(subId);
            epicTask.setSubIds(epicSubIds);
        }

        // Записываем новый статус для эпика
        updateEpicTask(epicTask);
    }

    // Удаление задачи по ID
    public Integer deleteById(Integer id) {
        if (taskMap.containsKey(id)) {
            taskMap.remove(id);
        } else if (epicMap.containsKey(id)) {
            // Удаляем подзадачи, если они есть
            EpicTask epic = epicMap.get(id);
            ArrayList<Integer> subIds = new ArrayList<Integer>(epic.getSubIds());
            for (Integer subId : subIds) {
                deleteById(subId);
            }
            // Удаляем эпик из списка
            epicMap.remove(id);
        } else if (subMap.containsKey(id)){
            // Удаляем подзадачу из эпика
            int currentEpicId = subMap.get(id).getEpicId();
            EpicTask epic = epicMap.get(currentEpicId);
            ArrayList<Integer> epicSubIds = epic.getSubIds();
            if (epicSubIds.contains(id)) {
                epicSubIds.remove(id);
                epic.setSubIds(epicSubIds);
                // и обновляем эпик
                updateEpicTask(epic);
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
    public ArrayList<String> getSubFromEpic(Integer epicId) {
        ArrayList<String> subTasks = new ArrayList<>();
        EpicTask epicTask = epicMap.get(epicId);
        ArrayList<Integer> subTasksIds = epicTask.getSubIds();

        for (int subTaskId : subTasksIds) {
            subTasks.add(subMap.get(subTaskId).toString());
        }

        return subTasks;
    }
}
