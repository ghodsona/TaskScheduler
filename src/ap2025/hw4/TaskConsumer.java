package ap2025.hw4;

import java.util.Map;
import java.util.Random;

public class TaskConsumer implements Runnable {
    private final Map<Priority, BlockingTaskQueue> allQueues;
    private final int workerNum; // workerId -> workerNum
    private final Object mainLock;


    public TaskConsumer(Map<Priority, BlockingTaskQueue> priorityQueues, int workerId, Object globalTaskNotificationLock) {
        this.allQueues = priorityQueues;
        this.workerNum = workerId;
        this.mainLock = globalTaskNotificationLock;
    }

    volatile boolean isStopping = false; // A simpler flag name

    @Override
    public void run() {
        System.out.println("Worker " + workerNum + " begin.");
        Random random = new Random();

        try {
            while (true) {
                Task job = null; // task -> job

                synchronized (mainLock) {
                    while ((job = getOneJob()) == null) { // findNextTask -> getOneJob
                        if (isStopping && areAllQueuesEmpty()) {
                            System.out.println("Worker " + workerNum + " is stopping.");
                            return;
                        }
                        mainLock.wait();
                    }
                }

                if (job.isCancelled()) {
                    System.out.println("Worker " + workerNum + " skip cancelled job " + job.getId());
                    continue;
                }

                System.out.println("Worker " + workerNum + " got job " + job.getId());
                Thread.sleep(random.nextInt(401) + 100); // simulate work
                System.out.println("Worker " + workerNum + " finished job " + job.getId());
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("Worker " + workerNum + " interrupted.");
        }
    }

    // Back to a simple if-else structure instead of a loop
    private Task getOneJob() {
        Task t = allQueues.get(Priority.HIGH).poll();
        if (t != null) {
            return t;
        }

        t = allQueues.get(Priority.MEDIUM).poll();
        if (t != null) {
            return t;
        }

        t = allQueues.get(Priority.LOW).poll();
        return t;
    }

    private boolean areAllQueuesEmpty() {
        for (Priority p : Priority.values()) {
            if (!allQueues.get(p).isEmpty()) {
                return false;
            }
        }
        return true;
    }

    public void signalShutdown() {
        isStopping = true;
        synchronized (mainLock) {
            mainLock.notifyAll();
        }
    }
}