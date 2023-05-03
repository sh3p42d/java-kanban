package Manager;

import Tasks.Task;
import java.util.ArrayList;
import java.util.HashMap;

public class InMemoryHistoryManager implements HistoryManager {
    private final ArrayList<Task> tasksHistory = new ArrayList<>();
    private final HashMap<Integer, Node<Task>> tasksMap = new HashMap<>();

    // Реализация двусвязного списка через узлы
    static class Node<E> {
        public E data;
        public Node<E> next;
        public Node<E> prev;

        public Node(Node<E> prev, E data, Node<E> next) {
            this.data = data;
            this.next = next;
            this.prev = prev;
        }
    }

    private Node<Task> head;
    private Node<Task> tail;

    private void linkLast(Task task) {
        final Node<Task> oldTail = tail;
        final Node<Task> newNode = new Node<>(oldTail, task, null);
        tail = newNode;

        if (oldTail == null) {
            head = newNode;
        } else {
            oldTail.prev = newNode;
        }
        getTasks(task.getTaskId(), newNode);
    }

    private void getTasks(int taskId, Node<Task> node) {
        if (tasksMap.containsKey(taskId)) {
            removeNode(taskId);
        }
        tasksMap.put(taskId, node);

        tasksHistory.clear();
        for (Node<Task> taskNode : tasksMap.values()) {
            tasksHistory.add(taskNode.data);
        }
    }

    private void removeNode(int id) {
        tasksMap.remove(id);
    }

    @Override
    public void add(Task task) {
        linkLast(task);

        if (tasksHistory.size() > 10) {
            tasksHistory.remove(0);
        }
    }

    @Override
    public ArrayList<Task> getHistory() {
        return tasksHistory;
    }

    @Override
    public void dropHistory() {
        tasksMap.clear();
        tasksHistory.clear();
    }

    @Override
    public void remove(int id) {
        // Тут очень хотелось добавить проверку на наличие записи, т.к. если не посмотреть все подзадачи эпика
        // будет NullPointerException
        tasksHistory.remove(tasksMap.get(id).data);
        removeNode(id);
    }
}
