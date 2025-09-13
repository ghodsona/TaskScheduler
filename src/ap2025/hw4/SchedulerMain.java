package ap2025.hw4;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SchedulerMain {
    public static final Object THE_LOCK = new Object();

    public static void main(String[] args) {
        // Configs
        final int MAKERS = 10;
        final int TAKERS = 2;
        final int JOBS_PER_MAKER = 10;
        final int Q_SIZE = 3;

        Map<Priority, BlockingTaskQueue> allQueues = new HashMap<>();
        for (Priority p : Priority.values()) {
            allQueues.put(p, new BlockingTaskQueue(Q_SIZE, THE_LOCK));
        }

        // Make and run producer threads
        List<Thread> makerThreads = new ArrayList<>();
        List<TaskProducer> producers = new ArrayList<>();
        for (int i = 0; i < MAKERS; i++) {
            TaskProducer p = new TaskProducer(allQueues, i + 1, JOBS_PER_MAKER);
            producers.add(p);
            Thread t = new Thread(p);
            makerThreads.add(t);
            t.start();
        }

        // Make and run consumer threads
        List<Thread> takerThreads = new ArrayList<>();
        List<TaskConsumer> consumers = new ArrayList<>();
        for (int i = 0; i < TAKERS; i++) {
            TaskConsumer c = new TaskConsumer(allQueues, i + 1, THE_LOCK);
            consumers.add(c);
            Thread t = new Thread(c);
            takerThreads.add(t);
            t.start();
        }

        // Wait for makers to finish
        for (Thread t : makerThreads) {
            try {
                t.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        System.out.println(">>> All makers are done.");

        // Tell takers to stop
        System.out.println(">>> Sending stop signal to takers...");
        for (TaskConsumer c : consumers) {
            c.signalShutdown();
        }

        // Wait for takers to finish
        for (Thread t : takerThreads) {
            try {
                t.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        System.out.println(">>> All done. End of program.");
    }
}