package Manager;

import Tasks.EpicTask;
import Tasks.StatusOfTask;
import Tasks.SubTask;
import Tasks.Task;
import com.google.gson.*;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.lang.reflect.Type;
import java.time.Duration;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

import static Manager.FileBackedTasksManager.loadFromFile;

public class HttpTaskServer {
    private static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;
    private static final Gson gson = new GsonBuilder()
            .setPrettyPrinting()
            .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeSerializer())
            .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeDeserializer())
            .registerTypeAdapter(Duration.class, new DurationSerializer())
            .registerTypeAdapter(Duration.class, new DurationDeserializer())
            .create();

    public static class TaskHandler implements HttpHandler {
        final String filePath = "resources/http_tasks.csv";
        FileBackedTasksManager manager = loadFromFile(new File(filePath));

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            Endpoint endpoint = getEndpoint(exchange.getRequestURI().getPath(), exchange.getRequestMethod());

            switch (endpoint) {
                case GET_TASKS: {
                    handleGetTasks(exchange);
                    break;
                }
                case GET_EPICS: {
                    handleGetEpics(exchange);
                    break;
                }
                case GET_SUBS: {
                    handleGetSubs(exchange);
                    break;
                }
                case GET_PRIORITY: {
                    handleGetPriority(exchange);
                    break;
                }
                case GET_HISTORY: {
                    handleGetHistory(exchange);
                    break;
                }
                case POST_TASKS: {
                    handlePostTask(exchange);
                    break;
                }
                case POST_EPICS: {
                    handlePostEpic(exchange);
                    break;
                }
                case POST_SUBS: {
                    handlePostSub(exchange);
                    break;
                }
                case DELETE_TASKS: {
                    handleDeleteTasks(exchange);
                    break;
                }
                case DELETE_EPICS: {
                    handleDeleteEpics(exchange);
                    break;
                }
                case DELETE_SUBS: {
                    handleDeleteSubs(exchange);
                    break;
                }
                case DELETE_HISTORY: {
                    handleDeleteHistory(exchange);
                    break;
                }
                default:
                    writeResponse(exchange, "Такого эндпоинта не существует", 404);
            }
        }

        private void handleGetTasks(HttpExchange exchange) throws IOException {
            String requestPath = exchange.getRequestURI().toString();
            if (requestPath.length() == 11) {
                writeResponse(exchange, gson.toJson(manager.getTasks()), 200);
            } else if (requestPath.length() >= 16 && requestPath.contains("?id=")) {
                Optional<Integer> id = getId(requestPath);
                if (id.isPresent() && manager.getTask(id.get()) != null) {
                    writeResponse(exchange, gson.toJson(manager.getTask(id.get())), 200);
                } else {
                    writeResponse(exchange, "Task с " +
                            exchange.getRequestURI().getQuery() + " не существует", 404);
                }
            } else {
                writeResponse(exchange, "Неверные параметры GET запроса для Task", 404);
            }
        }

        private void handleGetEpics(HttpExchange exchange) throws IOException {
            String requestPath = exchange.getRequestURI().toString();
            if (requestPath.length() == 11) {
                writeResponse(exchange, gson.toJson(manager.getEpicTasks()), 200);
            } else if (requestPath.length() >= 16 && requestPath.contains("?id=")) {
                Optional<Integer> id = getId(requestPath);
                if (id.isPresent() && manager.getEpic(id.get()) != null) {
                    writeResponse(exchange, gson.toJson(manager.getEpic(id.get())), 200);
                } else {
                    writeResponse(exchange, "Epic с " +
                            exchange.getRequestURI().getQuery() + " не существует", 404);
                }
            } else {
                writeResponse(exchange, "Неверные параметры GET запроса для Epic", 404);
            }
        }

        private void handleGetSubs(HttpExchange exchange) throws IOException {
            String requestPath = exchange.getRequestURI().toString();
            if (requestPath.length() == 10) {
                writeResponse(exchange, gson.toJson(manager.getSubTasks()), 200);
            } else if (requestPath.length() >= 15 && requestPath.contains("?id=")) {
                Optional<Integer> id = getId(requestPath);
                if (id.isPresent() && manager.getSub(id.get()) != null) {
                    writeResponse(exchange, gson.toJson(manager.getSub(id.get())), 200);
                } else {
                    writeResponse(exchange, "Sub с " +
                            exchange.getRequestURI().getQuery() + " не существует", 404);
                }
            } else {
                writeResponse(exchange, "Неверные параметры GET запроса для Sub", 404);
            }
        }

        private void handleGetPriority(HttpExchange exchange) throws IOException {
            String requestPath = exchange.getRequestURI().getPath();
            if (requestPath.length() == 15) {
                writeResponse(exchange, gson.toJson(manager.getPrioritizedTasks()), 200);
            } else {
                writeResponse(exchange,
                        "GET запрос для Priority должен быть без параметров", 404);
            }
        }

        private void handleGetHistory(HttpExchange exchange) throws IOException {
            String requestPath = exchange.getRequestURI().getPath();
            if (requestPath.length() == 14) {
                writeResponse(exchange, gson.toJson(manager.getHistory()), 200);
            } else {
                writeResponse(exchange,
                        "GET запрос для History должен быть без параметров", 404);
            }
        }

        private void handlePostTask(HttpExchange exchange) throws IOException {
            String requestPath = exchange.getRequestURI().getPath();
            if (requestPath.length() == 11) {
                InputStream inputStream = exchange.getRequestBody();
                String body = new String(inputStream.readAllBytes(), DEFAULT_CHARSET);

                try {
                    Task task = gson.fromJson(body, Task.class);
                    if (task.getTaskName().isEmpty() || task.getTaskDescription().isEmpty() ||
                            task.getTaskStatus().toString().isEmpty()) {
                        writeResponse(exchange, "Поля Task задачи не могут быть пустыми", 400);
                    } else {
                        // Обновляем или добавляем
                        // Для обновления в JSON должен быть указан ID
                        if (manager.getTask(task.getTaskId()) != null) {
                            manager.updateTask(task);
                            if (manager.getTasks().contains(task)) {
                                writeResponse(exchange, "Task задача обновлена", 201);
                            } else {
                                writeResponse(exchange, "Task задача не обновлена, " +
                                        "т.к. её время пересекается с другой задачей", 200);
                            }
                        } else {
                            manager.createTask(task);
                            if (manager.getTasks().contains(task)) {
                                writeResponse(exchange, "Task задача добавлена", 201);
                            } else {
                                writeResponse(exchange, "Task задача не добавлена, " +
                                        "т.к. её время пересекается с другой задачей", 200);
                            }
                        }
                    }
                } catch (JsonSyntaxException e) {
                    writeResponse(exchange,
                            "Получен некорректный JSON для создания Task задачи", 400);
                    e.printStackTrace();
                }
            } else {
                writeResponse(exchange,
                        "POST запрос для Task задачи должен быть без параметров", 404);
            }
        }

        private void handlePostEpic(HttpExchange exchange) throws IOException {
            String requestPath = exchange.getRequestURI().getPath();
            if (requestPath.length() == 11) {
                InputStream inputStream = exchange.getRequestBody();
                String body = new String(inputStream.readAllBytes(), DEFAULT_CHARSET);

                try {
                    EpicTask epic = gson.fromJson(body, EpicTask.class);

                    if (epic.getTaskName().isEmpty() || epic.getTaskDescription().isEmpty()) {
                        writeResponse(exchange, "Поля Epic задачи не могут быть пустыми", 400);
                    } else {
                        // Обновляем или добавляем
                        // Для обновления в JSON должен быть указан ID
                        if (manager.getEpic(epic.getTaskId()) != null) {
                            manager.updateEpicTask(epic);
                            writeResponse(exchange, "Epic задача обновлена", 201);
                        } else {
                            epic.setTaskStatus(StatusOfTask.NEW);
                            manager.createEpicTask(epic);
                            writeResponse(exchange, "Epic задача добавлена", 201);
                        }
                    }
                } catch (JsonSyntaxException e) {
                    writeResponse(exchange,
                            "Получен некорректный JSON для создания Epic задачи", 400);
                    e.printStackTrace();
                }
            } else {
                writeResponse(exchange,
                        "POST запрос для Epic задачи должен быть без параметров", 404);
            }
        }

        private void handlePostSub(HttpExchange exchange) throws IOException {
            String requestPath = exchange.getRequestURI().getPath();
            if (requestPath.length() == 10) {
                InputStream inputStream = exchange.getRequestBody();
                String body = new String(inputStream.readAllBytes(), DEFAULT_CHARSET);

                try {
                    SubTask sub = gson.fromJson(body, SubTask.class);
                    if (sub.getTaskName().isEmpty() || sub.getTaskDescription().isEmpty() ||
                            sub.getTaskStatus().toString().isEmpty() || manager.getEpic(sub.getEpicId()) == null) {
                        writeResponse(exchange,
                                "Поля Sub задачи пусты либо неверно задан epicId", 400);
                    } else {
                        // Обновляем или добавляем
                        // Для обновления в JSON должен быть указан ID
                        if (manager.getSub(sub.getTaskId()) != null) {
                            manager.updateSubTask(sub);
                            if (manager.getSubTasks().contains(sub)) {
                                writeResponse(exchange, "Sub задача обновлена", 201);
                            } else {
                                writeResponse(exchange, "Sub задача не обновлена, " +
                                        "т.к. её время пересекается с другой задачей", 200);
                            }
                        } else {
                            manager.createSubTask(sub);
                            if (manager.getSubTasks().contains(sub)) {
                                writeResponse(exchange, "Sub задача добавлена", 201);
                            } else {
                                writeResponse(exchange, "Sub задача не добавлена, " +
                                        "т.к. её время пересекается с другой задачей", 200);
                            }
                        }
                    }
                } catch (JsonSyntaxException e) {
                    writeResponse(exchange,
                            "Получен некорректный JSON для создания Sub задачи", 400);
                    e.printStackTrace();
                }
            } else {
                writeResponse(exchange,
                        "POST запрос для Sub задачи должен быть без параметров", 404);
            }
        }

        private void handleDeleteTasks(HttpExchange exchange) throws IOException {
            String requestPath = exchange.getRequestURI().toString();

            if (requestPath.length() == 11) {
                manager.deleteTasks();
                writeResponse(exchange, "Все Task задачи удалены", 200);
            } else if (requestPath.length() >= 16 && requestPath.contains("?id=")) {
                Optional<Integer> id = getId(requestPath);
                if (id.isPresent() && manager.getTask(id.get()) != null) {
                    manager.deleteTaskById(id.get());
                    writeResponse(exchange,
                            "Task задача с id = " + id.get() + " удалена", 200);
                } else {
                    writeResponse(exchange, "Task c " +
                            exchange.getRequestURI().getQuery() + " не существует", 404);
                }
            } else {
                writeResponse(exchange, "Неверные параметры DELETE запроса для Task", 404);
            }
        }

        private void handleDeleteEpics(HttpExchange exchange) throws IOException {
            String requestPath = exchange.getRequestURI().toString();

            if (requestPath.length() == 11) {
                manager.deleteEpicTasks();
                writeResponse(exchange, "Все Epic задачи удалены", 200);
            } else if (requestPath.length() >= 16 && requestPath.contains("?id=")) {
                Optional<Integer> id = getId(requestPath);
                if (id.isPresent() && manager.getEpic(id.get()) != null) {
                    manager.deleteEpicById(id.get());
                    writeResponse(exchange,
                            "Epic задача с id = " + id.get() + " удалена", 200);
                } else {
                    writeResponse(exchange, "Epic c " +
                            exchange.getRequestURI().getQuery() + " не существует", 404);
                }
            } else {
                writeResponse(exchange, "Неверные параметры DELETE запроса для Epic", 404);
            }
        }

        private void handleDeleteSubs(HttpExchange exchange) throws IOException {
            String requestPath = exchange.getRequestURI().toString();

            if (requestPath.length() == 10) {
                manager.deleteSubTasks();
                writeResponse(exchange, "Все Sub задачи удалены", 200);
            } else if (requestPath.length() >= 15 && requestPath.contains("?id=")) {
                Optional<Integer> id = getId(requestPath);
                if (id.isPresent() && manager.getSub(id.get()) != null) {
                    manager.deleteSubById(id.get());
                    writeResponse(exchange,
                            "Sub задача с id = " + id.get() + " удалена", 200);
                } else {
                    writeResponse(exchange, "Sub c " +
                            exchange.getRequestURI().getQuery() + " не существует", 404);
                }
            } else {
                writeResponse(exchange, "Неверные параметры DELETE запроса для Sub", 404);
            }
        }

        private void handleDeleteHistory(HttpExchange exchange) throws IOException {
            String requestPath = exchange.getRequestURI().getPath();

            if (requestPath.length() == 14) {
                manager.clearHistory();
                writeResponse(exchange, "Вся история просмотров удалена", 200);
            } else {
                writeResponse(exchange,
                        "DELETE запрос для History должен быть без параметров", 404);
            }
        }

        private Optional<Integer> getId(String path) {
            String[] pathParts = path.split("id=");
            try {
                return Optional.of(Integer.parseInt(pathParts[1]));
            } catch (NumberFormatException exception) {
                return Optional.empty();
            }
        }

        private void writeResponse(HttpExchange exchange,
                                   String responseString,
                                   int responseCode) throws IOException {
            if(responseString.isBlank()) {
                exchange.sendResponseHeaders(responseCode, 0);
            } else {
                byte[] bytes = responseString.getBytes(DEFAULT_CHARSET);
                exchange.sendResponseHeaders(responseCode, bytes.length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(bytes);
                }
            }
            exchange.close();
        }

        private Endpoint getEndpoint(String requestPath, String requestMethod) {
            String secondWord = requestPath.split("/")[2];

            // GET-запросы
            if (requestMethod.equals("GET")) {
                switch (secondWord) {
                    case "task":
                        return Endpoint.GET_TASKS;
                    case "epic":
                        return Endpoint.GET_EPICS;
                    case "sub":
                        return Endpoint.GET_SUBS;
                    case "priority":
                        return Endpoint.GET_PRIORITY;
                    case "history":
                        return Endpoint.GET_HISTORY;
                }
            }

            // POST-запросы
            if (requestMethod.equals("POST")) {
                switch (secondWord) {
                    case "task":
                        return Endpoint.POST_TASKS;
                    case "epic":
                        return Endpoint.POST_EPICS;
                    case "sub":
                        return Endpoint.POST_SUBS;
                }
            }

            // DELETE-запросы
            if (requestMethod.equals("DELETE")) {
                switch (secondWord) {
                    case "task":
                        return Endpoint.DELETE_TASKS;
                    case "epic":
                        return Endpoint.DELETE_EPICS;
                    case "sub":
                        return Endpoint.DELETE_SUBS;
                    case "history":
                        return Endpoint.DELETE_HISTORY;
                }
            }

            return Endpoint.UNKNOWN;
        }
    }

    enum Endpoint {
        GET_TASKS,
        GET_EPICS,
        GET_SUBS,
        GET_PRIORITY,
        GET_HISTORY,
        POST_TASKS,
        POST_EPICS,
        POST_SUBS,
        DELETE_TASKS,
        DELETE_EPICS,
        DELETE_SUBS,
        DELETE_HISTORY,
        UNKNOWN
    }

    public static class LocalDateTimeSerializer implements JsonSerializer<LocalDateTime> {
        private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
        @Override
        public JsonElement serialize(LocalDateTime localDateTime, Type type, JsonSerializationContext jsonSerializationContext) {
            return new JsonPrimitive(formatter.format(localDateTime));
        }
    }

    public static class LocalDateTimeDeserializer implements JsonDeserializer<LocalDateTime> {
        private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
        @Override
        public LocalDateTime deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
            return LocalDateTime.parse(jsonElement.getAsString(), formatter);
        }
    }

    public static class DurationSerializer implements JsonSerializer<Duration> {
        @Override
        public JsonElement serialize(Duration duration, Type type, JsonSerializationContext jsonSerializationContext) {
            return new JsonPrimitive(duration.toMinutes());
        }
    }

    public static class DurationDeserializer implements JsonDeserializer<Duration> {
        @Override
        public Duration deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
            return Duration.ofMinutes(jsonElement.getAsLong());
        }
    }
}
