package cn.itcast.spark.kafka

import java.text.SimpleDateFormat
import java.util.Date

import org.apache.spark.SparkConf
import org.apache.spark.streaming.dstream.{DStream, ReceiverInputDStream}
import org.apache.spark.streaming.kafka.KafkaUtils
import org.apache.spark.streaming.{Seconds, StreamingContext}

/**
  * Streaming流式数据处理，从Kafka Topic读取数据，采用Receiver方式，对每批次的数据进行词频统计WordCount
  */
object StreamingKafkaRecevier {
  def main(args: Array[String]): Unit = {

    // TODO: 1、构建Streaming应用上下文对象
    val sparkConf = new SparkConf()
      .setMaster("local[3]") // 其中一个线程被运行Receiver接收器，剩余两个运行Task任务，并行的运行
      .setAppName("StreamingKafkaRecevier")
    // def this(conf: SparkConf, batchDuration: Duration) 设置批处理时间间隔：划分流式数据时间间隔
    val ssc: StreamingContext = new StreamingContext(sparkConf, Seconds(3))

    // 设置级别
    ssc.sparkContext.setLogLevel("WARN")
    // TODO: 2、从Kafka Topic读取数据
    /*
      def createStream(
        ssc: StreamingContext,
        zkQuorum: String,
        groupId: String,
        topics: Map[String, Int],
        storageLevel: StorageLevel = StorageLevel.MEMORY_AND_DISK_SER_2
      ): ReceiverInputDStream[(String, String)]
     */
    // 表示Kafka Cluster依赖ZookeeperCluster
    val zkQuorum: String = "node01:2181,node02:2181,node03:2181"
    // 读取哪些Topic的数据, Map of (topic_name to numPartitions)
    val topics: Map[String, Int] = Map(
      "mykafka" -> 3
    )
    // 采用Receiver的方式从Kafka Topic中读取数据，使用old Consumer API中High Level API读取数据
    val kafkaDStream: ReceiverInputDStream[(String, String)] = KafkaUtils.createStream(
      ssc, zkQuorum, "streaming-topic-0001", topics
    )

    val inputDStream: DStream[String] = kafkaDStream.map(_._2.toString)

    // TODO: 3、针对DStream调用函数进行处理分析（基本与RDD中Transformation函数类似）
    // def transform[U: ClassTag](transformFunc: RDD[T] => RDD[U]): DStream[U]
    val wordCountsDStream: DStream[(String, Int)] = inputDStream.transform(rdd => {
      rdd
        // a. 将每行数据按照分隔符进行分割为单词，并且过滤为空的单词
        .flatMap(line => line.split("\\s+").filter(word => word.length > 0))
        // b. 将单词转换为二元组，表示每个单词出现一次
        .mapPartitions { words => words.map(word => (word, 1)) }
        // c. 按照单词Key分组，聚合统计出现的次数
        .reduceByKey((a, b) => a + b)
    })

    // TODO: 4、将每批次统计分析的结果进行输出
    // 调用DStream输出函数foreachRDD，针对每批次结果RDD进行输出操作
    wordCountsDStream.foreachRDD { (rdd, time) =>
      // TODO: 打印每批次时间，格式： 2019/07/10 11:39:00
      val batchTime = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(new Date(time.milliseconds))
      println("-----------------------------------")
      println(s"batchTime: $batchTime")
      println("-----------------------------------")

      // 对于Streaming应用来说，输出RDD的时候，需要判断RDD是否存在
      if (!rdd.isEmpty()) {
        // 针对RDD中每个分区数据操作
        rdd.foreachPartition { iter =>
          iter.foreach(println)
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
