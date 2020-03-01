public class SortWord {
    /*public static int[] qsort(int arr[],int start,int end) {
        int pivot = arr[start];
        int i = start;
        int j = end;
        while (i<j) {
            while ((i<j)&&(arr[j]>pivot)) {
                j--;
            }
            while ((i<j)&&(arr[i]<pivot)) {
                i++;
            }
            //当两个值相等,但是角标未重合时候,应该继续循环
            if ((arr[i]==arr[j])&&(i<j)) {
                j--;
            } else {
                int temp = arr[i];
                arr[i] = arr[j];
                arr[j] = temp;
            }
        }
        for (int i1 : arr) {
            System.out.print(i1+",");
        }
        System.out.println();
        //当角标的值大于起始角标的时候说明循环未结束,应该继续
        if (i-1>start) arr=qsort(arr,start,i-1);
        //当角标的值小于起始角标的时候说明循环未结束,应该继续
        if (j+1<end) arr=qsort(arr,j+1,end);
        return (arr);
    }*/

    public static void main(String[] args) {
        /*int arr[] = new int[]{3,3,3,7,9,4656,34,34,4656,5,6,7,8,9,343,23};
        int len = arr.length-1;
        arr=qsort(arr,0,len);
        for (int i:arr) {
            System.out.print(i+"\t");
        }*/
        int a = 10;
        a = a >> 2;
        System.out.println(a);

    }
}
