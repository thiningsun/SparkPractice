package cn.itcast.bigdata.spark;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.FlatMapFunction;
import org.apache.spark.api.java.function.PairFunction;
import org.apache.spark.api.java.function.VoidFunction;
import scala.Tuple2;

import java.util.Arrays;
import java.util.Iterator;

public class SparkWordCountJava {
    public static void main(String[] args) {

        SparkConf sparkConf = new SparkConf()
                .setMaster("local[2]").setAppName("SparkWordCountJava");

        //创建上下文实例
        JavaSparkContext jsc = new JavaSparkContext(sparkConf);
        JavaRDD<String> inputRDD = jsc.textFile("datas/wordcount/wordcount.txt");
        // TODO: 3、对数据进行处理，调用RDD中函数
        // a. 将每行数据分割分单词
        JavaRDD<String> wordRDD = inputRDD.flatMap(
                new FlatMapFunction<String, String>() {
                    @Override
                    public Iterator<String> call(String line) throws Exception {
                        return Arrays.asList(line.split("\\s+")).iterator();
                    }
                }
        );
        // b. 将单词转换为二元组
        JavaPairRDD<String, Integer> wordCount = wordRDD.mapToPair(
                new PairFunction<String, String, Integer>() {
                    @Override
                    public Tuple2<String, Integer> call(String s) throws Exception {
                        return new Tuple2<>(s, 1);
                    }
                }
        );

        // TODO: 4、将数据保存到文件系统
        wordCount.saveAsTextFile("datas/wordcount-" + System.currentTimeMillis());

        wordCount.foreach(
                new VoidFunction<Tuple2<String, Integer>>() {
                    @Override
                    public void call(Tuple2<String, Integer> stringIntegerTuple2) throws Exception {
                        System.out.println(stringIntegerTuple2);
                    }
                }
        );
        // 为了查看程序运行，线程休眠
        try {
            Thread.sleep(1000000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // 当应用结束的使用，关闭资源
        jsc.stop();

    }
}
