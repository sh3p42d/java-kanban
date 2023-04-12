package Manager;

import Tasks.Task;

import java.util.ArrayList;
import java.util.List;

public class InMemoryHistoryManager implements HistoryManager {
    private final List<Task> tasksHistory = new ArrayList<>(10);

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
}
