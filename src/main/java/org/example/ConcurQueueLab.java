package org.example;

import org.example.novaTech.model.Task;
import org.example.novaTech.producer.TaskProducer;
import org.example.novaTech.consumer.TaskConsumer;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;

public class ConcurQueueLab{
    public static final int numberOfTasks = 50;
    public static BlockingQueue<Task> taskQueue = new PriorityBlockingQueue<>(100);

    public static void main(String[] args) throws InterruptedException {

        for(int i = 0; i<3; i++){
            new Thread(new TaskProducer()).start();
            //sleep for .5 seconds
//            Thread.sleep(500);
        }

        //consumer thread pool
        //ExecutorService consumerPool = Executors.newFixedThreadPool(3);

        //for metrics
        ThreadPoolExecutor consumerPool = (ThreadPoolExecutor) Executors.newFixedThreadPool(3);

        Thread monitorLogger = new Thread(new MonitorLogger(consumerPool));
        monitorLogger.start();

        for(int i = 0; i<600; i++){
            consumerPool.execute(new TaskConsumer());

            //sleep consumer for 2 seconds
//            Thread.sleep(2000);
        }


        //allow consumers more time for consumption of tasks
        Thread.sleep(10000);
        consumerPool.shutdown();
    }
}