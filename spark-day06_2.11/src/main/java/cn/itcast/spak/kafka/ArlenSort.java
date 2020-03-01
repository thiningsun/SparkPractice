package cn.itcast.spak.kafka;

public class ArlenSort {
    public static void main(String[] args) {
        int[] arr = {1, 22, 33, 4, 6};
        int[] sort = sort(arr);

        for (int i : sort) {
            System.out.print(i+",");
        }

    }


    public static int[] sort(int[] arr) {

        //n个元素,只需要找n-1次最大值就能成正序排列
        for (int i = 0; i < arr.length-1; i++) {
            for (int j = 0; j < arr.length-1-i; j++) {
                if (arr[j] > arr[j + 1]) {
                    int temp = arr[j];
                    arr[j] = arr[j + 1];
                    arr[j + 1] = temp;
                }
            }
        }
        return arr;
    }
}
