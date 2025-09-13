package ap2025.hw4;

import java.util.Map;
import java.util.Random;

public class TaskConsumer implements Runnable {
    private final Map<Priority, BlockingTaskQueue> priorityQueues;
    private final int workerId;
    private final Object globalTaskNotificationLock;


    public TaskConsumer(Map<Priority, BlockingTaskQueue> priorityQueues, int workerId, Object globalTaskNotificationLock) {
        this.priorityQueues = priorityQueues;
        this.workerId = workerId;
        this.globalTaskNotificationLock = globalTaskNotificationLock;
    }

    volatile boolean shutdownSignalReceived = false;

    @Override
    public void run() {
        System.out.println("Worker " + workerId + " (Thread: " + Thread.currentThread().getName() + ") started.");
        Random random = new Random();

        try {
            while (true) {
                Task task = null;

                synchronized (globalTaskNotificationLock) {
                    while ((task = attemptTakeByPriority()) == null) {
                        if (shutdownSignalReceived && areAllQueuesEmpty()) {
                            System.out.println("Worker " + workerId + " shutting down as all queues are empty.");
                            return;
                        }
                        globalTaskNotificationLock.wait();
                    }
                }

                if (task.isCancelled()) {
                    System.out.println("Worker " + workerId + " skipped cancelled " + task);
                    continue;
                }

                System.out.println("Worker " + workerId + " retrieved " + task);
                System.out.println("Worker " + workerId + " is processing " + task);
                Thread.sleep(random.nextInt(401) + 100);
                System.out.println("Worker " + workerId + " finished processing " + task);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("Worker " + workerId + " was interrupted. Shutting down.");
        }
        System.out.println("Worker " + workerId + " finished run method.");
    }

    private Task attemptTakeByPriority() {
        Task task = priorityQueues.get(Priority.HIGH).poll();
        if (task != null) return task;

        task = priorityQueues.get(Priority.MEDIUM).poll();
        if (task != null) return task;

        task = priorityQueues.get(Priority.LOW).poll();
        return task;
    }

    private boolean areAllQueuesEmpty() {
        for (Priority p : Priority.values()) {
            if (!priorityQueues.get(p).isEmpty()) {
                return false;
            }
        }
        return true;
    }

    public void signalShutdown() {
        shutdownSignalReceived = true;
        synchronized (globalTaskNotificationLock) {
            globalTaskNotificationLock.notifyAll();
        }
    }
}