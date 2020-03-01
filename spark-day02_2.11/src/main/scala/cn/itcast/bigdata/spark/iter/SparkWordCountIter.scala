package cn.itcast.bigdata.spark.iter

import org.apache.spark.broadcast.Broadcast
import org.apache.spark.rdd.RDD
import org.apache.spark.storage.StorageLevel
import org.apache.spark.{SparkConf, SparkContext}

/**
  * 基于Scala语言,实现词频率统计
  */
object SparkWordCountIter {
  def main(args: Array[String]): Unit = {
    val sparkConf = new SparkConf()
      .setMaster("local[2]")
      .setAppName("SparkWordCountIter")
    val sc: SparkContext = SparkContext.getOrCreate(sparkConf)

    // TODO: 2、读取数据, 通过并行化函数将集合数据转换为RDD
    val inputRDD: RDD[String] = sc.textFile("datas/wordcount/wordcount.txt")

    // TODO: 3、对要处理的数据进行分析，调用RDD中函数（高阶函数）
    val wordCountRDD: RDD[(String, Int)] = inputRDD
      .flatMap(line => line.split("\\s+")
      .filter(word => word.length > 0))
      .mapPartitions(iter => iter.map {
        word => (word, 1)
      })
      .reduceByKey(_ + _)
    // TODO：4、将结果数据保存到文件系统中（本地文件系统）
    wordCountRDD
      .coalesce(1)
      .foreachPartition(iter =>
          iter.foreach{ tuple => println(tuple)
      })

    wordCountRDD.persist(StorageLevel.MEMORY_AND_DISK)


    // 为了开发测试，会将线程休眠
    Thread.sleep(1000000)

    // 当应用运行完成以后，关闭资源
    sc.stop()
  }
}
