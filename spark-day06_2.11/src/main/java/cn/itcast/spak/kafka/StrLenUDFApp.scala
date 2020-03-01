package cn.itcast.spak.kafka

import org.apache.spark.{Partition, Partitioner, SparkContext, TaskContext}
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.SparkSession
import org.apache.spark.streaming.kafka010.{HasOffsetRanges, OffsetRange}

object StrLenUDFApp {
  def main(args: Array[String]): Unit = {
    val spark: SparkSession = SparkSession.builder().appName("").master("local[2]").getOrCreate()
    val sc: SparkContext = spark.sparkContext
    sc.setLogLevel("WARN")
    val rdd: RDD[String] = sc.parallelize(Seq("hello word","hello word","hello spark","hello flink"),4)

    rdd.foreach(rdd=>{
      val offsets: Array[OffsetRange] = rdd.asInstanceOf[HasOffsetRanges].offsetRanges
      for (off<-offsets) {
        println(off)
      }
    })

    /*rdd.map((_,1)).foreach(iter=>println(iter))
    rdd.flatMap(_.split("\\s+")).map((_,1)).reduceByKey(_+_).map(_.swap).sortByKey().map(_.swap)
      .foreach(iter=>println(iter))*/
/*    val rdd1: RDD[(String, Int)] = rdd.map((_,1))
    val rdd2: RDD[(String, Int)] = rdd1.map(x=>(x._1,x._2*2))
    val rdd3: RDD[(String, Int)] = rdd1.union(rdd2)
    println("==========================")
    val rdd4: RDD[(String, Int)] = rdd3.distinct(4)
    val length: Int = rdd4.getNumPartitions*/

//    rdd3.reduceByKey((str1,str2)=>str1-str2).foreach(iter=>println(iter))

//    rdd3.foreach(iter=>println(iter))



    spark.stop()



  }

}

class Mypartitions(num: Int) extends Partitioner {
  override def numPartitions: Int = num

  override def getPartition(key: Any): Int = {
    if (key.toString.toInt % 2 != 0) {
      0
    }else{
      1
    }
  }
}
