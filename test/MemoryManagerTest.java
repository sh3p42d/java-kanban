import Manager.InMemoryTaskManager;
import Manager.Managers;

import org.junit.jupiter.api.BeforeEach;


class MemoryManagerTest extends TasksManagerTest<InMemoryTaskManager> {

    @BeforeEach
    public void beforeEachInit() {
        manager = (InMemoryTaskManager) Managers.getDefault();
        createAllTestTask(manager);
        getHistoryAllTestTask(manager);
    }
}