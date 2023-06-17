package Manager;

import Tasks.EpicTask;
import Tasks.SubTask;
import Tasks.Task;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import java.util.*;

public class HttpTaskManager extends FileBackedTasksManager {
    private final KVTaskClient client;
    private static final GsonBuilder gsonBuilder = new GsonBuilder();
    private static final Gson gson = gsonBuilder.setPrettyPrinting().create();

    public HttpTaskManager(String serverUrl) {
        super("resources/http_tasks.csv");
        this.client = new KVTaskClient(serverUrl);
    }

    @Override
    protected void save() {
        super.save();
        String jsonStr = "";
        try {
             jsonStr = gson.toJson(getTaskMap());
        } catch (JsonSyntaxException e) {
            System.out.println("Получен некорректный JSON TaskMap: " + e.getMessage());
        }
        client.put("tasks", jsonStr);

        try {
            jsonStr = gson.toJson(getEpicMap());
        } catch (JsonSyntaxException e) {
            System.out.println("Получен некорректный JSON EpicMap: " + e.getMessage());
        }
        client.put("epics", jsonStr);

        try {
            jsonStr = gson.toJson(getSubMap());
        } catch (JsonSyntaxException e) {
            System.out.println("Получен некорректный JSON SubMap: " + e.getMessage());
        }
        client.put("subs", jsonStr);

        try {
            jsonStr = gson.toJson(historyToString(getHistory()));
        } catch (JsonSyntaxException e) {
            System.out.println("Получен некорректный JSON History: " + e.getMessage());
        }
        client.put("history", jsonStr);
    }

    public static HttpTaskManager loadFromClient(String serverUrl) {
        HttpTaskManager manager = null;
        try {
            manager = (HttpTaskManager) Managers.getDefault(serverUrl);
            HistoryManager newManagerHistory = manager.getManagerHistory();
            KVTaskClient client = new KVTaskClient(serverUrl);
            int maxId = 1;

            String jsonAnswer = client.load("tasks");
            Map<Integer, Task> taskJson = gson.fromJson(jsonAnswer, new TypeToken<HashMap<Integer, Task>>() {
            }.getType());

            if (!taskJson.isEmpty()) {
                manager.taskMap.putAll(taskJson);
                if (maxId < Collections.max(taskJson.keySet())) {
                    maxId = Collections.max(taskJson.keySet());
                }
            }


            jsonAnswer = client.load("epics");
            Map<Integer, EpicTask> epicJson = gson.fromJson(jsonAnswer, new TypeToken<HashMap<Integer, EpicTask>>() {
            }.getType());

            if (!epicJson.isEmpty()) {
                manager.epicMap.putAll(epicJson);
                if (maxId < Collections.max(epicJson.keySet())) {
                    maxId = Collections.max(epicJson.keySet());
                }
            }


            jsonAnswer = client.load("subs");
            Map<Integer, SubTask> subJson = gson.fromJson(jsonAnswer, new TypeToken<HashMap<Integer, SubTask>>() {
            }.getType());
            if (!subJson.isEmpty()) {
                manager.subMap.putAll(subJson);
                if (maxId < Collections.max(subJson.keySet())) {
                    maxId = Collections.max(subJson.keySet());
                }
            }

            manager.setNextId(maxId);

            jsonAnswer = client.load("history");
            if (!jsonAnswer.equals("\"\"")) {
                List<Integer> historyJson = historyFromString(jsonAnswer.substring(1, jsonAnswer.length() - 1));

                for (Integer taskIdHistory : historyJson) {
                    Task historyTask;
                    if (manager.taskMap.containsKey(taskIdHistory)) {
                        historyTask = manager.taskMap.get(taskIdHistory);
                    } else if (manager.epicMap.containsKey(taskIdHistory)) {
                        historyTask = manager.epicMap.get(taskIdHistory);
                    } else {
                        historyTask = manager.subMap.get(taskIdHistory);
                    }
                    newManagerHistory.add(historyTask);
                }
            }
        } catch (JsonSyntaxException e) {
            System.out.println("Произошла ошибка при обработке JSON: " + e.getMessage());
        }
        return manager;
    }
}
