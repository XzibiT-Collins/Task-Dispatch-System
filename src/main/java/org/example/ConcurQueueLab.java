package org.example;

import org.example.novaTech.jsonExport.ExportTaskStatus;
import org.example.novaTech.model.Task;
import org.example.novaTech.producer.TaskProducer;
import org.example.novaTech.consumer.TaskConsumer;
import org.example.novaTech.threadMonitor.ThreadMonitoringLogger;
import org.example.novaTech.utils.TaskStatusEnum;

import java.util.UUID;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

public class ConcurQueueLab{
    private final static Logger logger = Logger.getLogger(ConcurQueueLab.class.getName());
    public static final int numberOfTasks = 10;
    public static BlockingQueue<Task> taskQueue = new PriorityBlockingQueue<>(50);
    public static ConcurrentHashMap<UUID, TaskStatusEnum> taskMap = new ConcurrentHashMap<>();
    public static AtomicInteger taskProcessedCount = new AtomicInteger(0);
    public static AtomicInteger taskCreatedCount = new AtomicInteger(0);

    public static void main(String[] args) throws InterruptedException {

        // Start producers
        for(int i = 0; i < 3; i++){
            new Thread(new TaskProducer()).start();
            Thread.sleep(1000);
        }


        // Consumer thread pool
        ThreadPoolExecutor consumerPool = (ThreadPoolExecutor) Executors.newFixedThreadPool(3);

        //Monitoring Thread
        Thread monitorLogger = new Thread(new ThreadMonitoringLogger(consumerPool));
        monitorLogger.setDaemon(true); //allows logger monitor to run in the background
        monitorLogger.start();


        System.out.println("Producers finished. Total tasks created: " + taskCreatedCount.get());
        System.out.println("Tasks in queue: " + taskQueue.size());

        // FIXED: Submit only as many consumers as there are tasks
        System.out.println("Submitting consumers for " + taskQueue.size() + " tasks");

        // submit tasks to the consumer pool with 3 threads
        for (int i = 0; i < 3; i++) {
            consumerPool.execute(new TaskConsumer());
        }

        // Allow time for task processing
        Thread.sleep(10000);

        // shutdown consumer pool
        consumerPool.shutdown();
        boolean terminated = consumerPool.awaitTermination(30, TimeUnit.SECONDS);

        if (!terminated) {
            logger.info("Consumer pool did not terminate within timeout, forcing shutdown...");
            consumerPool.shutdownNow(); //Terminate all actively executing tasks
        }

        //Scheduled TaskStatusExporter Thread
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(
                new ExportTaskStatus(taskMap),
                0,1, TimeUnit.MINUTES
        );

        // Final summary
        // Status breakdown
        long submitted = taskMap.values().stream().mapToLong(status -> status == TaskStatusEnum.SUBMITTED ? 1 : 0).sum();
        long processing = taskMap.values().stream().mapToLong(status -> status == TaskStatusEnum.PROCESSING ? 1 : 0).sum();
        long completed = taskMap.values().stream().mapToLong(status -> status == TaskStatusEnum.COMPLETED ? 1 : 0).sum();
        long failed = taskMap.values().stream().mapToLong(status -> status == TaskStatusEnum.FAILED ? 1 : 0).sum();


        //Used standard print statements to make them visible in the console
        System.out.println("\n=== FINAL SUMMARY ===");
        System.out.println("Tasks Created: " + taskCreatedCount.get());
        System.out.println("Tasks Processed (Manual Counter): " + taskProcessedCount.get());
        System.out.println("ThreadPool Completed Tasks: " + consumerPool.getCompletedTaskCount());
        System.out.println("Tasks Remaining in Queue: " + taskQueue.size());
        System.out.println("Task Status Breakdown:");
        System.out.println("  FAILED: " + failed);
        System.out.println("  SUBMITTED: " + submitted);
        System.out.println("  PROCESSING: " + processing);
        System.out.println("  COMPLETED: " + completed);
    }
}