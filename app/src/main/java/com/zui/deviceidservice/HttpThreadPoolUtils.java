package com.zui.deviceidservice;


import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by liufeng23 on 2017/7/31.
 * 设定一个http线程池来管理所有的http网络连接线程。
 */

public class HttpThreadPoolUtils {
    private HttpThreadPoolUtils() {

    }

    private static int CORE_THREAD_SIZE = 5;

    private static int MAX_THREAD_SIZE = 10;

    private static int KEEP_ALIVE_TIME = 10000;

    private static ThreadPoolExecutor threadPool;

    private static BlockingQueue<Runnable> workQueue = new ArrayBlockingQueue<Runnable>(10);

    // 线程工厂
    private static ThreadFactory threadFactory = new ThreadFactory() {
        private final AtomicInteger integer = new AtomicInteger();

        @Override
        public Thread newThread(Runnable r) {
            return new Thread(r, "myThreadPool thread:"
                    + integer.getAndIncrement());
        }
    };
    static {
        threadPool = new ThreadPoolExecutor(CORE_THREAD_SIZE, MAX_THREAD_SIZE, KEEP_ALIVE_TIME,
                TimeUnit.SECONDS, workQueue, threadFactory);
    }

    /**
     * 从线程池中抽取线程，执行指定的Runnable对象
     *
     * @param runnable
     */
    public static void execute(Runnable runnable) {
        threadPool.execute(runnable);
    }

}