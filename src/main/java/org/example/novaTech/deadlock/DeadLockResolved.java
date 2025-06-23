package org.example.novaTech.deadlock;

import java.util.logging.Logger;

public class DeadLockResolved {
    private static final Logger logger = Logger.getLogger(DeadLockResolved.class.getName());
    private static final Object resourceOne = new Object();
    private static final Object resourceTwo = new Object();

    public static void main(String[] args) {
        Thread threadOne = new Thread(() -> {
            synchronized (resourceOne) {
                logger.info("Thread One locked resource 1");

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    logger.info("Thread One interrupted: "+ e.getMessage());
                }

                synchronized (resourceTwo) {
                    logger.info("Thread One locked resource 2");
                    logger.info("Thread One is doing work...");
                }
            }
        });

        Thread threadTwo = new Thread(() -> {
            synchronized (resourceOne) {
                logger.info("Thread Two locked resource 1");

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    logger.info("Thread Two interrupted: "+ e.getMessage());
                }

                synchronized (resourceTwo) {
                    logger.info("Thread Two locked resource 2");
                    logger.info("Thread Two is doing work...");
                }
            }
        });

        threadOne.start();
        threadTwo.start();

        try {
            threadOne.join();
            threadTwo.join();
        } catch (InterruptedException e) {
            logger.info("Unable to join threads: "+ e.getMessage());
        }
    }
}
