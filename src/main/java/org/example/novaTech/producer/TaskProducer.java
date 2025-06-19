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
        for(int k=1; k<5; k++) {
            for (int i = 1; i < ConcurQueueLab.numberOfTasks; i++) {

                logger.info(Thread.currentThread().getName() + ": creating task " + i);
                Task task = Task.builder()
                        .id(UUID.randomUUID())
                        .name("Task "+ (int) (Math.random()*10)+1)
                        .priority((int) (Math.random()*10)+1)
                        .createdTimestamp(Instant.now())
                        .payload("Payload "+ i+k)
                        .build();
                try {
                    ConcurQueueLab.taskQueue.put(task);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
            //delay thread
            try {
                Thread.sleep(500);
                System.out.println();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
