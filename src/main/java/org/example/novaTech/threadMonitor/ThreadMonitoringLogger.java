package org.example.novaTech.threadMonitor;

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
            logger.info("Thread Pool Status: "+ consumerPool.isTerminated() +
                    " Queue Size: "+ consumerPool.getQueue().size() +" "+
                    "Processed Tasks: "+ consumerPool.getCompletedTaskCount());

            //sleep for 5 seconds
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }
}
