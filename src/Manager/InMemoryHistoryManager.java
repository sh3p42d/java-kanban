package Manager;

import Tasks.Task;
import java.util.LinkedList;
import java.util.List;

public class InMemoryHistoryManager implements HistoryManager {
    private final LinkedList<Task> tasksHistory = new LinkedList<>();

    @Override
    public void add(Task task) {
        tasksHistory.add(task);
        if (tasksHistory.size() > 10) {
            tasksHistory.remove(0);
        }
    }

    @Override
    public List<Task> getHistory() {
        return tasksHistory;
    }

    @Override
    public void dropHistory() {
        tasksHistory.clear();
    }
}
