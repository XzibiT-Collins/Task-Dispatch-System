package org.example.novaTech.producer;

import lombok.AllArgsConstructor;
import org.example.novaTech.model.Task;
import org.example.ConcurQueueLab;
import java.time.Instant;
import java.util.UUID;
import java.util.logging.Logger;

@AllArgsConstructor
public class TaskProducer implements Runnable{
    private final Logger logger = Logger.getLogger(TaskProducer.class.getName());

    @Override
    public void run() {
        for(int k=1; k<3; k++) {
            for (int i = 1; i < ConcurQueueLab.numberOfTasks; i++) {

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
                ConcurQueueLab.taskMap.put(task.getId(), task.getStatus());

                // Increment created task counter
                ConcurQueueLab.taskCreatedCount.incrementAndGet();

                try {
                    ConcurQueueLab.taskQueue.put(task);
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
