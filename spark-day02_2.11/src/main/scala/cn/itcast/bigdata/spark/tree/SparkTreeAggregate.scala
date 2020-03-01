package cn.itcast.bigdata.spark.tree

import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext, TaskContext}


object SparkTreeAggregate {
  def main(args: Array[String]): Unit = {
    // TODO: 1、构建SparkContext实例对象，传递Spark Application配置信息，封装在SparkConf中
    val sparkConf: SparkConf = new SparkConf()
      .setMaster("local[1]") // 相当于spark-submit提交时 --master参数
      .setAppName("SparkAggregate")
    val sc: SparkContext = new SparkContext(sparkConf)
    sc.setLogLevel("WARN")

    // 通过并行化创建RDD
    val numberRDD: RDD[Int] = sc.parallelize(
      (1 to 10).toList,
      numSlices = 6
    )
    numberRDD.foreachPartition(datas =>
      println(s"p-${TaskContext.getPartitionId()}, datas: ${datas.mkString(", ")}")
    )

//    ClassTag](zeroValue: U)(seqOp: (U, T) => U, combOp: (U, U)
    // 使用RDD中aggregate函数进行聚合操作
    val reslutsTree = numberRDD.treeAggregate(0)(
      (u: Int, item: Int) => {
        println(s"seqOp: p-${TaskContext.getPartitionId()}, $u + $item = ${u + item}")
        u + item
      },
      (u1: Int, u2: Int) => {
        println(s"combOp: p-${TaskContext.getPartitionId()}, $u1 + $u2 = ${u1 + u2}")
        u1 + u2
      },
      // 表示聚合的深度
      depth = 2
    )
    println(s"ResultAgg = $reslutsTree")

    // 为了开发测试，会将线程休眠
    Thread.sleep(1000000)

    // 当应用运行完成以后，关闭资源
    sc.stop()


  }
}
