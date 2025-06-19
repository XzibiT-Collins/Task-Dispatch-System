package org.example.novaTech.consumer;

import org.example.novaTech.model.Task;

import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import org.example.ConcurQueueLab;
import org.example.novaTech.utils.TaskStatusEnum;

public class TaskConsumer implements Runnable{
    private final Logger logger = Logger.getLogger(TaskConsumer.class.getName());

    @Override
    public void run() {
        try {
            Task task = ConcurQueueLab.taskQueue.poll(1, TimeUnit.SECONDS);

            if(task != null) {

                try {
                    //Throw intentional exception to simulate failure
                    if(Math.random() > 0.5){
                        throw new Exception("Intentional Exception");
                    }

                    // Change task status to processing
                    ConcurQueueLab.taskMap.replace(task.getId(), TaskStatusEnum.PROCESSING);

                    logger.info(Thread.currentThread().getName() + ": processing task " + task.getName());

                    // Simulate processing time before marking as processed
                    Thread.sleep(2000);

                    // Mark task as processed in the task object
                    task.setProcessed(true);

                    // Change task status to completed
                    ConcurQueueLab.taskMap.replace(task.getId(), TaskStatusEnum.COMPLETED);

                    // FIXED: Increment counter ONLY after successful processing
                    ConcurQueueLab.taskProcessedCount.incrementAndGet();

                    logger.info(Thread.currentThread().getName() + ": completed task " + task.getName());
                }catch (Exception e){
                    if(task.getRetryCount() < 3){
                        logger.warning(Thread.currentThread().getName() + ": error processing task - " + e.getMessage());
                        task.setRetryCount(task.getRetryCount()+1); //increase retry count on task
                        ConcurQueueLab.taskQueue.put(task); //requeue task
                    }else{
                        task.setStatus(TaskStatusEnum.FAILED);
                        ConcurQueueLab.taskMap.replace(task.getId(), TaskStatusEnum.FAILED);
                        logger.warning("******************************"+
                                Thread.currentThread().getName() +
                                ": Task failed after 3 retry attempts: " +
                                task.getName() +
                                "******************************");
                    }
                }
            } else {
                logger.fine(Thread.currentThread().getName() + ": no task available");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.warning(Thread.currentThread().getName() + ": interrupted while processing task");
        } catch (Exception e) {
            logger.severe(Thread.currentThread().getName() + ": error processing task - " + e.getMessage());
        }
    }
}

