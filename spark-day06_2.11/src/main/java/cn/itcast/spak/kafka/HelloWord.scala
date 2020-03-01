package cn.itcast.spak.kafka

import org.apache.spark.{SparkConf, SparkContext}

object HelloWord extends App {
    val sparkConf: SparkConf = new SparkConf()
      .setAppName("HelloWorld")
      .setMaster("local[2]")
    val sc: SparkContext = new SparkContext(sparkConf)
    sc.setLogLevel("WARN")

    val numbers: Seq[Int] = Seq(1, 2, 3, 4, 5)
    //15
    val i: Int = numbers.foldRight(0)(_-_)
    print(i)

}
