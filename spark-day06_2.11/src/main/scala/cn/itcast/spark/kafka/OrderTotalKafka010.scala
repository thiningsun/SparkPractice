package cn.itcast.spark.kafka


import java.text.SimpleDateFormat
import java.util.Date

import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.spark.SparkConf
import org.apache.spark.streaming.dstream.{DStream, InputDStream, MapWithStateDStream}
import org.apache.spark.streaming.kafka010._
import org.apache.spark.streaming.{Seconds, State, StateSpec, StreamingContext}

/**
  * Streaming流式数据处理，从Kafka Topic读取数据，使用Kafka 0.10.0提供新的Consumer API读取数据，实时累加统计各个省份销售订单额
  */
object OrderTotalKafka010 {
//  node01:9092,node02:9092,node03:9092
// 检查点目录
val CHECKPOINT_PATH: String = "datas/streaming/state-order-22222222"

  /**
    * 此函数表示从Kafka读取数据，实时统计各个省份销售订单额，最终将结果打印在控制台
    * @param ssc
    *            流式上下文实例对象
    */
  def processStreaming(ssc: StreamingContext): Unit = {

    // TODO: 2、从Kafka Topic中读取数据，集成Kafka 0.10.0以上版本的新的消费者API
    /*
      def createDirectStream[K, V](
        ssc: StreamingContext,
        locationStrategy: LocationStrategy,
        consumerStrategy: ConsumerStrategy[K, V]
      ): InputDStream[ConsumerRecord[K, V]]
     */
    // 相当于：MapReduce处理数据是MapTask本地性，相对于RDD中分区数据处理的Perfered Location
    val locationStrategy: LocationStrategy = LocationStrategies.PreferConsistent
    /*
      def Subscribe[K, V](
        topics: Iterable[jl.String],
        kafkaParams: collection.Map[String, Object]
      ): ConsumerStrategy[K, V]
     */
    // 表示从哪些Topic中读取数据
    val topics: Iterable[String] = Array("order")
    // 表示从Kafka 消费数据的配置参数，传递给KafkaConsumer
    val kafkaParams: collection.Map[String, Object] = Map(
      "bootstrap.servers" ->
        "node01:9092,node02:9092,node03:9092",
      "key.deserializer" -> classOf[StringDeserializer],
      "value.deserializer" -> classOf[StringDeserializer],
      "group.id" -> "kafka-order-group-0001",
      "auto.offset.reset" -> "latest",
      "enable.auto.commit" -> (false: java.lang.Boolean)
    )
    // 指定从Kafka消费数据的消费策略，涵盖topics和配置参数
    val  consumerStrategy: ConsumerStrategy[String, String] = ConsumerStrategies.Subscribe(
      topics, kafkaParams
    )
    // 从Kafka的Topic中读取数据
    val kafkaDStream: InputDStream[ConsumerRecord[String, String]] = KafkaUtils.createDirectStream[String, String](
      ssc, //
      locationStrategy, //
      consumerStrategy
    )


    // TODO: 4、将每批次统计分析的结果进行输出
    kafkaDStream.foreachRDD{ (rdd, time) =>
      // TODO: 打印每批次时间，格式： 2019/07/10 11:39:00
      val batchTime = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(new Date())
      println("-----------------------------------")
      println(s"batchTime: $batchTime")
      println("-----------------------------------")

      // 对于Streaming应用来说，输出RDD的时候，需要判断RDD是否存在
      if(!rdd.isEmpty()){
        rdd.foreachPartition{iter => iter.foreach(println)}

        // TODO: 当直接从Kafka Topic读取数据的RDD为KafkaRDD，里面包含RDD中各个分区偏移量范围
        val offsetRanges: Array[OffsetRange] = rdd.asInstanceOf[HasOffsetRanges].offsetRanges
        for(offsetRange <- offsetRanges){
          println(s"topic: ${offsetRange.topic}   partition: ${offsetRange.partition}  offsets: ${offsetRange.fromOffset} to ${offsetRange.untilOffset}")
        }
      }
    }
  }

  def main(args: Array[String]): Unit = {

    // TODO: 1、构建Streaming应用上下文对象
    /*
      def getOrCreate(
        checkpointPath: String,
        creatingFunc: () => StreamingContext,
        hadoopConf: Configuration = SparkHadoopUtil.get.conf,
        createOnError: Boolean = false
      ): StreamingContext
     */
    val context = StreamingContext.getOrCreate(
      // 当检查点目录存在的时候（非第一次运行），再次启动应用从此目录中的数据恢复StreamingContext
      CHECKPOINT_PATH, //
      // 当检查点目录不存在的时候，创建新的StreamingContxt实例对象，第一次运行的时候
      () => {
        val sparkConf: SparkConf = new SparkConf()
          .setMaster("local[3]") // 其中一个线程被运行Receiver接收器，剩余两个运行Task任务，并行的运行
          .setAppName("OrderTotalMapWithState")
          // TODO: 表示的是每秒钟读取每个分区的数据的条目数的最大值，此时3个分区，BatchInterval为5s，最大数据量：15万
          .set("spark.streaming.kafka.maxRatePerPartition", "10000")
        // def this(conf: SparkConf, batchDuration: Duration) 设置批处理时间间隔：划分流式数据时间间隔
        val ssc: StreamingContext = new StreamingContext(sparkConf, Seconds(5))

        // TODO：有状态的统计，使用以前批次Batch得到的状态，为了安全起见，程勋自动定时的将数据进行Checkpoint到文件系统
        ssc.checkpoint(CHECKPOINT_PATH)

        // 处理数据
        processStreaming(ssc)

        ssc
      }
    )

    // 设置级别
    context.sparkContext.setLogLevel("WARN")

    // TODO: 5、启动Streaming应用程序
    context.start() // 启动Receiver接收器，实时接收数据源的数据
    // 当流式应用启动以后，正常情况下，一直运行，除非程序异常或者人为停止
    context.awaitTermination()

    // 当应用运行完成以后，关闭资源
    context.stop(stopSparkContext = true, stopGracefully = true)
  }


}
