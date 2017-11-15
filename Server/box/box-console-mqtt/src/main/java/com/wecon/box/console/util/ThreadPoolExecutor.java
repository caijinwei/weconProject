package com.wecon.box.console.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

/**
 * 自定义线程池，可配置线程数
 * @author whp 2017年10月28日下午5:09:01
 */
public class ThreadPoolExecutor {

    /* 单例 */
    private static ThreadPoolExecutor instance = null;

    public static final int SYSTEM_BUSY_TASK_COUNT = 150;
    /* 默认池中线程数 */
    public static int workerNum = 0;
    /* 已经处理的任务数 */
    private static int taskCounter = 0;

    public static boolean systemIsBusy = false;

    private static List<AbstractTask> taskQueue = Collections.synchronizedList(new LinkedList<AbstractTask>());
    /* 池中的所有线程 */
    public PoolWorker[] workers;

    private static final Logger logger = LogManager.getLogger(ThreadPoolExecutor.class);

    /**
     * 初始化线程池，根据不同类型创建不同类型的线程
     * @param initParams
     */
    public void initThreadPool(List<int[]> initParams){
        if(null != initParams){
            for(int[] params : initParams){
                workerNum += params[1];
            }
            workers = new PoolWorker[workerNum];
            int index = 0;
            for(int[] params : initParams){
                for(int i=0;i<params[1];i++){
                    workers[index] = new PoolWorker(index, params[0]);
                    index++;
                }
            }
        }
    }

    private ThreadPoolExecutor() {
        /*workers = new PoolWorker[workerNum];
        for (int i = 0; i < workers.length; i++) {
            workers[i] = new PoolWorker(i);
        }*/
    }

    public static ThreadPoolExecutor getInstance() {
        if (instance == null) {
            synchronized (ThreadPoolExecutor.class) {
                if (instance == null) {
                    instance = new ThreadPoolExecutor();
                }
            }
        }
        return instance;
    }

    /**
     * 增加新的任务
     * 每增加一个新任务，都要唤醒任务队列
     *
     * @param newTask
     */
    public void addTask(AbstractTask newTask) {
        synchronized (taskQueue) {
            newTask.setTaskId(++taskCounter);
            newTask.setSubmitTime(new Date());
            taskQueue.add(newTask);
            /* 唤醒队列, 开始执行 */
            taskQueue.notifyAll();
        }
    }

    /**
     * 批量增加新任务
     *
     * @param taskes
     */
    public void batchAddTask(AbstractTask[] taskes) {
        if (taskes == null || taskes.length == 0) {
            return;
        }
        synchronized (taskQueue) {
            for (int i = 0; i < taskes.length; i++) {
                if (taskes[i] == null) {
                    continue;
                }
                taskes[i].setTaskId(++taskCounter);
                taskes[i].setSubmitTime(new Date());
                taskQueue.add(taskes[i]);
            }
			/* 唤醒队列, 开始执行 */
            taskQueue.notifyAll();
        }
        for (int i = 0; i < taskes.length; i++) {
            if (taskes[i] == null) {
                continue;
            }
        }
    }

    /**
     * 线程池信息
     *
     * @return
     */
    public String getInfo() {
        StringBuffer sb = new StringBuffer();
        sb.append("Task Queue Size:" + taskQueue.size());
        for (int i = 0; i < workers.length; i++) {
            sb.append("Worker " + i + " is " + ((workers[i].isWaiting()) ? "Waiting." : "Running."));
        }
        return sb.toString();
    }

    /**
     * 销毁线程池
     */
    public synchronized void destroy() {
        for (int i = 0; i < workerNum; i++) {
            workers[i].stopWorker();
            workers[i] = null;
        }
        taskQueue.clear();
    }

    /**
     * 池中工作线程
     */
    private class PoolWorker extends Thread {
        private int index = -1;
        /* 该工作线程是否有效 */
        private boolean isRunning = true;
        /* 该工作线程是否可以执行新任务 */
        private boolean isWaiting = true;

        private int type;

        public PoolWorker(int index, int type) {
            this.index = index;
            this.type = type;
            start();
        }

        public void stopWorker() {
            this.isRunning = false;
        }

        public boolean isWaiting() {
            return this.isWaiting;
        }

        /**
         * 循环执行任务
         * 这也许是线程池的关键所在
         */
        public void run() {
            while (isRunning && isWaiting) {
                AbstractTask task = null;
                synchronized (taskQueue) {
                    while (taskQueue.isEmpty()) {
                        try {
							/* 任务队列为空，则等待有新任务加入从而被唤醒 */
                            taskQueue.wait(20);
                        } catch (InterruptedException ie) {
                            ie.printStackTrace();
                        }
                    }
					/* 取出任务执行 */
                    task = taskQueue.remove(0);
                }
                if (task != null && type == task.getType()) {
                    logger.info("线程【"+index+"】，类型【"+type+"】正在执行任务！！！");
                    isWaiting = false;
                    try {

                        task.run();

                    } catch (Exception e) {
                        e.printStackTrace();

                    }
                    isWaiting = true;
                    task = null;
                }
            }
        }
    }
}
