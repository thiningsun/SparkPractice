package cn.itcast.bigdata.spark

import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}

object SparkWordCountSubmit {
  //todo spark Application的Drive program,必须要创建SparkContext实例对象
  def main(args: Array[String]): Unit = {

    // 判断参数是否传递正确
    if (args.length<2) {
      println("Usage： SparkWordCountSubmit <input> <output>")
      System.exit(-1)
    }

    // TODO: 1、构建SparkContext实例对象，传递Spark Application配置信息，封装在SparkConf中

    val sparkConf = new SparkConf()
      .setMaster("local[2]")
      .setAppName("SparkWordCount")

    val sc: SparkContext = new SparkContext(sparkConf)

    //datas/wordcount/wordcount.txt
    // TODO: 2、读取数据, 从本地文件系统读取数据文件数据
    val inputRDD: RDD[String] = sc.textFile(args(0))

    // TODO: 3、对要处理的数据进行分析，调用RDD中函数（高阶函数）
    val wordcount: RDD[(String, Int)] = inputRDD.flatMap(line => line.split("\\s+"))
      .map(word => (word, 1))
      .reduceByKey(_ + _)

    wordcount.foreachPartition(iter=>{
      iter.foreach(word =>println(word))
    })
//    wordcount.foreach(tuple=>println(tuple))

//    wordcount.saveAsTextFile(args(1)+"-"+System.currentTimeMillis())

    sc.stop()
  }
}
