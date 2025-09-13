package ap2025.hw4;

import java.util.LinkedList;
import java.util.List;

public class BlockingTaskQueue {
    private final List<Task> queue;
    private final int capacity;
    private final Object globalTaskNotificationLock;


    public BlockingTaskQueue(int capacity, Object globalTaskNotificationLock) {
        if (capacity <= 0) {
            throw new IllegalArgumentException("Capacity must be positive for the queue.");
        }
        this.queue = new LinkedList<>();
        this.capacity = capacity;
        this.globalTaskNotificationLock = globalTaskNotificationLock;
    }

    public void put(Task task) throws InterruptedException {
        synchronized (this) {
            while (queue.size() == capacity) {
                wait();
            }
            queue.add(task);
            notifyAll();
        }
        synchronized (globalTaskNotificationLock) {
            globalTaskNotificationLock.notifyAll();
        }
    }

    public synchronized Task take() throws InterruptedException {
        // TODO: ap2025.hw4.BlockingTaskQueue take method (blocking)
        while (queue.isEmpty()) {
            wait();
        }
        Task task = queue.remove(0);
        notifyAll();
        return task;
    }

    public synchronized Task poll() {
        // TODO: ap2025.hw4.BlockingTaskQueue poll method (non-blocking)
        if (queue.isEmpty()) {
            return null;
        }
        Task task = queue.remove(0);
        notifyAll();
        return task;
    }

    public synchronized boolean isEmpty() {
        return queue.isEmpty();
    }

    public synchronized int size() {
        return queue.size();
    }
}