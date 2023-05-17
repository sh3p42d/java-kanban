package Manager;

import Tasks.*;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FileBackedTasksManager extends InMemoryTaskManager {
    private final String path;
    private final Map<Integer, Task> taskMap = super.getTaskMap();
    private final Map<Integer, EpicTask> epicMap = super.getEpicMap();
    private final Map<Integer, SubTask> subMap = super.getSubMap();
    private final HistoryManager managerHistory = super.getManagerHistory();

    public FileBackedTasksManager(String path) {
        this.path = path;
    }

    public static void main(String[] args) {
        String filePath = "resources/tasks.csv";

        FileBackedTasksManager writerManager = new FileBackedTasksManager(filePath);
        TasksForTest.crudTasks(writerManager);
        System.out.println("\nЗадачи и история, которые сохранились в файл:");
        System.out.println(writerManager.getTaskMap());
        System.out.println(writerManager.getEpicMap());
        System.out.println(writerManager.getSubMap());
        System.out.println(writerManager.getManagerHistory().getHistory());

        FileBackedTasksManager readerManager = loadFromFile(new File(filePath));
        System.out.println("\nЗадачи и история, которые выгрузили из файла:");
        System.out.println(readerManager.getTaskMap());
        System.out.println(readerManager.getEpicMap());
        System.out.println(readerManager.getSubMap());
        System.out.println(readerManager.getManagerHistory().getHistory());
    }

    // Сохранение задач и истории в файл
    private void save() {
        Path filePath = Path.of(path);
        Map<Integer, Task> allMap = allMapMerge();
        String history = historyToString(managerHistory);
        String fileHead = "id,type,name,status,description,epic\n";

        try (BufferedWriter bw = Files.newBufferedWriter(filePath)) {
            bw.flush();
            bw.append(fileHead);

            if (allMap.isEmpty() && history.isEmpty()) {
                throw new ManagerSaveException("Нет задач и истории для записи");
            } else if (allMap.values().isEmpty()) {
                throw new ManagerSaveException("Нет задач для записи");
            } else if (history.isEmpty()) {
                throw new ManagerSaveException("Нет истории для записи");
            }

            for (Map.Entry<Integer, Task> entry : allMap.entrySet()) {
                bw.append(taskToString(entry.getValue())).append("\n");
            }

            bw.append("\n").append(history);
        } catch (ManagerSaveException e) {
            System.out.println(e.getMessage());
        } catch (IOException e) {
            e.getMessage();
        }
    }

    private Map<Integer, Task> allMapMerge() {
        Map<Integer, Task> allMap = new HashMap<>(taskMap);
        allMap.putAll(epicMap);
        allMap.putAll(subMap);
        return allMap;
    }

    private static String historyToString(HistoryManager manager) {
        StringBuilder history = new StringBuilder();

        List<Task> taskList = manager.getHistory();
        if (!taskList.isEmpty()) {
            for (Task task : taskList) {
                history.append(task.getTaskId()).append(",");
            }
        }

        // Удаляем запятую, если бы хотя бы одна запись
        if (history.length() > 0) {
            return history.substring(0, history.length() - 1);
        }

        return history.toString();
    }

    private String taskToString(Task task) {
        String strTask = task.getTaskId() + "," + getType(task) + "," +
                task.getTaskName() + "," + task.getTaskStatus() + "," + task.getTaskDescription();

        if (getType(task).equals(TasksType.SUBTASK)) {
            strTask = strTask + "," + subMap.get(task.getTaskId()).getEpicId();
        }

        return strTask;
    }

    private TasksType getType(Task task) {
        TasksType type = TasksType.TASK;
        if (task.getClass().toString().equals("class Tasks.EpicTask")) {
            type = TasksType.EPIC;
        } else if (task.getClass().toString().equals("class Tasks.SubTask")) {
            type = TasksType.SUBTASK;
        }
        return type;
    }

    // Загрузка задач и истории из файла
    private static FileBackedTasksManager loadFromFile(File file) {
        FileBackedTasksManager manager = new FileBackedTasksManager(file.getPath());

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            Map<Integer, Task> newTaskMap = new HashMap<>();
            Map<Integer, EpicTask> newEpicMap = new HashMap<>();
            Map<Integer, SubTask> newSubMap = new HashMap<>();
            HistoryManager newManagerHistory = Managers.getDefaultHistory();

            String fileString;
            List<String> taskList = new ArrayList<>();

            while ((fileString = br.readLine()) != null ) {
                taskList.add(fileString);
            }

            // Записываем задачи в нужные Map'ы
            for (int i = 1; i <= (taskList.size() - 3); i++) {
                String taskType = taskList.get(i).split(",")[1];
                Integer taskId = Integer.valueOf(taskList.get(i).split(",")[0]);

                // Устанавливаем NextId для super'а, это исключает перезапись задач,
                // в случае если мы продолжим работу менеджера после загрузки файла
                if (taskId >= manager.getNextId()) {
                    manager.setNextId(taskId + 1);
                }

                if (taskType.equals(TasksType.TASK.toString())) {
                    newTaskMap.put(taskId, fromStringTask(taskList.get(i)));
                } else if (taskType.equals(TasksType.EPIC.toString())) {
                    newEpicMap.put(taskId, fromStringEpic(taskList.get(i)));
                } else {
                    newSubMap.put(taskId, fromStringSub(taskList.get(i)));
                }

                // Когда записали последнюю задачу, добавляем подзадачи в эпики
                // и определяем статусы эпиков
                if (i == (taskList.size() - 3)) {
                    for (SubTask subTask : newSubMap.values()) {
                        EpicTask epic = newEpicMap.get(subTask.getEpicId());
                        epic.addSub(subTask.getTaskId());
                    }
                    updateAllEpicStatusesFromFile(newEpicMap, newSubMap);
                }
            }

            // Считываем историю из файла в менеджера истории
            List<Integer> newHistory = historyFromString(taskList.get(taskList.size() - 1));
            for (Integer taskIdHistory : newHistory) {
                Task historyTask;
                if (newTaskMap.containsKey(taskIdHistory)) {
                    historyTask = newTaskMap.get(taskIdHistory);
                } else if (newEpicMap.containsKey(taskIdHistory)) {
                    historyTask = newEpicMap.get(taskIdHistory);
                } else {
                    historyTask = newSubMap.get(taskIdHistory);
                }
                newManagerHistory.add(historyTask);
            }

            manager.setTaskMap(newTaskMap);
            manager.setEpicMap(newEpicMap);
            manager.setSubMap(newSubMap);
            manager.setManagerHistory(newManagerHistory);
        } catch (IOException e) {
            e.getMessage();
        }
        return manager;
    }

    private static Task fromStringTask(String value) {
        String[] taskString = value.split(",");

        Task task = new Task(taskString[2], taskString[4], statusFromFile(taskString[3]));
        task.setTaskId(Integer.parseInt(taskString[0]));
        return task;
    }

    private static EpicTask fromStringEpic(String value) {
        String[] taskString = value.split(",");

        EpicTask task = new EpicTask(taskString[2], taskString[4]);
        task.setTaskId(Integer.parseInt(taskString[0]));
        return task;
    }

    private static SubTask fromStringSub(String value) {
        String[] taskString = value.split(",");

        SubTask task = new SubTask(taskString[2], taskString[4], statusFromFile(taskString[3]),
                Integer.parseInt(taskString[5]));
        task.setTaskId(Integer.parseInt(taskString[0]));

        return task;
    }

    private static StatusOfTask statusFromFile(String statusFromFile) {
        StatusOfTask status = StatusOfTask.NEW;
        if (statusFromFile.equals("IN_PROGRESS")) {
            status = StatusOfTask.IN_PROGRESS;
        } else if (statusFromFile.equals("DONE")) {
            status = StatusOfTask.DONE;
        }
        return status;
    }

    private static List<Integer> historyFromString(String value) {
        List<Integer> historyList = new ArrayList<>();
        String[] historyString = value.split(",");
        for (String s : historyString) {
            historyList.add(Integer.valueOf(s));
        }
        return historyList;
    }

    private static void updateAllEpicStatusesFromFile(Map<Integer, EpicTask> newEpicMap,
                                                      Map<Integer, SubTask> newSubMap) {
        for (EpicTask epic : newEpicMap.values()) {
            ArrayList<Integer> epicSubIds = epic.getSubIds();
            ArrayList<StatusOfTask> statuses = new ArrayList<>();

            for (Integer epicSubId : epicSubIds) {
                statuses.add(newSubMap.get(epicSubId).getTaskStatus());
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
                epic.setTaskStatus(StatusOfTask.NEW);
            } else if (doneStatus == statuses.size()) {
                epic.setTaskStatus(StatusOfTask.DONE);
            } else {
                epic.setTaskStatus(StatusOfTask.IN_PROGRESS);
            }
        }
    }

    // Добавляем save в методы от класса-родителя InMemoryTaskManager
    // Удаление всех задач каждого типа
    @Override
    public void deleteTasks() {
        super.deleteTasks();
        save();
    }

    @Override
    public void deleteEpicTasks() {
        super.deleteEpicTasks();
        save();
    }

    @Override
    public void deleteSubTasks() {
        super.deleteSubTasks();
        save();
    }

    // Получение задачи каждого типа по ID с записью в историю просмотров
    @Override
    public Task getTask(Integer id) {
        super.getTask(id);
        save();
        return super.getTask(id);
    }

    @Override
    public EpicTask getEpic(Integer id) {
        super.getEpic(id);
        save();
        return super.getEpic(id);
    }

    @Override
    public SubTask getSub(Integer id) {
        super.getSub(id);
        save();
        return super.getSub(id);
    }

    // Создание каждого типа задач
    @Override
    public int createTask(Task task) {
        super.createTask(task);
        save();
        return task.getTaskId();
    }

    @Override
    public int createEpicTask(EpicTask epicTask) {
        super.createEpicTask(epicTask);
        save();
        return epicTask.getTaskId();
    }

    @Override
    public int createSubTask(SubTask subTask) {
        super.createSubTask(subTask);
        save();
        return subTask.getTaskId();
    }

    // Обновление задач каждого типа
    @Override
    public void updateTask(Task task) {
        super.updateTask(task);
        save();
    }

    @Override
    public void updateEpicTask(EpicTask epicTask) {
        super.updateEpicTask(epicTask);
        save();
    }

    @Override
    public void updateSubTask(SubTask subTask) {
        super.updateSubTask(subTask);
        save();
    }

    // Удаление задачи по ID
    @Override
    public Integer deleteTaskById(Integer id) {
        super.deleteTaskById(id);
        save();
        return id;
    }

    @Override
    public Integer deleteEpicById(Integer id) {
        super.deleteEpicById(id);
        save();
        return id;
    }

    @Override
    public Integer deleteSubById(Integer id) {
        super.deleteSubById(id);
        save();
        return id;
    }
}
