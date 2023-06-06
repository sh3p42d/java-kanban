package Manager;

import Tasks.EpicTask;
import Tasks.StatusOfTask;
import Tasks.SubTask;
import Tasks.Task;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

public class InMemoryTaskManager implements TaskManager {
    private int nextId = 1;
    protected Map<Integer, Task> taskMap = new HashMap<>();
    protected Map<Integer, EpicTask> epicMap = new HashMap<>();
    protected Map<Integer, SubTask> subMap = new HashMap<>();
    protected final Set<Task> prioritizedTasks = new TreeSet<>(Comparator.comparing(Task::getStartTime,
            Comparator.nullsLast(Comparator.naturalOrder())));
    private HistoryManager managerHistory = Managers.getDefaultHistory();

    private boolean checkTimeBusy(Task task) {
        boolean busy = true;

        if (task.getStartTime() == null || prioritizedTasks.isEmpty()) {
            return false;
        }

        LocalDateTime taskStart = task.getStartTime();
        LocalDateTime taskEnd = task.getStartTime().plusMinutes(task.getDuration().toMinutes());

        for (Task setTask : getPrioritizedTasks()) {
            LocalDateTime setTaskStart = null;
            LocalDateTime setTaskEnd = null;

            if (setTask.getStartTime() != null && setTask.getDuration() != null) {
                setTaskStart = setTask.getStartTime();
                setTaskEnd = setTask.getStartTime().plusMinutes(task.getDuration().toMinutes());
            }

            if (setTask.getStartTime() == null) {
                busy = false;
            // Данная проверка необходима, при обновлении полей задач/подзадач
            } else if (Objects.equals(setTaskStart, taskStart) &&
                    Objects.equals(setTaskEnd, taskEnd) &&
                    setTask.getTaskId() == task.getTaskId()) {

                busy = false;
                break;
            } else {
                if (taskStart.isAfter(setTaskEnd) || taskEnd.isBefore(setTaskStart)) {
                    busy = false;
                } else {
                    busy = true;
                    System.out.println("Время задачи: " + task);
                    System.out.println("пересекается с другой: " + setTask);
                    break;
                }
            }
        }
        return busy;
    }

    public Set<Task> getPrioritizedTasks() {
        return prioritizedTasks;
    }

    public int getNextId() {
        return nextId;
    }

    public void setNextId(int nextId) {
        this.nextId = nextId;
    }

    protected Map<Integer, Task> getTaskMap() {
        return taskMap;
    }

    protected Map<Integer, EpicTask> getEpicMap() {
        return epicMap;
    }

    protected Map<Integer, SubTask> getSubMap() {
        return subMap;
    }

    protected HistoryManager getManagerHistory() {
        return managerHistory;
    }

    protected void setManagerHistory(HistoryManager managerHistory) {
        this.managerHistory = managerHistory;
    }

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
            prioritizedTasks.remove(task);
        }
        taskMap.clear();
    }

    @Override
    public void deleteEpicTasks() {
        for (SubTask subTask : subMap.values()) {
            managerHistory.removeOneTaskHistory(subTask.getTaskId());
            prioritizedTasks.remove(subTask);
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
            prioritizedTasks.remove(subTask);
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
        Task task = taskMap.get(id);
        managerHistory.add(task);
        return task;
    }

    @Override
    public EpicTask getEpic(Integer id) {
        EpicTask epic = epicMap.get(id);
        managerHistory.add(epic);
        return epic;
    }

    @Override
    public SubTask getSub(Integer id) {
        SubTask sub = subMap.get(id);
        managerHistory.add(sub);
        return sub;
    }

    // Создание каждого типа задач
    @Override
    public int createTask(Task task) {
        if (!checkTimeBusy(task)) {
            task.setTaskId(nextId);
            nextId++;
            taskMap.put(task.getTaskId(), task);
            prioritizedTasks.add(task);
        }
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
        if (!checkTimeBusy(subTask)) {
            subTask.setTaskId(nextId);
            nextId++;
            subMap.put(subTask.getTaskId(), subTask);
            prioritizedTasks.add(subTask);

            int epicId = subTask.getEpicId();
            EpicTask epicTask = epicMap.get(epicId);
            epicTask.addSub(subTask.getTaskId());
            updateEpicStatus(epicTask);
            updateEpicTime(epicTask);
        }
        return subTask.getTaskId();
    }

    // Обновление задач каждого типа
    @Override
    public void updateTask(Task task) {
        if (!checkTimeBusy(task)) {
            taskMap.put(task.getTaskId(), task);
        }
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
        if (!checkTimeBusy(subTask)) {
            int currentEpicId = subTask.getEpicId();
            EpicTask epicTask = epicMap.get(currentEpicId);
            subMap.put(subTask.getTaskId(), subTask);
            updateEpicStatus(epicTask);
            updateEpicTime(epicTask);
        }
    }

    // Удаление задачи по ID
    @Override
    public Integer deleteTaskById(Integer id) {
        removeTaskHistory(id);
        prioritizedTasks.remove(taskMap.remove(id));
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
        prioritizedTasks.remove(getSub(id));
        int currentEpicId = subMap.remove(id).getEpicId();
        EpicTask epic = epicMap.get(currentEpicId);
        epic.removeSub(id);
        updateEpicStatus(epic);
        updateEpicTime(epic);
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

    private void updateEpicTime(EpicTask epic) {
        ArrayList<LocalDateTime> starts = new ArrayList<>();
        ArrayList<Duration> durations = new ArrayList<>();

        for (Integer subId : epic.getSubIds()) {
            SubTask anySub = subMap.get(subId);
            if (anySub.getStartTime() != null) {
                starts.add(anySub.getStartTime());
                durations.add(anySub.getDuration());
            }
        }

        try {
            if (!starts.isEmpty()) {
                epic.setStartTime(Collections.min(starts));
                epic.setDuration(Duration.ofMinutes(
                        durations.stream()
                                .mapToLong(Duration::toMinutes)
                                .sum()
                ));
                epic.setEndTime();
            }
        } catch (NullPointerException e) {
            System.out.println("Ошибка добавления времени в эпик");
        }
    }

    // История просмотров
    public List<Task> getHistory() {
        return managerHistory.getHistory();
    }

    @Override
    public void clearHistory() {
        managerHistory = Managers.getDefaultHistory();
    }

    private void removeTaskHistory(Integer id) {
        managerHistory.removeOneTaskHistory(id);
    }
}
