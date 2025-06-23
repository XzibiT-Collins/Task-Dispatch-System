package org.example.novaTech.consumer;

import org.example.novaTech.model.Task;

import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;
import org.example.novaTech.utils.TaskStatusEnum;

public class TaskConsumer implements Runnable{
    private final Logger logger = Logger.getLogger(TaskConsumer.class.getName());
    private final BlockingQueue<Task> taskQueue;
    private final ConcurrentHashMap<UUID, TaskStatusEnum> taskMap;
    private final AtomicInteger taskProcessedCount;

    public TaskConsumer(BlockingQueue<Task> blockingQueue, ConcurrentHashMap<UUID, TaskStatusEnum> taskMap, AtomicInteger taskProcessedCount) {
        this.taskQueue = blockingQueue;
        this.taskMap = taskMap;
        this.taskProcessedCount = taskProcessedCount;
    }

    @Override
    public void run() {
        try {
            while (true) {
                Task task = taskQueue.poll(1, TimeUnit.SECONDS);
                if(task == null) {
                    logger.fine(Thread.currentThread().getName() + ": no task available");
                    break;
                }
                else{
                    try {
                        //Throw intentional exception to simulate failure
                        if (Math.random() > 0.5) {
                            throw new Exception("Intentional Exception");
                        }

                        // Change task status to processing
                        taskMap.replace(task.getId(), TaskStatusEnum.PROCESSING);
                        task.setStatus(TaskStatusEnum.PROCESSING);

                        logger.info(Thread.currentThread().getName() + ": processing task " + task.getName());

                        // Simulate processing time before marking as processed
                        Thread.sleep(2000);

                        // Mark the task as processed in the task object
                        task.setProcessed(true);
                        task.setStatus(TaskStatusEnum.COMPLETED);

                        // Change task status to completed
                        taskMap.replace(task.getId(), TaskStatusEnum.COMPLETED);

                        // Increment counter ONLY after successful processing
                        taskProcessedCount.incrementAndGet();

                        logger.info(Thread.currentThread().getName() + ": completed task " + task.getName());
                    } catch (Exception e) {
                        if (task.getRetryCount() < 3) {
                            logger.warning(Thread.currentThread().getName() + ": error processing task - " + e.getMessage());
                            task.setRetryCount(task.getRetryCount() + 1); //increase retry count on task
                            taskQueue.put(task); //requeue task
                        } else {
                            task.setStatus(TaskStatusEnum.FAILED);
                            taskMap.replace(task.getId(), TaskStatusEnum.FAILED);
                            logger.warning(
                                    Thread.currentThread().getName() +
                                    ": Task failed after 3 retry attempts: " +
                                    task.getName());
                        }
                    }
                }
            }
        } catch (InterruptedException e) {
            logger.info("Consumer thread interrupted");
            Thread.currentThread().interrupt();
        }
    }
}

