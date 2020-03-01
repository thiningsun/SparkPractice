package cn.itcast.spak.kafka;

import scala.actors.threadpool.*;

import java.io.FileOutputStream;

public class MyThread implements Runnable {
    private static int num = 20;

    @Override
    public void run() {
        while (true) {
            synchronized (MyThread.class) {
                if (num > 0) {
                    try {
                        Thread.sleep(200);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    num--;
                    System.out.println(Thread.currentThread().getName() + "卖了票，当前剩余" + num + "张");
                } else {
                    System.out.println(Thread.currentThread().getName() + "票卖光了");
                    break;
                }
            }
        }
    }
}

class Test {
    public static void main(String[] args) throws ExecutionException, InterruptedException {
        int threadNum = 5;
        ExecutorService executorService = Executors.newFixedThreadPool(threadNum);
        for (int i = 0; i < threadNum; i++) {
            MyThread thread = new MyThread();
            Future future = executorService.submit(thread);
            System.out.println("获取值："+future.get());
        }
        try {
            executorService.shutdown();
            executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


        System.out.println("执行完毕");


    }
}
