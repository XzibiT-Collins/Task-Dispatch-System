package org.example;

import org.example.novaTech.model.Task;
import org.example.novaTech.producer.TaskProducer;
import org.example.novaTech.consumer.TaskConsumer;
import org.example.novaTech.threadMonitor.ThreadMonitoringLogger;
import org.example.novaTech.utils.TaskStatusEnum;

import java.util.UUID;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class ConcurQueueLab{
    public static final int numberOfTasks = 10;
    public static BlockingQueue<Task> taskQueue = new PriorityBlockingQueue<>(50);
    public static volatile boolean producersFinished = false;
    public static ConcurrentHashMap<UUID, TaskStatusEnum> taskMap = new ConcurrentHashMap<>();

    // Counter for actually processed tasks
    public static AtomicInteger taskProcessedCount = new AtomicInteger(0);
    // Counter for created tasks (for verification)
    public static AtomicInteger taskCreatedCount = new AtomicInteger(0);

    public static void main(String[] args) throws InterruptedException {

        // Start producers
        ExecutorService producerPool = Executors.newFixedThreadPool(3);
        for(int i = 0; i < 3; i++){
            producerPool.execute(new TaskProducer());
        }
        producerPool.shutdown();

        // Consumer thread pool
        ThreadPoolExecutor consumerPool = (ThreadPoolExecutor) Executors.newFixedThreadPool(3);

        Thread monitorLogger = new Thread(new ThreadMonitoringLogger(consumerPool));
        monitorLogger.setDaemon(true);
        monitorLogger.start();

        // Wait for producers to finish
        producerPool.awaitTermination(30, TimeUnit.SECONDS);
        producersFinished = true;

        System.out.println("Producers finished. Total tasks created: " + taskCreatedCount.get());
        System.out.println("Tasks in queue: " + taskQueue.size());

        // FIXED: Submit only as many consumers as there are tasks
        System.out.println("Submitting consumers for " + taskQueue.size() + " tasks");

        // Submit one consumer per task in queue
        int tasksToProcess = taskQueue.size();
        for (int i = 0; i < tasksToProcess; i++) {
            consumerPool.execute(new TaskConsumer());
        }

        // Wait a bit for processing to start
        Thread.sleep(7000); // Allow time for processing

        // FIXED: Use awaitTermination instead of wait
        consumerPool.shutdown();
        boolean terminated = consumerPool.awaitTermination(30, TimeUnit.SECONDS);

        if (!terminated) {
            System.err.println("Consumer pool did not terminate within timeout, forcing shutdown...");
            consumerPool.shutdownNow();
        }

        // Final summary
        System.out.println("\n=== FINAL SUMMARY ===");
        System.out.println("Tasks Created: " + taskCreatedCount.get());
        System.out.println("Tasks Processed (Manual Counter): " + taskProcessedCount.get());
        System.out.println("ThreadPool Completed Tasks: " + consumerPool.getCompletedTaskCount());
        System.out.println("Tasks Remaining in Queue: " + taskQueue.size());

        // Status breakdown
        long submitted = taskMap.values().stream().mapToLong(status -> status == TaskStatusEnum.SUBMITTED ? 1 : 0).sum();
        long processing = taskMap.values().stream().mapToLong(status -> status == TaskStatusEnum.PROCESSING ? 1 : 0).sum();
        long completed = taskMap.values().stream().mapToLong(status -> status == TaskStatusEnum.COMPLETED ? 1 : 0).sum();

        System.out.println("Task Status Breakdown:");
        System.out.println("  SUBMITTED: " + submitted);
        System.out.println("  PROCESSING: " + processing);
        System.out.println("  COMPLETED: " + completed);
    }
}