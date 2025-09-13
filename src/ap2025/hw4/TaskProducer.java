package ap2025.hw4;

import java.util.Map;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

public class TaskProducer implements Runnable {
    private final Map<Priority, BlockingTaskQueue> queues;
    private final int makerId;
    private final int howMany;
    private static AtomicInteger taskIdCounter = new AtomicInteger(0);
    private volatile boolean needToStop = false;

    public TaskProducer(Map<Priority, BlockingTaskQueue> priorityQueues, int producerId, int numberOfTasksToProduce) {
        this.queues = priorityQueues;
        this.makerId = producerId;
        this.howMany = numberOfTasksToProduce;
    }

    @Override
    public void run() {
        System.out.println("Maker " + makerId + " started.");
        Random random = new Random();
        for (int i = 0; i < howMany && !needToStop; i++) {
            try {
                int taskId = taskIdCounter.getAndIncrement();

                Priority p;
                int r = random.nextInt(3);
                if (r == 0) {
                    p = Priority.HIGH;
                } else if (r == 1) {
                    p = Priority.MEDIUM;
                } else {
                    p = Priority.LOW;
                }

                Task task = new Task(taskId, "Some task data", p);
                BlockingTaskQueue q = queues.get(p);
                System.out.println("Maker " + makerId + " makes " + task);
                q.put(task);

                Thread.sleep(random.nextInt(151) + 50);

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.err.println("Maker " + makerId + " stopped by interrupt.");
                break;
            }
        }
        System.out.println("Maker " + makerId + " finished.");
    }

    public void requestShutdown() {
        this.needToStop = true;
    }
}