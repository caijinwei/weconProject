package com.wecon.box.console.main;

import com.wecon.box.console.task.SubscribeTask;
import com.wecon.box.console.task.TestTask;
import org.apache.logging.log4j.LogManager;

/**
 * Created by zengzhipeng on 2017/7/25.
 */
public class Program {
    private static final org.apache.logging.log4j.Logger logger = LogManager.getLogger(Program.class.getName());

    public static void main(String[] args) throws InterruptedException {
        String profile = "dev";
        if (args.length > 0) {
            profile = args[0];
        }
        logger.info("run profile:" + profile);
        System.setProperty("spring.profiles.active", profile);

        SubscribeTask taskTest = new SubscribeTask();
        taskTest.start();

        /*TestTask taskTest = new TestTask();
        taskTest.start();*/
    }
}
