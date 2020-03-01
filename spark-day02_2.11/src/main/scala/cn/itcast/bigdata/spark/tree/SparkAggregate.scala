package cn.itcast.bigdata.spark.tree

import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext, TaskContext}

/**
  * 使用Scala语言，基于Spark框架实现词频统计WordCount
  */
object SparkAggregate {
  def main(args: Array[String]): Unit = {
    // TODO: Spark Application 的Driver Program，
    // TODO 必须要创建SparkContext实例对象，读取数据和调度Job执行
    val sparkConf: SparkConf = new SparkConf()
      .setMaster("local[1]") // 相当于spark-submit提交时 --master参数
      .setAppName("SparkAggregate")
    val sc: SparkContext = SparkContext.getOrCreate(sparkConf)
    sc.setLogLevel("WARN")

    // 通过并行化创建RDD
    val numbersRDD: RDD[Int] = sc.parallelize(
      (1 to 10).toList,
      numSlices = 6
    )
    numbersRDD.foreachPartition(
      datas =>
        println(s"p-${TaskContext.getPartitionId()}, datas: ${datas.mkString(",")}")
    )

    numbersRDD.foreach(x => print(s"$x, "))

    println()
    // todo 使用RDD中aggregate函数进行聚合操作
       /*
       def aggregate[U: ClassTag]
       (zeroValue: U)
       (
         seqOp: (U, T) => U,
         combOp: (U, U) => U
       ): U
       */

    val reslutAgg: Int = numbersRDD.aggregate(0)(
      (u: Int, item: Int) => {
        println(s"seqOp: p-${TaskContext.getPartitionId()}, $u + $item = ${u + item}")
        u + item
      },
      (u1: Int, u2: Int) => {
        println(s"combOp: p-${TaskContext.getPartitionId()}, $u1 + $u2 = ${u1 + u2}")
        u1 + u2
      }
    )
    println(s"Resluts= $reslutAgg")

    // 为了开发测试，会将线程休眠
    Thread.sleep(1000000)

    // 当应用运行完成以后，关闭资源
    sc.stop()
  }
}
