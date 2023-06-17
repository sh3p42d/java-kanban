import Manager.*;
import Tasks.EpicTask;
import static Manager.HttpTaskManager.loadFromClient;
import static Manager.HttpTaskServer.*;

import Tasks.StatusOfTask;
import Tasks.SubTask;
import Tasks.Task;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpClient;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class HttpTaskManagerTest extends TasksManagerTest<HttpTaskManager> {
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();


    private static final Gson gsonTaskServer = new GsonBuilder()
            .setPrettyPrinting()
            .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeSerializer())
            .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeDeserializer())
            .registerTypeAdapter(Duration.class, new DurationSerializer())
            .registerTypeAdapter(Duration.class, new DurationDeserializer())
            .create();

    private final String kvServerUrl = "http://localhost:8078";
    private KVTaskClient client;
    private final String httpServerUrl = "http://localhost:8080";
    private static KVServer kvServer;
    private static final int PORT = 8080;
    private HttpServer httpServer;

    @BeforeEach
    public void beforeEachInit() {
        try {
            kvServer = new KVServer();
            kvServer.start();
        } catch (IOException e) {
            System.out.println("Сервер ключ-значение не удалось запустить. \n" +
                    "Проверьте доступность " + kvServerUrl);
            e.printStackTrace();
        }

        client = new KVTaskClient(kvServerUrl);
        manager = (HttpTaskManager) Managers.getDefault(kvServerUrl);

        createAllTestTask(manager);
        getHistoryAllTestTask(manager);

        try {
            httpServer = HttpServer.create();
            httpServer.bind(new InetSocketAddress(PORT), 0);
            httpServer.createContext("/tasks", new TaskHandler());
            httpServer.start();
        } catch (IOException e) {
            System.out.println("Сервер маппинга не удалось запустить");
            e.printStackTrace();
        }

    }

    @AfterEach
    public void afterEachInit() {
        try {
            kvServer.stop();
        } catch (NullPointerException e) {
            System.out.println("Сервер ключ-значение по адресу " + kvServerUrl + " не был запущен " +
                    "и поэтому не может быть остановлен");
            e.printStackTrace();
        }

        try {
            httpServer.stop(1);
        } catch (NullPointerException e) {
            System.out.println("Сервер маппинга по адресу " + httpServerUrl + " не был запущен " +
                    "и поэтому не может быть остановлен");
            e.printStackTrace();
        }
    }

    // Работа с файлом
    @Test
    public void shouldNotGetAnything() {
        manager = (HttpTaskManager) Managers.getDefault(kvServerUrl);
        manager.deleteTasks(); // вызываем метод save()

        assertTrue(manager.getTasks().isEmpty());
        assertTrue(manager.getEpicTasks().isEmpty());
        assertTrue(manager.getSubTasks().isEmpty());
        assertTrue(manager.getHistory().isEmpty());

        assertEquals("{}", client.load("tasks"));
        assertEquals("{}", client.load("epics"));
        assertEquals("{}", client.load("subs"));
        assertEquals("\"\"", client.load("history"));
    }

    @Test
    public void shouldGetEpicWithoutSubsWithHistory() {
        manager = (HttpTaskManager) Managers.getDefault(kvServerUrl);
        EpicTask testEpic = createTestEpic(manager);
        manager.getEpic(testEpic.getTaskId());
        manager.deleteSubTasks();

        assertTrue(manager.getTasks().isEmpty());
        assertEquals(1, manager.getEpicTasks().size());
        assertTrue(manager.getSubTasks().isEmpty());
        assertEquals(1, manager.getHistory().size());

        // Без toString считает что это разные массивы
        assertEquals(manager.getEpicTasks().toString(),
                formatEpicListFromJson(client.load("epics")).toString());
        assertEquals("\"" + testEpic.getTaskId() + "\"" , client.load("history"));
    }

    @Test
    public void shouldGetEpicWithoutSubsWithoutHistory() {
        manager = (HttpTaskManager) Managers.getDefault(kvServerUrl);
        createTestEpic(manager);
        manager.deleteSubTasks();

        assertTrue(manager.getTasks().isEmpty());
        assertEquals(1, manager.getEpicTasks().size());
        assertTrue(manager.getSubTasks().isEmpty());
        assertTrue(manager.getHistory().isEmpty());

        assertEquals(manager.getEpicTasks().toString(),
                formatEpicListFromJson(client.load("epics")).toString());
        assertEquals("\"\"" , client.load("history"));
    }

    @Test
    public void shouldLoadFromServer() {
        manager = (HttpTaskManager) Managers.getDefault(kvServerUrl);
        manager = loadFromClient(kvServerUrl);

        assertEquals(3, manager.getTasks().size());
        assertEquals(3, manager.getEpicTasks().size());
        assertEquals(4, manager.getSubTasks().size());
        assertEquals(10, manager.getHistory().size());
    }

    @Test
    public void shouldLoadFromEmptyServer() {
        manager.deleteTasks();
        manager.deleteEpicTasks();
        manager.deleteSubTasks();

        manager = (HttpTaskManager) Managers.getDefault(kvServerUrl);
        manager = loadFromClient(kvServerUrl);

        assertTrue(manager.getTasks().isEmpty());
        assertTrue(manager.getEpicTasks().isEmpty());
        assertTrue(manager.getSubTasks().isEmpty());
        assertTrue(manager.getHistory().isEmpty());
    }

    @Test
    public void shouldLoadFromServerWithoutSubs() {
        manager.deleteTasks();
        manager.deleteEpicTasks();
        manager.deleteSubTasks();

        manager = (HttpTaskManager) Managers.getDefault(kvServerUrl);
        EpicTask testEpic = createTestEpic(manager);
        manager.getEpic(testEpic.getTaskId());
        manager.deleteSubTasks();
        manager = loadFromClient(kvServerUrl);

        assertTrue(manager.getTasks().isEmpty());
        assertEquals(1, manager.getEpicTasks().size());
        assertTrue(manager.getSubTasks().isEmpty());
        assertEquals(1, manager.getHistory().size());

        assertEquals(manager.getEpicTasks().toString(),
                formatEpicListFromJson(client.load("epics")).toString());
        assertEquals("\"" + testEpic.getTaskId() + "\"" , client.load("history"));
    }

    @Test
    public void shouldLoadFromServerWithoutHistory() {
        manager.clearHistory();
        manager = (HttpTaskManager) Managers.getDefault(kvServerUrl);
        manager = loadFromClient(kvServerUrl);

        assertEquals(3, manager.getTasks().size());
        assertEquals(3, manager.getEpicTasks().size());
        assertEquals(4, manager.getSubTasks().size());
        assertTrue(manager.getHistory().isEmpty());
    }

    @Test
    public void shouldGetTasks() {
        String serverAllTasks = sendGetToServer("/tasks/task");
        assertEquals(gsonTaskServer.toJson(manager.getTasks()), serverAllTasks);

        int taskId = 1;
        String serverOneTask = sendGetToServer("/tasks/task/?id=" + taskId);
        assertEquals(gsonTaskServer.toJson(manager.getTask(taskId)), serverOneTask);
    }

    @Test
    public void shouldNotGetTasks() {
        int taskId = 1000;
        String serverOneTask = sendGetToServer("/tasks/task/?id=" + taskId);
        assertEquals("Task с id=" + taskId + " не существует", serverOneTask);

        serverOneTask = sendGetToServer("/tasks/task/?qwe=" + taskId);
        assertEquals("Неверные параметры GET запроса для Task", serverOneTask);
    }

    @Test
    public void shouldGetEpics() {
        String serverAllEpics = sendGetToServer("/tasks/epic");
        assertEquals(gsonTaskServer.toJson(manager.getEpicTasks()), serverAllEpics);

        int epicId = 4;
        String serverOneEpic = sendGetToServer("/tasks/epic/?id=" + epicId);
        assertEquals(gsonTaskServer.toJson(manager.getEpic(epicId)), serverOneEpic);
    }

    @Test
    public void shouldNotGetEpics() {
        int epicId = 1000;
        String serverOneEpic = sendGetToServer("/tasks/epic/?id=" + epicId);
        assertEquals("Epic с id=" + epicId + " не существует", serverOneEpic);

        serverOneEpic = sendGetToServer("/tasks/epic/?qwe=" + epicId);
        assertEquals("Неверные параметры GET запроса для Epic", serverOneEpic);
    }

    @Test
    public void shouldGetSubs() {
        String serverAllSubs = sendGetToServer("/tasks/sub");
        assertEquals(gsonTaskServer.toJson(manager.getSubTasks()), serverAllSubs);

        int subId = 7;
        String serverOneSub = sendGetToServer("/tasks/sub/?id=" + subId);
        assertEquals(gsonTaskServer.toJson(manager.getSub(subId)), serverOneSub);
    }

    @Test
    public void shouldNotGetSubs() {
        int subId = 1000;
        String serverOneSub = sendGetToServer("/tasks/sub/?id=" + subId);
        assertEquals("Sub с id=" + subId + " не существует", serverOneSub);

        serverOneSub = sendGetToServer("/tasks/sub/?qwe=" + subId);
        assertEquals("Неверные параметры GET запроса для Sub", serverOneSub);
    }

    @Test
    public void shouldGetPriority() {
        String serverAllSubs = sendGetToServer("/tasks/priority");
        assertEquals(gsonTaskServer.toJson(manager.getPrioritizedTasks()), serverAllSubs);

        String serverOneSub = sendGetToServer("/tasks/priority/?id=123");
        assertEquals("GET запрос для Priority должен быть без параметров", serverOneSub);
    }

    @Test
    public void shouldGetHistory() {
        String serverAllSubs = sendGetToServer("/tasks/history");
        assertEquals(gsonTaskServer.toJson(manager.getHistory()), serverAllSubs);

        String serverOneSub = sendGetToServer("/tasks/history/?id=123");
        assertEquals("GET запрос для History должен быть без параметров", serverOneSub);
    }

    @Test
    public void shouldPostTask() {
        Task task = manager.getTask(1);
        task.setTaskName("qwe123");
        String serverOneTask = sendPostToServer("/tasks/task", gsonTaskServer.toJson(task));
        assertEquals("Task задача обновлена", serverOneTask);

        Task newTask =  new Task("New Test task",
                "Фывапролдж Ячсмить", StatusOfTask.DONE, "12.02.2024 12:00", 150);
        newTask.setStartTime(task.getStartTime().plusDays(5));
        serverOneTask = sendPostToServer("/tasks/task", gsonTaskServer.toJson(newTask));
        assertEquals("Task задача добавлена", serverOneTask);
    }

    @Test
    public void shouldNotPostTask() {
        Task task = manager.getTask(1);
        task.setTaskName("");
        String serverOneTask = sendPostToServer("/tasks/task", gsonTaskServer.toJson(task));
        assertEquals("Поля Task задачи не могут быть пустыми", serverOneTask);

        serverOneTask = sendPostToServer("/tasks/task", gsonTaskServer.toJson("шляпа"));
        assertEquals("Получен некорректный JSON для создания Task задачи", serverOneTask);

        task.setTaskName("qwe123");
        task.setStartTime(manager.getTask(2).getStartTime());
        serverOneTask = sendPostToServer("/tasks/task", gsonTaskServer.toJson(task));
        assertEquals("Task задача не обновлена, т.к. её время пересекается с другой задачей", serverOneTask);

        Task newTask =  new Task("New Test task",
                "Фывапролдж Ячсмить", StatusOfTask.DONE, "12.02.2024 12:00", 150);
        serverOneTask = sendPostToServer("/tasks/task", gsonTaskServer.toJson(newTask));
        assertEquals("Task задача не добавлена, т.к. её время пересекается с другой задачей", serverOneTask);

        serverOneTask = sendPostToServer("/tasks/task/?id=123", gsonTaskServer.toJson(newTask));
        assertEquals("POST запрос для Task задачи должен быть без параметров", serverOneTask);
    }

    @Test
    public void shouldPostEpic() {
        EpicTask epic = manager.getEpic(6);
        epic.setTaskName("qwe123");
        String serverOneTask = sendPostToServer("/tasks/epic", gsonTaskServer.toJson(epic));
        assertEquals("Epic задача обновлена", serverOneTask);

        EpicTask newEpic = new EpicTask("hjkl", "asdfg");
        serverOneTask = sendPostToServer("/tasks/epic", gsonTaskServer.toJson(newEpic));
        assertEquals("Epic задача добавлена", serverOneTask);
    }

    @Test
    public void shouldNotPostEpic() {
        EpicTask epic = manager.getEpic(6);
        epic.setTaskName("");
        String serverOneTask = sendPostToServer("/tasks/epic", gsonTaskServer.toJson(epic));
        assertEquals("Поля Epic задачи не могут быть пустыми", serverOneTask);

        serverOneTask = sendPostToServer("/tasks/epic", gsonTaskServer.toJson("шляпа"));
        assertEquals("Получен некорректный JSON для создания Epic задачи", serverOneTask);

        serverOneTask = sendPostToServer("/tasks/epic/?id=123", gsonTaskServer.toJson(epic));
        assertEquals("POST запрос для Epic задачи должен быть без параметров", serverOneTask);
    }

    @Test
    public void shouldPostSub() {
        SubTask sub = manager.getSub(7);
        sub.setTaskName("qwe123");
        String serverOneTask = sendPostToServer("/tasks/sub", gsonTaskServer.toJson(sub));
        assertEquals("Sub задача обновлена", serverOneTask);

        SubTask newTask =  new SubTask("кекич",
                "olololo", StatusOfTask.NEW, 4, "10.01.2027 12:00", 150);
        serverOneTask = sendPostToServer("/tasks/sub", gsonTaskServer.toJson(newTask));
        assertEquals("Sub задача добавлена", serverOneTask);
    }

    @Test
    public void shouldNotPostSub() {
        SubTask sub = manager.getSub(7);
        sub.setTaskName("");
        String serverOneTask = sendPostToServer("/tasks/sub", gsonTaskServer.toJson(sub));
        assertEquals("Поля Sub задачи пусты либо неверно задан epicId", serverOneTask);

        serverOneTask = sendPostToServer("/tasks/sub", gsonTaskServer.toJson("шляпа"));
        assertEquals("Получен некорректный JSON для создания Sub задачи", serverOneTask);

        sub.setTaskName("qwe123");
        sub.setStartTime(manager.getTask(2).getStartTime());
        serverOneTask = sendPostToServer("/tasks/sub", gsonTaskServer.toJson(sub));
        assertEquals("Sub задача не обновлена, т.к. её время пересекается с другой задачей", serverOneTask);

        SubTask newTask =  new SubTask("кекич",
                "olololo", StatusOfTask.NEW, 4, "10.01.2024 12:00", 150);
        serverOneTask = sendPostToServer("/tasks/sub", gsonTaskServer.toJson(newTask));
        assertEquals("Sub задача не добавлена, т.к. её время пересекается с другой задачей", serverOneTask);

        serverOneTask = sendPostToServer("/tasks/sub/?id=123", gsonTaskServer.toJson(newTask));
        assertEquals("POST запрос для Sub задачи должен быть без параметров", serverOneTask);
    }

    @Test
    public void shouldDeleteTasks() {
        int taskId = 1;
        String serverOneTask = sendDeleteToServer("/tasks/task/?id=" + taskId);
        assertEquals("Task задача с id = " + taskId + " удалена", serverOneTask);

        String serverAllTasks = sendDeleteToServer("/tasks/task");
        assertEquals("Все Task задачи удалены", serverAllTasks);
    }

    @Test
    public void shouldNotDeleteTasks() {
        int taskId = 1000;
        String serverOneTask = sendDeleteToServer("/tasks/task/?id=" + taskId);
        assertEquals("Task c id=" + taskId + " не существует", serverOneTask);

        serverOneTask = sendDeleteToServer("/tasks/task/?qwe=" + taskId);
        assertEquals("Неверные параметры DELETE запроса для Task", serverOneTask);
    }

    @Test
    public void shouldDeleteEpics() {
        int epicId = 4;
        String serverOneEpic = sendDeleteToServer("/tasks/epic/?id=" + epicId);
        assertEquals("Epic задача с id = " + epicId + " удалена", serverOneEpic);

        String serverAllEpics = sendDeleteToServer("/tasks/epic");
        assertEquals("Все Epic задачи удалены", serverAllEpics);
    }

    @Test
    public void shouldNotDeleteEpics() {
        int epicId = 1000;
        String serverOneEpic = sendDeleteToServer("/tasks/epic/?id=" + epicId);
        assertEquals("Epic c id=" + epicId + " не существует", serverOneEpic);

        serverOneEpic = sendDeleteToServer("/tasks/epic/?qwe=" + epicId);
        assertEquals("Неверные параметры DELETE запроса для Epic", serverOneEpic);
    }

    @Test
    public void shouldDeleteSubs() {
        int subId = 7;
        String serverOneSub = sendDeleteToServer("/tasks/sub/?id=" + subId);
        assertEquals("Sub задача с id = " + subId + " удалена", serverOneSub);

        String serverAllSubs = sendDeleteToServer("/tasks/sub");
        assertEquals("Все Sub задачи удалены", serverAllSubs);
    }

    @Test
    public void shouldNotDeleteSubs() {
        int subId = 1000;
        String serverOneSub = sendDeleteToServer("/tasks/sub/?id=" + subId);
        assertEquals("Sub c id=" + subId + " не существует", serverOneSub);

        serverOneSub = sendDeleteToServer("/tasks/sub/?qwe=" + subId);
        assertEquals("Неверные параметры DELETE запроса для Sub", serverOneSub);
    }

    @Test
    public void shouldDeleteHistory() {
        String serverAllSubs = sendDeleteToServer("/tasks/history");
        assertEquals("Вся история просмотров удалена", serverAllSubs);

        String serverOneSub = sendDeleteToServer("/tasks/history/?id=123");
        assertEquals("DELETE запрос для History должен быть без параметров", serverOneSub);
    }

    @Test
    public void shouldReturnUnknown() {
        String serverAllSubs = sendUnknownToServer();
        assertEquals("Такого эндпоинта не существует", serverAllSubs);
    }

    private List<EpicTask> formatEpicListFromJson(String json) {
        Map<Integer, EpicTask> epicJson = gson.fromJson(json, new TypeToken<HashMap<Integer, EpicTask>> () {
        }.getType());

        return new ArrayList<>(epicJson.values());
    }

    private String sendGetToServer (String url) {
        String body = "";
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(httpServerUrl + url))
                    .GET()
                    .build();

            HttpClient serverClient = HttpClient.newHttpClient();
            HttpResponse.BodyHandler<String> handler = HttpResponse.BodyHandlers.ofString();
            body = serverClient.send(request, handler).body();
        } catch (IOException | InterruptedException e) {
            System.out.println("Ошибка GET запроса");
        }
        return body;
    }

    private String sendPostToServer (String url, String task) {
        String body = "";
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(httpServerUrl + url))
                    .POST(HttpRequest.BodyPublishers.ofString(task))
                    .build();

            HttpClient serverClient = HttpClient.newHttpClient();
            HttpResponse.BodyHandler<String> handler = HttpResponse.BodyHandlers.ofString();
            body = serverClient.send(request, handler).body();
        } catch (IOException | InterruptedException e) {
            System.out.println("Ошибка POST запроса");
        }
        return body;
    }

    private String sendDeleteToServer (String url) {
        String body = "";
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(httpServerUrl + url))
                    .DELETE()
                    .build();

            HttpClient serverClient = HttpClient.newHttpClient();
            HttpResponse.BodyHandler<String> handler = HttpResponse.BodyHandlers.ofString();
            body = serverClient.send(request, handler).body();
        } catch (IOException | InterruptedException e) {
            System.out.println("Ошибка DELETE запроса");
        }
        return body;
    }

    private String sendUnknownToServer() {
        String body = "";
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(httpServerUrl + "/tasks/task"))
                    .PUT(HttpRequest.BodyPublishers.ofString("unknown"))
                    .build();

            HttpClient serverClient = HttpClient.newHttpClient();
            HttpResponse.BodyHandler<String> handler = HttpResponse.BodyHandlers.ofString();
            body = serverClient.send(request, handler).body();
        } catch (IOException | InterruptedException e) {
            System.out.println("Ошибка отправки запроса");
        }
        return body;
    }
}
