import org.apache.poi.ss.usermodel.Sheet;

import java.io.*;

public class Test01 {
    public static void main(String[] args) throws IOException {
//        Test01();
//        Test02();
        Test03();
        Test04();
    }

    private static void Test04() {

    }

    private static void Test03() throws IOException {
        String dir = "D:\\tmp";
        FileInputStream fin=new FileInputStream("D:\\tmp\\test1.xlsx");
        FileOutputStream fout=new FileOutputStream("D:\\tmp\\test1111.xlsx");
        int len=0;
        byte[] buff=new byte[1024];
        while((len=fin.read(buff))!=-1) {
            fout.write(buff, 0, len);
        }
        fin.close();
        fout.close();
    }

    private static void Test02() throws IOException {
        String dir = "D:\\tmp";
        File file = new File(dir);
        File[] files = file.listFiles();
        BufferedReader br = null;
        BufferedWriter bw = null;
        for (File file1 : files) {
            //创建输入流
             br=new BufferedReader(new FileReader(dir + "\\" + file1.getName()));
            //创建输出流
             bw=new BufferedWriter(new FileWriter(dir+"\\temp.xlsx",true));
            String line;//用于接收读到的数据
            while((line=br.readLine())!=null) {
                System.out.println(line);
                bw.write(line);
                bw.write("\r\n");
            }
            bw.flush();
            br.close();
        }
        bw.close();
    }




    private static void Test01() throws IOException {
        String string = "C:\\Users\\Administrator\\Desktop\\balance_history.txt";
        BufferedReader reader = new BufferedReader(new FileReader(new File(string)));
        String l;
        while ((l = reader.readLine()) != null) {
            String replace = l.replace("\\", "");
            System.out.println(replace);
        }
    }
}
