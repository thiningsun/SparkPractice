package cn.itcast.spak.kafka

import org.apache.spark.broadcast.Broadcast
import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}

object ScalaHelloWorld {
  def main(args: Array[String]): Unit = {
    val sparkConf: SparkConf = new SparkConf()
      .setAppName("HelloWorld")
      .setMaster("local[2]")

    val sc: SparkContext = new SparkContext(sparkConf)
    val list1 = List(("jame",23), ("wade",3), ("kobe",24))
    val list2 = List(("jame","cave"), ("wade","bulls"), ("kobe","lakers"))
    val rdd1 = sc.makeRDD(list1)
    val rdd2 = sc.makeRDD(list2)
/*    val tuples: Array[(String, (Int, String))] = rdd1.join(rdd2).collect()
    for (t<-tuples) {
      print(t._1+","+t._2)
    }*/

    val broadcastValue: Broadcast[Array[(String, Int)]] = sc.broadcast(rdd1.collect())
   /* val value: RDD[(String, (String, Null))] = rdd2.map((rdd: (String, String)) => {
      val value: Array[(String, Int)] = broadcastValue.value
      for (v <- value) {
        if (rdd._1.equals(v._1)) {
          return (rdd._1, (rdd._2, v._2.toString))
        }
      }
      (rdd._1, (rdd._2.toString, null))
    })

    print(value.collect())*/
/*    val inputRDD: RDD[String] = sc.textFile("")
    sc.setLogLevel("WARN")
    val wordCount: RDD[(String, Int)] = inputRDD
      .filter(line => line.length > 0)
      .flatMap(world => world.split("\\s+"))
      .map((_, 1))
      .reduceByKey(_ + _)
    wordCount.foreachPartition(iter=>
    iter.foreach(line=>println(line))
    )*/

    sc.stop()

  }
}
