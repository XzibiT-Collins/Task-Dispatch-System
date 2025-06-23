package org.example;

import org.example.novaTech.jsonExport.ExportTaskStatus;
import org.example.novaTech.model.Task;
import org.example.novaTech.producer.TaskProducer;
import org.example.novaTech.consumer.TaskConsumer;
import org.example.novaTech.threadMonitor.ThreadMonitoringLogger;
import org.example.novaTech.utils.TaskStatusEnum;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class ConcurQueueLab{
    private final static Logger logger = Logger.getLogger(ConcurQueueLab.class.getName());
    private static final int numberOfTasks = 10;
    private static final BlockingQueue<Task> taskQueue = new PriorityBlockingQueue<>(50);
    private static ConcurrentHashMap<UUID, TaskStatusEnum> taskMap = new ConcurrentHashMap<>();
    private static AtomicInteger taskProcessedCount = new AtomicInteger(0);
    private static AtomicInteger taskCreatedCount = new AtomicInteger(0);

    public static void main(String[] args) throws InterruptedException {

        // Start producers
        for(int i = 0; i < 3; i++){
            new Thread(new TaskProducer(taskMap,taskQueue,numberOfTasks,taskCreatedCount)).start();
            Thread.sleep(1000);
        }


        // Consumer thread pool
        ThreadPoolExecutor consumerPool = (ThreadPoolExecutor) Executors.newFixedThreadPool(3);

        //Monitoring Thread
        Thread monitorLogger = new Thread(new ThreadMonitoringLogger(consumerPool,taskQueue,taskMap,taskCreatedCount,taskProcessedCount));
        monitorLogger.setDaemon(true); //allows logger monitor to run in the background
        monitorLogger.start();


        System.out.println("Producers finished. Total tasks created: " + taskCreatedCount.get());
        System.out.println("Tasks in queue: " + taskQueue.size());

        // FIXED: Submit only as many consumers as there are tasks
        System.out.println("Submitting consumers for " + taskQueue.size() + " tasks");

        // submit tasks to the consumer pool with 3 threads
        for (int i = 0; i < 3; i++) {
            consumerPool.execute(new TaskConsumer(taskQueue,taskMap,taskProcessedCount));
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
        Map<TaskStatusEnum, Long> statusBreakdown = taskMap.values().stream().collect(Collectors.groupingBy(
                task-> task,
                Collectors.counting()
        ));

        //Used standard print statements to make them visible in the console
        System.out.println("\n=== FINAL SUMMARY ===");
        System.out.println("Tasks Created: " + taskCreatedCount.get());
        System.out.println("Tasks Processed (Manual Counter): " + taskProcessedCount.get());
        System.out.println("ThreadPool Completed Tasks: " + consumerPool.getCompletedTaskCount());
        System.out.println("Tasks Remaining in Queue: " + taskQueue.size());
        System.out.println("Task Status Breakdown:");
        statusBreakdown.forEach((status,count) -> System.out.println("  " + status + ": " + count));


        //shut down monitor log and scheduled export threads
        monitorLogger.join();
        if(monitorLogger.isAlive()){
            monitorLogger.interrupt();
        }

        if(!scheduler.isTerminated()){
            scheduler.shutdown();
            logger.info("Scheduled task status export thread successfully shut down");
        }
    }
}