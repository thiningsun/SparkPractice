package cn.itcast.spark.streaming.window

import java.text.SimpleDateFormat
import java.util.{Date, Properties}

import kafka.serializer.StringDecoder
import org.apache.spark.SparkConf
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.{DataFrame, Row, SaveMode, SparkSession}
import org.apache.spark.sql.types._
import org.apache.spark.streaming.dstream.{DStream, InputDStream}
import org.apache.spark.streaming.kafka.KafkaUtils
import org.apache.spark.streaming.{Seconds, StreamingContext}

/**
  * Streaming流式数据处理，从Kafka Topic读取数据，采用Direct方式，实时窗口统计最近时间内的各个省份订单数
  * 	对窗口内的数据使用Dataset进行分析处理（集成SparkSQL），将结果存储到MySQL数据表中
  */
object OrderCountWindowSQL {

	// 检查点目录
	val CHECKPOINT_PATH: String = "datas/streaming/state-order-8766ddd3333"

	// Streaming 批处理时间间隔BatchInterval
	val STREAMING_BATCH_INTERVAL: Int = 3
	// 窗口时间间隔设置
	val STREAMING_WINDOW_INTERVAL: Int = STREAMING_BATCH_INTERVAL * 2
	val STREAMING_SLIDER_INTERVAL: Int = STREAMING_BATCH_INTERVAL * 1

	/**
	  * 此函数表示从Kafka读取数据，实时统计各个省份销售订单额，最终将结果打印在控制台
	  * @param ssc
	  *            流式上下文实例对象
	  */
	def processStreaming(ssc: StreamingContext): Unit = {
		// TODO: 2、从Kafka Topic读取数据
		// Kafka Cluster相关配置参数
		val kafkaParams: Map[String, String] = Map(
			// 表示的事Kafka Cluster地址信息
			"metadata.broker.list" ->
				"bigdata-cdh01.itcast.cn:9092,bigdata-cdh02.itcast.cn:9092,bigdata-cdh03.itcast.cn:9092",
			// 表示从Topic的哪个偏移量开始读取数据，默认值最大偏移量，就是从从新的数据开始读取
			"auto.offset.reset" -> "largest"
		)
		// 从哪些Topic中读取数据
		val topics: Set[String] = Set("orderTopic")
		// 采用Direct方式从Kafka Topics中读取数据
		val kafkaDStream: InputDStream[(String, String)] = KafkaUtils.createDirectStream[String, String, StringDecoder, StringDecoder](
			ssc, kafkaParams, topics
		)

		// TODO: 设置窗口操作，窗口的大小和滑动大小
		/*
			def window(windowDuration: Duration, slideDuration: Duration): DStream[T]
		 */
		val windowDStream: DStream[(String, String)] = kafkaDStream.window(
			Seconds(STREAMING_WINDOW_INTERVAL), // 设置窗口统计中窗口大小
			Seconds(STREAMING_SLIDER_INTERVAL) // 设置滑动大写，每隔多长时间执行一次
		)

		// TODO: 4、将每批次统计分析的结果进行输出
		windowDStream.foreachRDD{ (rdd, time) =>
			// TODO: 打印每批次时间，格式： 2019/07/10 11:39:00
			val batchTime = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(new Date(time.milliseconds))
			println("-----------------------------------")
			println(s"batchTime: $batchTime")
			println("-----------------------------------")

			// 对于Streaming应用来说，输出RDD的时候，需要判断RDD是否存在
			if(!rdd.isEmpty()){
				// TODO: 集成SparkSQL分析，需要将RDD转换Dataset/DataFrame, 分析处理并将结果保存到MySQL表中
				// a. 将RDD数据类型转换为Row
				val rowOrdersRDD: RDD[Row] = rdd
			    	// 过滤不合格的数据
			    	.filter(order => null != order._2 && order._2.trim.split(",").length >= 3)
					// 针对RDD中各个分区数据操作，将每条订单数据转换为Row类型数据
			    	.mapPartitions{ iter =>
						iter.map{ case(_, order) =>
							// 按照分隔符进行分割数据
							val Array(orderId, provinceId, orderAmt) = order.trim.split(",")
						 	// 返回Row类型
							Row(orderId, provinceId.toInt, orderAmt.toDouble)
						}
					}

				// b. 自定义Schema，指定字段名称和字段类型
				val orderSchema: StructType = StructType(
					Array(
						StructField("orderId", StringType, nullable = true),
						StructField("provinceId", IntegerType, nullable = true),
						StructField("orderAmt", DoubleType, nullable = true)
					)
				)

				// c. 构建SparkSession实例对象
				// 参考文档：http://spark.apache.org/docs/2.2.0/streaming-programming-guide.html#dataframe-and-sql-operations
				val spark = SparkSession.builder().config(rdd.sparkContext.getConf).getOrCreate()
				import spark.implicits._

				// d. 创建DataFrame
				val ordersDF: DataFrame = spark.createDataFrame(rowOrdersRDD, orderSchema)

				// e. 编写DSL分析
				val provinceCountRDD: DataFrame = ordersDF.groupBy($"provinceId").count()

				// f. 将数据保存到MySQL表中
				provinceCountRDD
			    	.coalesce(1)
			    	.write
			    	.mode(SaveMode.Overwrite)
			    	.option("driver", "com.mysql.jdbc.Driver")
			    	.jdbc(
						"jdbc:mysql://bigdata-cdh01.itcast.cn:3306/test?user=root&password=123456",
						"order_province_cnt",
						new Properties()
					)
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
				val sparkConf = new SparkConf()
					.setMaster("local[3]") // 其中一个线程被运行Receiver接收器，剩余两个运行Task任务，并行的运行
					.setAppName("OrderCountWindowSQL")
					// TODO: 表示的是每秒钟读取每个分区的数据的条目数的最大值，此时3个分区，BatchInterval为5s，最大数据量：15万
					.set("spark.streaming.kafka.maxRatePerPartition", "10000")
				// def this(conf: SparkConf, batchDuration: Duration) 设置批处理时间间隔：划分流式数据时间间隔
				val ssc: StreamingContext = new StreamingContext(sparkConf, Seconds(STREAMING_BATCH_INTERVAL))

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
