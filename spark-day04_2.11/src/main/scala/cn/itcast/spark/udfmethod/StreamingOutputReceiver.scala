package cn.itcast.spark.udfmethod

import org.apache.spark.SparkConf
import org.apache.spark.streaming.{Seconds, StreamingContext}

/**
  * Streaming流式数据处理，从TCP Sockect 读取数据，对每批次的数据进行词频统计WordCount
  */
object StreamingOutputReceiver {
  def main(args: Array[String]): Unit = {
    // TODO: 1、构建Streaming应用上下文对象
    val spark =new SparkConf()
      .setMaster("local[3]")
      .setAppName("StreamingOutputReceiver")
    val ssc: StreamingContext = new StreamingContext(spark,Seconds(5))

    // 设置级别
    ssc.sparkContext.setLogLevel("WARN")
    // TODO: 2、从TCP Socket 读取数据 bigdata-cdh03.itcast.cn  9999
    val inputDStream = ssc.receiverStream(new CustomReceive("bigdata-cdh03.itcast.cn", 9999))


//    ssc.receiverStream()
  }
}
