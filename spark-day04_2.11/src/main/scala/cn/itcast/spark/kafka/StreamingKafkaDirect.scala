package cn.itcast.spark.kafka

import java.text.SimpleDateFormat
import java.util.Date

import org.apache.spark.SparkConf
import org.apache.spark.streaming.dstream.{DStream, InputDStream}
import org.apache.spark.streaming.kafka.KafkaUtils
import org.apache.spark.streaming.{Seconds, StreamingContext}
import kafka.serializer.StringDecoder

/**
  * Streaming流式数据处理，从Kafka Topic读取数据，采用Direct方式，对每批次的数据进行词频统计WordCount
  */
object StreamingKafkaDirect {
  def main(args: Array[String]): Unit = {

    // TODO: 1、构建Streaming应用上下文对象
    val sparkConf = new SparkConf()
      .setMaster("local[3]") // 其中一个线程被运行Receiver接收器，剩余两个运行Task任务，并行的运行
      .setAppName("StreamingKafkaDirect")
      // TODO: 表示的是每秒钟读取每个分区的数据的条目数的最大值，此时3个分区，BatchInterval为5s，最大数据量：15万
      .set("spark.streaming.kafka.maxRatePerPartition", "10000")
    // def this(conf: SparkConf, batchDuration: Duration) 设置批处理时间间隔：划分流式数据时间间隔
    val ssc: StreamingContext = new StreamingContext(sparkConf, Seconds(5))

    // 设置级别
    ssc.sparkContext.setLogLevel("WARN")

    // TODO: 2、从Kafka Topic读取数据
    /*
          def createDirectStream[
          K: ClassTag,
          V: ClassTag,
          KD <: Decoder[K]: ClassTag,
          VD <: Decoder[V]: ClassTag] (
            ssc: StreamingContext,
            kafkaParams: Map[String, String],
            topics: Set[String]
          ): InputDStream[(K, V)]
         */

    // Kafka Cluster相关配置参数
    val kafkaParams: Map[String, String] = Map(
      // 表示的事Kafka Cluster地址信息
      "metadata.broker.list" ->
        "node01:9092,node02:9092,node03:9092",
      // 表示从Topic的哪个偏移量开始读取数据，默认值最大偏移量，就是从从新的数据开始读取
      "auto.offset.reset" -> "largest"
    )
    // 从哪些Topic中读取数据
    val topics: Set[String] = Set("test")
    // 采用Direct方式从Kafka Topics中读取数据
    val kafkaDStream: InputDStream[(String, String)] = KafkaUtils.createDirectStream[String, String, StringDecoder, StringDecoder](
      ssc, kafkaParams, topics
    )


    kafkaDStream.foreachRDD(tuple =>tuple.keys+ "==============="+ tuple.values )

    kafkaDStream.foreachRDD(line => println(line))

    println("=============================")

    val inputDStream: DStream[String] = kafkaDStream.map(_._2.toString)

    val wordCountDStream: DStream[(String, Int)] = inputDStream.transform {
      rd =>
        rd.flatMap(_.split("\\s+"))
          .filter(word => word.length > 0)
          .mapPartitions(words => words.map(words => (words, 1)))
          .reduceByKey(_ + _)
    }

    wordCountDStream.foreachRDD { (rdd, time) =>
      val batchTime = new SimpleDateFormat("yyyy/MM/dd HH/mm/ss").format(new Date(time.milliseconds))
      println("-----------------------------------")
      println(s"batchTime: $batchTime")
      println("-----------------------------------")

      if (!rdd.isEmpty()) {
        rdd.foreachPartition {
          iter => iter.foreach(println)
        }
      }

    }
    // TODO: 5、启动Streaming应用程序
    ssc.start() // 启动Receiver接收器，实时接收数据源的数据

    // 当流式应用启动以后，正常情况下，一直运行，除非程序异常或者人为停止
    ssc.awaitTermination()

    // 当应用运行完成以后，关闭资源
    ssc.stop(stopSparkContext = true, stopGracefully = true)
  }
}
