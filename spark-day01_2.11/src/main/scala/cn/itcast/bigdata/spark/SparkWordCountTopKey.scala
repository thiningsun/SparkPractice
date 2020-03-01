package cn.itcast.bigdata.spark

import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}

//使用Scala语言，基于Spark框架实现词频统计WordCount,
// 获取词频出现次数最多的三个单词
object SparkWordCountTopKey {
  def main(args: Array[String]): Unit = {
    val sparkConf: SparkConf = new SparkConf()
      .setMaster("local[2]")
      .setAppName("SparkWordCountTopKey")
    val sc: SparkContext = new SparkContext(sparkConf)
    //设置日志级别
    sc.setLogLevel("WARN")
    //读取数据
    val inputRDD: RDD[String] = sc.textFile("datas/wordcount/wordcount.txt")
    // TODO: 3、对要处理的数据进行分析，调用RDD中函数（高阶函数）
    val wordCountRDD: RDD[(String, Int)] = inputRDD
      .flatMap(line => line.split("\\s+"))
      .map(word => (word, 1))
      .reduceByKey(_ + _)
    wordCountRDD.foreach(println)


    println("==============================================================")

    // TODO： 需要对统计词频进行降序排序，获取最大的前三个单词数据
    val topArray: Array[(Int, String)] = wordCountRDD
      .map(tuple => tuple.swap)
      //todo 默认升序,false为降序,另一个参数是
      .sortBy(tuple => tuple._1, ascending = false)
      //      .sortByKey(ascending = false)
      .take(3)

    println("==============================================================22222222")
    wordCountRDD.map(tuple => tuple.swap)
      .sortByKey(ascending = false)
      .top(3)
      .foreach(line =>
        println(line))


    topArray.foreach(println)


    println("==============================================================")

    //todo 方式二：   def sortBy[K](f: (T) => K, ascending: Boolean = true) : RDD[T]
    wordCountRDD.sortBy(tuple => tuple._2, ascending = false)
      .take(3)
      .foreach(x => println(x))
    // 方式三：   def top(num: Int)(implicit ord: Ordering[T]): Array[T]
    wordCountRDD.top(3)(Ordering.by(tuple => tuple._2))
      .foreach(println)


    Thread.sleep(1000000)
    // 当应用运行完成以后，关闭资源
    sc.stop()

  }
}
