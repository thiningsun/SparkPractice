package cn.itcast.spak.kafka;

import sun.management.Agent;

public class Single {

    private static Single single;
    private static int age=20;

    public Single() {
    }

    public static Single getInstace() {
        if (single == null) {
            synchronized (Single.class) {
                if (single == null) {
                    single = new Single();
                }
            }
        }
        return single;
    }


    public static void main(String[] args) {

    }
}


