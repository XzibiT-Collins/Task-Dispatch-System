package org.example.novaTech.consumer;

import org.example.novaTech.model.Task;
import java.util.logging.Logger;

class TaskConsumer implements Runnable{
    private final Logger logger = Logger.getLogger(Consumer.class.getName());

    @Override
    public void run() {
        while(!Thread.currentThread().isInterrupted()) {
            try {
                Task task = ConcurQueueLab.taskQueue.take();

                logger.info(Thread.currentThread().getName() + ": processing task " + task.getName());
                task.setProcessed(true);

            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }
}

