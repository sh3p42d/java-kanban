package Manager;
import Tasks.Task;

import java.util.List;


public interface HistoryManager {
    void add(Task task);
    void removeOneTaskHistory(Integer id);
    List<Task> getHistory();
}
