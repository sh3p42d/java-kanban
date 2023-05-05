package Manager;

import Tasks.Task;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class InMemoryHistoryManager implements HistoryManager {
    private final HashMap<Integer, Node<Task>> tasksMap = new HashMap<>();

    // Реализация двусвязного списка через узлы
    private static class Node<E> {
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
        tasksMap.put(task.getTaskId(), newNode);
    }

    private void removeNode(Integer id) {
        if (tasksMap.containsKey(id)) {
            Node<Task> node = tasksMap.remove(id);
            node.prev = node.next;
        }
    }

    public List<Task> getHistory() {
        List<Task> tasksHistory = new ArrayList<>();
        for (Node<Task> task : tasksMap.values()) {
            tasksHistory.add(task.data);
        }
        return tasksHistory;
    }

    @Override
    public void add(Task task) {
        if (task != null) {
            removeNode(task.getTaskId());
            linkLast(task);
        }
    }

    @Override
    public void removeOneTaskHistory(Integer id) {
        removeNode(id);
    }
}
