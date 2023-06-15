package Manager;

import Tasks.EpicTask;
import Tasks.SubTask;
import Tasks.Task;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.util.*;

public class HttpTaskManager extends FileBackedTasksManager {
    private final KVTaskClient client;
    GsonBuilder gsonBuilder = new GsonBuilder();
    private final Gson gson = gsonBuilder.setPrettyPrinting().create();

    public HttpTaskManager(String serverUrl) {
        super("resources/http_tasks.csv");
        this.client = new KVTaskClient(serverUrl);
    }

    @Override
    protected void save() {
        super.save();
        String jsonStr = gson.toJson(getTaskMap());
        client.put("tasks", jsonStr);

        jsonStr = gson.toJson(getEpicMap());
        client.put("epics", jsonStr);

        jsonStr = gson.toJson(getSubMap());
        client.put("subs", jsonStr);

        jsonStr = gson.toJson(historyToString(getHistory()));
        client.put("history", jsonStr);
    }

    public static HttpTaskManager loadFromClient(String serverUrl) {
        HttpTaskManager manager = (HttpTaskManager) Managers.getDefaultHttpManager(serverUrl);
        HistoryManager newManagerHistory = manager.getManagerHistory();
        GsonBuilder gsonBuilder = new GsonBuilder();
        Gson gson = gsonBuilder.setPrettyPrinting().create();
        KVTaskClient client = new KVTaskClient(serverUrl);
        int maxId = 1;

        String jsonAnswer = client.load("tasks");
        Map<Integer, Task> taskJson = gson.fromJson(jsonAnswer, new TypeToken<HashMap<Integer, Task>> () {
            }.getType());

        if (!taskJson.isEmpty()) {
            manager.taskMap.putAll(taskJson);
            if (maxId < Collections.max(taskJson.keySet())) {
                maxId = Collections.max(taskJson.keySet());
            }
        }


        jsonAnswer = client.load("epics");
        Map<Integer, EpicTask> epicJson = gson.fromJson(jsonAnswer, new TypeToken<HashMap<Integer, EpicTask>> () {
            }.getType());

        if (!epicJson.isEmpty()) {
            manager.epicMap.putAll(epicJson);
            if (maxId < Collections.max(epicJson.keySet())) {
                maxId = Collections.max(epicJson.keySet());
            }
        }


        jsonAnswer = client.load("subs");
        Map<Integer, SubTask> subJson = gson.fromJson(jsonAnswer, new TypeToken<HashMap<Integer, SubTask>> () {
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

        return manager;
    }
}
