package Manager;

import Tasks.*;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FileBackedTasksManager extends InMemoryTaskManager {
    private final String path;
    public FileBackedTasksManager(String path) {
        this.path = path;
    }

    // Сохранение задач и истории в файл
    protected void save() {
        Path filePath = Path.of(path);
        Map<Integer, Task> allMap = allMapMerge();
        String history = historyToString(getHistory());
        String fileHead = "id,type,name,status,description,start,duration,epic\n";

        try (BufferedWriter bw = Files.newBufferedWriter(filePath)) {
            bw.flush();
            bw.append(fileHead);

            if (allMap.isEmpty() && history.isEmpty()) {
                throw new ManagerSaveException("Нет задач и истории для записи");
            }

            for (Map.Entry<Integer, Task> entry : allMap.entrySet()) {
                bw.append(taskToString(entry.getValue())).append("\n");
            }

            bw.append("\n").append(history);
        } catch (ManagerSaveException e) {
            System.out.println(e.getMessage());
        } catch (IOException e) {
            throw new ManagerSaveException("Ошибка при записи в файл");
        }
    }

    protected Map<Integer, Task> allMapMerge() {
        // Собираем Map'ы из родителя
        Map<Integer, Task> allMap = new HashMap<>(super.getTaskMap());
        allMap.putAll(super.getEpicMap());
        allMap.putAll(super.getSubMap());
        return allMap;
    }

    protected static String historyToString(List<Task> taskList) {
        StringBuilder history = new StringBuilder();

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

        if (task.getStartTime() != null) {
            strTask = strTask  + "," +
                    task.getStartTime().format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")) + "," +
                    task.getDuration().toMinutes();
        }

        if (getType(task).equals(TasksType.SUBTASK)) {
            strTask = strTask + "," + super.getSubMap().get(task.getTaskId()).getEpicId();
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
    public static FileBackedTasksManager loadFromFile(File file) {
        FileBackedTasksManager manager = (FileBackedTasksManager) Managers.getDefaultFileManager(file.getPath());

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            HistoryManager newManagerHistory = manager.getManagerHistory();

            String fileString;
            List<String> taskList = new ArrayList<>();

            while ((fileString = br.readLine()) != null ) {
                taskList.add(fileString);
            }

            // Проверяем есть ли история
            int historyLine = 2;
            boolean emptyHistory = taskList.get(taskList.size() - 1)
                    .equals("id,type,name,status,description,start,duration,epic")
                    || taskList.get(taskList.size() - 1).isEmpty();
            if (!emptyHistory) {
                historyLine += 1;
            }

            // Записываем задачи в нужные Map'ы
            for (int i = 1; i <= (taskList.size() - historyLine); i++) {
                String taskType = taskList.get(i).split(",")[1];
                int taskId = Integer.parseInt(taskList.get(i).split(",")[0]);

                if (taskType.equals(TasksType.TASK.toString())) {
                    manager.setNextId(taskId);
                    manager.createTask(fromStringTask(taskList.get(i)));
                } else if (taskType.equals(TasksType.EPIC.toString())) {
                    manager.setNextId(taskId);
                    manager.createEpicTask(fromStringEpic(taskList.get(i)));
                } else {
                    manager.setNextId(taskId);
                    manager.createSubTask(fromStringSub(taskList.get(i)));
                }
            }

            // Считываем историю из файла в менеджера истории
            if (!emptyHistory) {
                List<Integer> newHistory = historyFromString(taskList.get(taskList.size() - 1));

                for (Integer taskIdHistory : newHistory) {
                    Task historyTask;
                    if (manager.getTaskMap().containsKey(taskIdHistory)) {
                        historyTask = manager.getTask(taskIdHistory);
                    } else if (manager.getEpicMap().containsKey(taskIdHistory)) {
                        historyTask = manager.getEpic(taskIdHistory);
                    } else {
                        historyTask = manager.getSub(taskIdHistory);
                    }
                    newManagerHistory.add(historyTask);
                }
            }

            manager.setManagerHistory(newManagerHistory);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return manager;
    }

    private static Task fromStringTask(String value) {
        String[] taskString = value.split(",");

        Task task = new Task(taskString[2], taskString[4], statusFromFile(taskString[3]));

        if (taskString.length == 7) {
            task.setStartTime(LocalDateTime.parse(taskString[5], DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")));
            task.setDuration(Duration.ofMinutes(Long.parseLong(taskString[6])));
        }

        task.setTaskId(Integer.parseInt(taskString[0]));
        return task;
    }

    private static EpicTask fromStringEpic(String value) {
        String[] taskString = value.split(",");

        EpicTask task = new EpicTask(taskString[2], taskString[4]);

        if (taskString.length == 7) {
            task.setStartTime(LocalDateTime.parse(taskString[5], DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")));
            task.setDuration(Duration.ofMinutes(Long.parseLong(taskString[6])));
        }

        task.setTaskStatus(statusFromFile(taskString[3]));
        task.setTaskId(Integer.parseInt(taskString[0]));
        return task;
    }

    private static SubTask fromStringSub(String value) {
        String[] taskString = value.split(",");

        SubTask task = new SubTask(taskString[2], taskString[4], statusFromFile(taskString[3]), -1);

        if (taskString.length == 8) {
            task.setStartTime(LocalDateTime.parse(taskString[5], DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")));
            task.setDuration(Duration.ofMinutes(Long.parseLong(taskString[6])));
            task.setEpicId(Integer.parseInt(taskString[7]));
        } else {
            task.setEpicId(Integer.parseInt(taskString[5]));
        }

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

    protected static List<Integer> historyFromString(String value) {
        List<Integer> historyList = new ArrayList<>();
        String[] historyString = value.split(",");
        for (String s : historyString) {
            historyList.add(Integer.valueOf(s));
        }
        return historyList;
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

    // История просмотров
    @Override
    public void clearHistory() {
        super.clearHistory();
        save();
    }
}
