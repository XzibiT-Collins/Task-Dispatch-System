package org.example.novaTech.threadMonitor;

import org.example.ConcurQueueLab;
import org.example.novaTech.model.Task;
import org.example.novaTech.utils.TaskStatusEnum;

import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

public class ThreadMonitoringLogger implements Runnable{
    private final Logger logger = Logger.getLogger(ThreadMonitoringLogger.class.getName());
    private final ThreadPoolExecutor consumerPool;
    private final BlockingQueue<Task> taskQueue;
    private final ConcurrentHashMap<UUID, TaskStatusEnum> taskMap;
    private final AtomicInteger taskCreatedCount;
    private final AtomicInteger taskProcessedCount;

    public ThreadMonitoringLogger(ThreadPoolExecutor consumerPool, BlockingQueue<Task> taskQueue, ConcurrentHashMap<UUID, TaskStatusEnum> taskMap ,AtomicInteger taskCreatedCount, AtomicInteger taskProcessedCount) {
        this.consumerPool = consumerPool;
        this.taskQueue = taskQueue;
        this.taskMap = taskMap;
        this.taskCreatedCount = taskCreatedCount;
        this.taskProcessedCount = taskProcessedCount;
    }

    @Override
    public void run() {
        while(!consumerPool.isTerminated()){
            logger.info(String.format(
                    "Pool Status - Active: %d, Queue: %d, Completed: %d, Tasks in BlockingQueue: %d",
                    consumerPool.getActiveCount(),
                    consumerPool.getQueue().size(),
                    consumerPool.getCompletedTaskCount(),
                    taskQueue.size()
            ));

            // Log task counts with better formatting
            logger.info(String.format(
                    "Task Counts - Created: %d, Processed: %d, Pool Completed: %d",
                    taskCreatedCount.get(),
                    taskProcessedCount.get(),
                    consumerPool.getCompletedTaskCount()
            ));

            // Log task status distribution
            long submitted = taskMap.values().stream()
                    .mapToLong(status -> status == TaskStatusEnum.SUBMITTED ? 1 : 0).sum();
            long processing = taskMap.values().stream()
                    .mapToLong(status -> status == TaskStatusEnum.PROCESSING ? 1 : 0).sum();
            long completed = taskMap.values().stream()
                    .mapToLong(status -> status == TaskStatusEnum.COMPLETED ? 1 : 0).sum();

            logger.info(String.format(
                    "Status Distribution - SUBMITTED: %d, PROCESSING: %d, COMPLETED: %d",
                    submitted, processing, completed
            ));

            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }

        logger.info("MonitorLogger terminated");
    }
}
