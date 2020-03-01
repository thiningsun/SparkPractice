package cn.itcast.spak.kafka;

import sun.applet.Main;

import java.util.Stack;

/**
 * 单例模式,懒汉加载
 */
public class ArlenTest01 implements Runnable {
    private static ArlenTest01 arlenTest01;
    public int age = 20;
    public String name = "zhangsan";

    public ArlenTest01() {

    }

    public static ArlenTest01 getInstance() {
        if (arlenTest01 == null) {
            synchronized (ArlenTest01.class){
                if (arlenTest01 == null) {
                    arlenTest01 = new ArlenTest01();
                }
            }
        }

        return arlenTest01;
    }

    @Override
    public void run() {
        for (int i = 0; i < 10; i++) {
            System.out.println(i+Thread.currentThread().getName());

        }
    }
}


 class Student{
     public static void main(String[] args) {
         ArlenTest01 instance = ArlenTest01.getInstance();
         System.out.println(instance.age);
         System.out.println(instance.name);
     }
}

