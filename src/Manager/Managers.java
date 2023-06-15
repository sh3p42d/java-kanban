package Manager;

public final class Managers {
    private Managers() {}

    public static TaskManager getDefault() {
        return new InMemoryTaskManager();
    }

    public static TaskManager getDefaultFileManager(String path) {
        return new FileBackedTasksManager(path);
    }

    public static TaskManager getDefaultHttpManager(String serverUrl) {
        return new HttpTaskManager(serverUrl);
    }

    public static HistoryManager getDefaultHistory() {
        return new InMemoryHistoryManager();
    }
}
