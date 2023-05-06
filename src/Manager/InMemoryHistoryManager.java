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
        final Node<Task> oldTail = this.tail;
        final Node<Task> newNode = new Node<>(oldTail, task, null);
        this.tail = newNode;

        if (oldTail == null) {
            this.head = newNode;
        } else {
            oldTail.next = newNode;
        }
        tasksMap.put(task.getTaskId(), newNode);
    }

    private void removeNode(Integer id) {
        if (tasksMap.containsKey(id)) {
            Node<Task> node = tasksMap.remove(id);
            Node<Task> next = node.next;
            Node<Task> prev = node.prev;

            if (prev == null) {
                this.head = next;
            } else {
                prev.next = next;
                node.prev = null;
            }

            if (next == null) {
                this.tail = prev;
            } else {
                next.prev = prev;
                node.next = null;
            }

            node.data = null;
        }
    }

    @Override
    public List<Task> getHistory() {
        List<Task> tasksHistory = new ArrayList<>();
        Node<Task> node = head;

        // head не может быть null, если просмотрена хотя бы одна задача
        while (node != null) {
            tasksHistory.add(node.data);
            node = node.next;
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
