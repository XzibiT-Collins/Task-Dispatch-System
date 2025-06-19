package org.example.novaTech.threadMonitor;

import org.example.ConcurQueueLab;
import org.example.novaTech.utils.TaskStatusEnum;

import java.util.concurrent.ThreadPoolExecutor;
import java.util.logging.Logger;

public class ThreadMonitoringLogger implements Runnable{
    private final Logger logger = Logger.getLogger(ThreadMonitoringLogger.class.getName());
    private final ThreadPoolExecutor consumerPool;

    public ThreadMonitoringLogger(ThreadPoolExecutor consumerPool) {
        this.consumerPool = consumerPool;
    }

    @Override
    public void run() {
        while(!consumerPool.isTerminated()){
            logger.info(String.format(
                    "Pool Status - Active: %d, Queue: %d, Completed: %d, Tasks in BlockingQueue: %d",
                    consumerPool.getActiveCount(),
                    consumerPool.getQueue().size(),
                    consumerPool.getCompletedTaskCount(),
                    ConcurQueueLab.taskQueue.size()
            ));

            // Log task counts with better formatting
            logger.info(String.format(
                    "Task Counts - Created: %d, Processed: %d, Pool Completed: %d",
                    ConcurQueueLab.taskCreatedCount.get(),
                    ConcurQueueLab.taskProcessedCount.get(),
                    consumerPool.getCompletedTaskCount()
            ));

            // Log task status distribution
            long submitted = ConcurQueueLab.taskMap.values().stream()
                    .mapToLong(status -> status == TaskStatusEnum.SUBMITTED ? 1 : 0).sum();
            long processing = ConcurQueueLab.taskMap.values().stream()
                    .mapToLong(status -> status == TaskStatusEnum.PROCESSING ? 1 : 0).sum();
            long completed = ConcurQueueLab.taskMap.values().stream()
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
