package cn.itcast.spak.kafka;


import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;
import java.util.concurrent.locks.ReentrantLock;

public class Thread01 implements Callable<String> {

    @Override
    public String call() throws Exception {
        StringBuilder str = new StringBuilder();
        for (int i = 0; i < 10; i++) {
            str.append(i);
        }
        return str.toString();
    }

    public static void main(String[] args) throws Exception {

        FutureTask task = new FutureTask(new Thread01());
        new Thread(task).start();

        System.out.println(task.get());


//        System.out.println(new Thread01().call());
    }

    static  ReentrantLock reentrantLock = new ReentrantLock();

    public static void lock01() {
        reentrantLock.lock();
        System.out.println("aaaaaaaaaaa");
        reentrantLock.unlock();

        synchronized (Thread01.class){
        }
    }

    public  void lock02() {
        synchronized (this){
        }
    }

}
