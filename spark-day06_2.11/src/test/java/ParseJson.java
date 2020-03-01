import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import scala.actors.threadpool.locks.ReentrantLock;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class ParseJson {

    public static void main(String[] args) throws IOException {

        //创建输入流
        BufferedReader br=new BufferedReader(new FileReader("D:\\temp文件\\new.txt"));
        String line;//用于接收读到的数据
        while((line=br.readLine())!=null) {
            String[] split = line.split("\\'");
            String jsonStr = split[1].replace("\\","").replace("\"{","{").replace("}\"","}");
            System.out.println(jsonStr);
            JSONObject json = JSON.parseObject(jsonStr);//转成json对象
            String data = json.getString("data");
            JSONArray jsonArray = JSON.parseArray(data);
            for (int i = 0; i < jsonArray.size(); i++) {
                JSONObject arrObj = (JSONObject) jsonArray.get(i);
                String detail=arrObj.getString("detail");
                JSONObject jsonDetail = JSON.parseObject(detail);
                String a = jsonDetail.getString("a");
                String newI = jsonDetail.getString("i");
                System.out.println(a+","+newI);
            }
        }
        br.close();
    }
}
