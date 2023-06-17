package Manager;

public final class Managers {
    private Managers() {}

    public static TaskManager getDefault(String serverUrl) {
        return new HttpTaskManager(serverUrl);
    }

    public static TaskManager getDefaultFileManager(String path) {
        return new FileBackedTasksManager(path);
    }

    // нужен для запуска тестов
    public static TaskManager getDefaultMemoryManager() {
        return new InMemoryTaskManager();
    }

    public static HistoryManager getDefaultHistory() {
        return new InMemoryHistoryManager();
    }
}
