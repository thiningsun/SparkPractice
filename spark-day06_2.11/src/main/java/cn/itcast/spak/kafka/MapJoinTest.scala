package cn.itcast.spak.kafka

import java.util.UUID

import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.sql.SparkSession

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

object MapJoinTest {

  def main(args: Array[String]): Unit = {

    run(100)
  }

  def run(num: Int): Unit = {
    val buff: ArrayBuffer[String] = ArrayBuffer[String]()

    val map = new mutable.HashMap[String, String]
    for (x <- 1 to num) {
      val key = UUID.randomUUID.toString
      buff += key
    }
    buff += "mark-01"
    println(buff)

    var startTime = System.currentTimeMillis();
    for (x <- buff) {
      map.put(x, x)
    }
    val conf = new SparkConf()
    conf.setMaster("local[2]")
    System.setProperty("HADOOP_USER_NAME", "hadoop")
    conf.set("spark.driver.maxResultSize", "10G")
    val spark: SparkSession = SparkSession.builder().config(conf).appName("GroupByCollectAsMapTest").getOrCreate()

    val sc: SparkContext = spark.sparkContext

    val mapData: mutable.HashMap[String, String] = sc.broadcast(map).value

    val data: RDD[String] = sc.parallelize(buff)
      .repartition(9)

    val collectData = data.filter(x => {
      !mapData.contains(x)
    })

    println(" mapjoin  total use :       " + ((System.currentTimeMillis() - startTime) / 1000) + " millisecond  ;  Data : " + collectData.count())

    val buffJoin = ArrayBuffer[String]()
    for (elem <- buff) {
      buffJoin += elem
    }
    buffJoin -= "mark-01"

    println(buffJoin.size)


    val leftData = data.map(x => {
      (x, 1)
    })

    val rightData = sc.parallelize(buffJoin).map(x => {
      (x, 2)
    })

    startTime = System.currentTimeMillis();

    val joinRdd = leftData.leftOuterJoin(rightData).filter(x => {

      None.equals(x._2._2)
    })
    println(" join  total use :       " + ((System.currentTimeMillis() - startTime)) + " millisecond  ;  Data : " + joinRdd.count)


  }
}