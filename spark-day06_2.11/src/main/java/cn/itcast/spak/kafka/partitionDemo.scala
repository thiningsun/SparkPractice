package cn.itcast.spak.kafka

import org.apache.log4j.{Level, Logger}
import org.apache.spark.rdd.RDD
import org.apache.spark.{HashPartitioner, SparkConf, SparkContext, TaskContext}

/**
  * repartition和partitionBy的区别
  */

object partitionDemo {
  def main(args: Array[String]): Unit = {
//    Logger.getLogger("org.apache.spark").setLevel(Level.WARN)
    val conf: SparkConf = new SparkConf().setAppName("localTest").setMaster("local[4]")
    val sc = new SparkContext(conf)
    sc.setLogLevel("WARN")
    //设置4个分区;
    val rdd = sc.parallelize(List("hello", "jason", "what", "are", "you", "doing","hi","jason","do","you","eat","dinner","hello","jason","do","you","have","some","time","hello","jason","time","do","you","jason","jason"),6)

    val wordCount=rdd.flatMap(word=>word.split(",")).map((_,1))
    println(wordCount.getNumPartitions+" :-->分区个数")
    val rep: RDD[(String, Int)] = wordCount.repartition(10)
    rep.foreachPartition(pair=>{
      println("第几个分区-----"+TaskContext.getPartitionId()+"-"+Thread.currentThread().getName)
      pair.foreach(p=>{
        println(p)
      })
    })

    print("************************")

    val parBy: RDD[(String, Int)] = wordCount.partitionBy(new HashPartitioner(10))
    parBy.foreachPartition(pair=>{
      println("第几个分区-----"+TaskContext.getPartitionId()+"-"+Thread.currentThread().getName)
      pair.foreach(p=>println(p))
    })

    println(parBy.getNumPartitions+"-->分区个数")

    parBy.reduceByKey(_+_).foreachPartition(iter=>{
      println("第几个分区-----"+TaskContext.getPartitionId()+"-"+Thread.currentThread().getName)
      iter.foreach(word=>println(word))
    })

    sc.stop()
  }
}
