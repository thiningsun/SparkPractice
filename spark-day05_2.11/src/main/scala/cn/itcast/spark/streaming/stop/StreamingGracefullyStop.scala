package cn.itcast.spark.streaming.stop

import java.text.SimpleDateFormat
import java.util.Date

import kafka.serializer.StringDecoder
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs.Path
import org.apache.spark.SparkConf
import org.apache.spark.streaming.dstream.{DStream, InputDStream, MapWithStateDStream}
import org.apache.spark.streaming.kafka.KafkaUtils
import org.apache.spark.streaming.{Seconds, State, StateSpec, StreamingContext}

/**
  * Streaming流式数据处理，从Kafka Topic读取数据，采用Direct方式，实时累加统计各个省份销售订单额
  */
object StreamingGracefullyStop {

	// 检查点目录
	val CHECKPOINT_PATH: String = "datas/streaming/state-order-22299"

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


		// TODO: 3、实时统计各个省份销售订单额，数据格式：orderId,provinceId,orderPrice
		val ordersDStream: DStream[(Int, Double)] = kafkaDStream.transform{ rdd =>
			rdd
				// 过滤异常数据（为null，字段不对）
				.filter(msg => null != msg._2 && msg._2.trim.split(",").length >= 3)
				// 提取字段：provinceId,orderPrice
				.mapPartitions{ datas =>
					datas.map{ case(_, order) =>
						val Array(orderId, provinceId, orderPrice) = order.toString.split(",")
						// 返回二元组
						(provinceId.toInt, orderPrice.toDouble)
					}
				}
				// 聚合，按照各个省份聚合, 获取得到此批次中各个省份的订单销售额
				.reduceByKey((a, b) => a + b)
		}

		/**
		  * 使用新的状态更新函数，实时累加统计各个省份销售订单额
		  * 函数：mapWithState
		  * 	针对每批次中数据，一条一条的更新状态，有数据就更新，没有就不更新，也是依据Key更新状态
		  * 声明：
		  *	  def mapWithState[StateType: ClassTag, MappedType: ClassTag](
				  spec: StateSpec[K, V, StateType, MappedType]
				): MapWithStateDStream[K, V, StateType, MappedType]
		  	StateSpec：
		  		表示：封装如何一条一条数据进行更新状态
		    参数函数：
		  		- 第一个泛型参数：StateType
		  			状态的类型，针对应用就是省份总的销售订单额，Double
		  		- 第二个泛型参数：MappedType
		  			表示每条数据更新状态以后，返回给应用参数类型，针对此应用来说: 二元组 -> (省份ID， 省份总的销售订单额）
		  */
		/*
		  def function[KeyType, ValueType, StateType, MappedType](
		  	  // 此函数，才是真正对每批次中每条数据更新状态的
			  mappingFunction: (KeyType, Option[ValueType], State[StateType]) => MappedType
			): StateSpec[KeyType, ValueType, StateType, MappedType]
		 */
		val spec: StateSpec[Int, Double, Double, (Int, Double)] = StateSpec.function(
			// (KeyType, Option[ValueType], State[StateType]) => MappedType
			(provinceId: Int, orderAmt: Option[Double], state: State[Double]) => {
				// i. 获取当前批次中Key的状态（省份销售额
				val currentState = orderAmt.getOrElse(0.0)
				// ii. 获取Key的以前状态（省份的以前销售额）
				val previousState = state.getOption().getOrElse(0.0)
				// iii. 得到最新Key的转态
				val lastestState = currentState + previousState
				// iv. 更新当前Key状态信息
				state.update(lastestState)
				// v. 返回应用数据使用
				(provinceId, lastestState)
			}
		)
		// 使用状态更细函数，更新状态（销售额）
		val provincePriceDStream: MapWithStateDStream[Int, Double, Double, (Int, Double)] = ordersDStream.mapWithState(spec)


		// TODO: 4、将每批次统计分析的结果进行输出
		provincePriceDStream.foreachRDD{ (rdd, time) =>
			// TODO: 打印每批次时间，格式： 2019/07/10 11:39:00
			val batchTime = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(new Date(time.milliseconds))
			println("-----------------------------------")
			println(s"batchTime: $batchTime")
			println("-----------------------------------")

			// 对于Streaming应用来说，输出RDD的时候，需要判断RDD是否存在
			if(!rdd.isEmpty()){
				// 针对RDD中每个分区数据操作
				rdd.foreachPartition{iter =>
					iter.foreach(println)
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
				val sparkConf = new SparkConf()
					.setMaster("local[3]") // 其中一个线程被运行Receiver接收器，剩余两个运行Task任务，并行的运行
					.setAppName("StreamingGracefullyStop")
					// TODO: 表示的是每秒钟读取每个分区的数据的条目数的最大值，此时3个分区，BatchInterval为5s，最大数据量：15万
					.set("spark.streaming.kafka.maxRatePerPartition", "10000")
					// TODO: 设置流式应用程序停止时，优雅的停止
					.set("spark.streaming.stopGracefullyOnShutdown", "true")
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
		context.start()


		// TODO: 当应用启动以后，循环判断 HDFS上目录下某个文件（监控文件）是否存在，如果存在就停止优雅停止应用
		// 每隔10s中检查应用是否停止
		val checkInterval = 10 * 1000
		// 表示应用是否停止
		var isStreamingStop = false

		// 当流式应用没有被停止的时候，检查监控文件是否存在
		while(!isStreamingStop){
			// `true` if it's stopped, 等待10s以后，应用是否停止
			isStreamingStop = context.awaitTerminationOrTimeout(checkInterval)

			// 应用未停止并且监控文件存在，停止流式应用，要停止应用创建下面文件
			// 创建命令：${HADOOOP_HOME}/bin/hdfs dfs -touchz /spark/streaming/stop-order
			if(!isStreamingStop && isExsitsMonitorFile("datas/streaming/stop-order.txt")){
				// 当应用运行完成以后，关闭资源
				context.stop(stopSparkContext = true, stopGracefully = true)
			}
		}

	}


	/**
	  * 指定HDFS路径，判断文件是否存在，存在返回true
	  */
	def isExsitsMonitorFile(checkFile: String): Boolean = {
		// i. 加载HDFS Client配置信息
		val conf = new Configuration()
		// ii. 获取路径
		val chechPath = new Path(checkFile)
		// iii. 获取HDFS文件系统
		val hdfs = chechPath.getFileSystem(conf)
		// iv. 判断文件是否存在
		hdfs.exists(chechPath)
	}

}
