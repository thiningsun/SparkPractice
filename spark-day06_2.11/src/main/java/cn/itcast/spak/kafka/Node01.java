package cn.itcast.spak.kafka;


import javafx.scene.Node;

import java.util.HashMap;

public class Node01 {

    private Node01 next;
    private Object item;

    public Node01(Node01 next, Object item) {
        this.next = next;
        this.item = item;
    }

    public static void main(String[] args) {
        HashMap<Object, Object> map = new HashMap<>(16,0.75f);
        map.put("hello", "world01");
        map.put("hello", "world02");
        map.put("hello", "world03");

        System.out.println(map.get("hello"));

    }
}
