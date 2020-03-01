package cn.itcast.bigdata.spark
import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}
object SparkWordCount {
  def main(args: Array[String]): Unit = {
    //TODO: Spark Application 的Driver Program，
    // 必须要创建SparkContext实例对象，读取数据和调度Job执行
    val sparkConf = new SparkConf()
      .setMaster("local[2]")
      .setAppName("SparkCount")
    val sc = new SparkContext(sparkConf)
    sc.setLogLevel("WARN")

    // TODO: 2、读取数据, 从本地文件系统读取数据文件数据
    //    val inputRDD: RDD[String] = sc.textFile("datas/wordcount/wordcount.txt")
    val inputRDD: RDD[String] = sc.parallelize(List("lijie hello lisi", "zhangsan wangwu mazi", "hehe haha nihaoa heihei lure hehe hello word"))
    /*
        // TODO: 3、对要处理的数据进行分析，调用RDD中函数（高阶函数）
        val wordCountRDD: RDD[(String, Int)] = inputRDD
          // 对每行数据按照分割符分割
          .flatMap(line => line.split("\\s+"))
          // 将单词转换为二元组，表示每个单词出现一次
          .map(word => (word, 1))
          // 按照Key进行聚合操作，将相同的Key的出现的次数相加
    //      .reduce(_+_)
          .reduceByKey((a, b) => a + b)*/
    val wordCountRDD: RDD[(String, Int)] = inputRDD
      .filter(line => line.length > 0)
      .flatMap(line => line.split("\\s+"))
      .map((_, 1))

    wordCountRDD.partitionBy(new MyPartitioner(4))
      .saveAsTextFile("D:\\tmp\\file")
    println(wordCountRDD.collect().toBuffer)
    // 当应用运行完成以后，关闭资源
    sc.stop()
  }
}
