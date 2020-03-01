package cn.itcast.spark.steaming

import org.apache.spark.SparkConf
import org.apache.spark.streaming.dstream.{DStream, ReceiverInputDStream}
import org.apache.spark.streaming.{Seconds, StreamingContext}

object StreamingWordCount {
  def main(args: Array[String]): Unit = {
    //todo 构建streaming 应用上下文对象
    val sparkConf = new SparkConf()
      .setMaster("local[3]")
      .setAppName("StreamingWordCount")
    val ssc: StreamingContext = new StreamingContext(sparkConf,Seconds(5))
    // 设置级别
    ssc.sparkContext.setLogLevel("WARN")
    // TODO: 2、从TCP Socket 读取数据 bigdata-cdh03.itcast.cn  9999
    /*
      def socketTextStream(
        hostname: String,
        port: Int,
        // 存储级别觉得是Receiver接收器将接收的数据划分为Block以后的存储级别
        storageLevel: StorageLevel = StorageLevel.MEMORY_AND_DISK_SER_2
      ): ReceiverInputDStream[String]
     */
    val inputDStream: ReceiverInputDStream[String] = ssc.socketTextStream("node01",9999)
    // TODO: 3、针对DStream调用函数进行处理分析（基本与RDD中Transformation函数类似）
    val wordsDStream: DStream[String] =inputDStream.flatMap(line => line.split("\\s+")).filter(word => word.length>0)

    wordsDStream.map(word => (word,1))
      .reduceByKey(_+_)
      .print(10)

    // TODO: 5、启动Streaming应用程序
    ssc.start()
    ssc.awaitTermination()
    ssc.stop(stopSparkContext = true,stopGracefully = true)

  }
}
