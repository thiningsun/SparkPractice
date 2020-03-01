package cn.itcast.spark.structed

import org.apache.spark.sql.streaming.{OutputMode, StreamingQuery}
import org.apache.spark.sql.{DataFrame, Dataset, SparkSession}

/**
  * 结构化流式处理模块，从Kafka 读取数据（仅支持Kafka New Consumer API），直接获取Topic中的数据
  * 		实时统计各省份销售订单额
  */
object StructuredOrderTotalKafka {
  def main(args: Array[String]): Unit = {
    // TODO: 1、构建SparkSession实例对象，读取数据源的数据，封装在DataFrame/Dataset数据结构中
    val spark: SparkSession = SparkSession.builder()
      .master("local[3]")
      .appName("StructuredOrderTotalKafka")
      .config("spark.sql.shuffle.partitions",3)
      .getOrCreate()

    spark.sparkContext.setLogLevel("WARN")

    // 隐式导入，主要用于RDD转换DataFrame
    import spark.implicits._
    //导入SparkSQL的函数库
    import org.apache.spark.sql.functions._

    // TODO: 2、从Kafka Topic中实时获取数据
    val kafkaStreamDF: DataFrame =spark.readStream
      .format("kafka")
      .option("kafka.bootstrap.servers", "node01:9092,node02:9092,node03:9092")
      .option("subscribe","order")
      .load()

   val provinceAmtStreamDF: DataFrame =kafkaStreamDF
//        .selectExpr("CAST(value AS STRING")
        .selectExpr("CAST(value AS STRING)")
        .as[String]
        .filter($"value".isNotNull and(length(trim($"value"))>0))
      //数据格式 orderId,proviceId,orderPrice
        .mapPartitions{ orders =>
            orders
              .filter(order => order.trim.split(",").length == 3)
              .map{order =>
              val Array(_,provinceId,orderAmt) = order.trim.split(",")
              (provinceId.toInt,orderAmt.toDouble)
         }
    }
      .withColumnRenamed("_1","provinceId").withColumnRenamed("_2","orderAmt")
    // 按照省份ID进行累加订单金额
      .groupBy($"provinceId").agg(sum($"orderAmt").as("amtTotal"))

    // TODO: 将结果打印控制台
    val quary: StreamingQuery =provinceAmtStreamDF.writeStream
      .outputMode(OutputMode.Update())
      .format("console")
      .start()

    quary.awaitTermination()
  }
}
