package ap2025.hw4;

import java.util.LinkedList;
import java.util.List;

public class BlockingTaskQueue {
    private final List<Task> list;
    private final int maxSize;
    private final Object mainLock;


    public BlockingTaskQueue(int capacity, Object globalLock) {
        if (capacity <= 0) {
            throw new IllegalArgumentException("size must be > 0");
        }
        this.list = new LinkedList<>();
        this.maxSize = capacity;
        this.mainLock = globalLock;
    }

    public void put(Task task) throws InterruptedException {
        // block for adding item to list
        synchronized (this) {
            while (list.size() == maxSize) {
                wait(); // wait if list is full
            }
            list.add(task);
            notifyAll(); // tell other threads
        }

        // block for waking up workers
        synchronized (mainLock) {
            mainLock.notifyAll();
        }
    }

    public synchronized Task take() throws InterruptedException {
        while (list.isEmpty()) {
            wait();
        }
        Task task = list.remove(0);
        notifyAll();
        return task;
    }

    public synchronized Task poll() {
        if (list.isEmpty()) {
            return null;
        }
        Task task = list.remove(0);
        notifyAll();
        return task;
    }

    public synchronized boolean isEmpty() {
        return list.isEmpty();
    }

    public synchronized int size() {
        return list.size();
    }
}