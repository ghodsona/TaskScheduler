package ap2025.hw4;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SchedulerMain {
    public static final Object globalTaskNotificationLock = new Object();

    public static void main(String[] args) {
        final int NUM_PRODUCERS = 10;
        final int NUM_CONSUMERS = 2;
        final int TASKS_PER_PRODUCER = 10;
        final int QUEUE_CAPACITY = 3;

        Map<Priority, BlockingTaskQueue> priorityQueues = new HashMap<>();
        for (Priority p : Priority.values()) {
            priorityQueues.put(p, new BlockingTaskQueue(QUEUE_CAPACITY, globalTaskNotificationLock));
        }

        List<Thread> producerThreads = new ArrayList<>();
        List<TaskProducer> producers = new ArrayList<>();
        for (int i = 0; i < NUM_PRODUCERS; i++) {
            TaskProducer producer = new TaskProducer(priorityQueues, i + 1, TASKS_PER_PRODUCER);
            producers.add(producer);
            Thread thread = new Thread(producer);
            producerThreads.add(thread);
            thread.start();
        }

        List<Thread> consumerThreads = new ArrayList<>();
        List<TaskConsumer> consumers = new ArrayList<>();
        for (int i = 0; i < NUM_CONSUMERS; i++) {
            TaskConsumer consumer = new TaskConsumer(priorityQueues, i + 1, globalTaskNotificationLock);
            consumers.add(consumer);
            Thread thread = new Thread(consumer);
            consumerThreads.add(thread);
            thread.start();
        }

        for (Thread thread : producerThreads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        System.out.println("All producers have finished.");


        System.out.println("sending shutdown message to consumers...");
        for (TaskConsumer consumer : consumers) {
            consumer.signalShutdown();
        }


        for (Thread thread : consumerThreads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        System.out.println("all of consumers have shut down. FINISH!");

    }
}