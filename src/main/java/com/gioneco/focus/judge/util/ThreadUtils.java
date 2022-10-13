package com.gioneco.focus.judge.util;

import java.util.concurrent.Executor;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author DJ
 * @className ThreadUtils
 * @Description
 * @date 2022-10-11 11:01
 */
public class ThreadUtils {
    
    
    public static void run(String threadPoolName,Runnable runnable) {
        Object bean = BeanUtil.getBean(threadPoolName);
        if (bean instanceof Executor) {
            Executor executor = (Executor) bean;
            executor.execute(runnable);
        }
    }
    
    public static void schedule(String threadPoolName,Runnable runnable, long delay, TimeUnit unit) {
        Object bean = BeanUtil.getBean(threadPoolName);
        if (bean instanceof ScheduledExecutorService) {
            ScheduledExecutorService  executor = (ScheduledExecutorService ) bean;
            executor.schedule(runnable,delay,unit);
        }
    }
    
}
