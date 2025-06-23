package org.example.novaTech.producer;

import lombok.AllArgsConstructor;
import org.example.novaTech.model.Task;
import org.example.ConcurQueueLab;
import org.example.novaTech.utils.TaskStatusEnum;

import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

public class TaskProducer implements Runnable{
    private final Logger logger = Logger.getLogger(TaskProducer.class.getName());
    private final ConcurrentHashMap<UUID, TaskStatusEnum> taskMap;
    private final BlockingQueue<Task> taskQueue;
    private final int numberOfTasks;
    private final AtomicInteger taskCreatedCount;

    public TaskProducer(ConcurrentHashMap<UUID, TaskStatusEnum> taskMap, BlockingQueue<Task> taskQueue, int numberOfTasks, AtomicInteger taskCreatedCount) {
        this.taskMap = taskMap;
        this.taskQueue = taskQueue;
        this.numberOfTasks = numberOfTasks;
        this.taskCreatedCount = taskCreatedCount;
    }

    @Override
    public void run() {
        for(int k=1; k<3; k++) {
            for (int i = 1; i < numberOfTasks; i++) {

                logger.info(Thread.currentThread().getName() + ": creating task " + i);
                Task task = Task.builder()
                        .id(UUID.randomUUID())
                        .name("Task "+ (int) (Math.random()*10)+1)
                        .priority((int) (Math.random()*10)+1)
                        .createdTimestamp(Instant.now())
                        .status(TaskStatusEnum.SUBMITTED)
                        .payload("Payload "+ i+k)
                        .build();

                // Put the task in statusHashMap
                taskMap.put(task.getId(), task.getStatus());

                // Increment created task counter
                taskCreatedCount.incrementAndGet();

                try {
                    taskQueue.put(task);
                    logger.fine(Thread.currentThread().getName() + ": queued task " + task.getName());
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        logger.info(Thread.currentThread().getName() + ": Producer finished");
    }
}
