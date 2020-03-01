package cn.itcast.spark.sql

import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.hbase.HBaseConfiguration
import org.apache.hadoop.hbase.client.Put
import org.apache.hadoop.hbase.io.ImmutableBytesWritable
import org.apache.hadoop.hbase.mapreduce.TableOutputFormat
import org.apache.hadoop.hbase.util.Bytes
import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}

/**
  * SparkCore从HBase表中读取数据
  */
object SparkWriteHBase {

	def main(args: Array[String]): Unit = {

		// TODO: 1、SparkContext实例对象，读取数据和调度Job执行
		val sc: SparkContext = SparkContext.getOrCreate(
			new SparkConf()
				.setMaster("local[4]").setAppName("SparkWriteHBase")
				// TODO: 设置Spark Application序列化为Kryo方式, 默认对基本数据使用Kryo序列化，其他类需要注册
				.set("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
				// TODO: 表示如果有数据类型没有被注册使用Kryo序列化，程序将报错
		    	//.set("spark.kryo.registrationRequired", "true")
		)


		// 模拟数据
		val list: List[(String, Int)] = List(("spark", 9999), ("hadoop", 234), ("flink", 450), ("hbase", 342))
		// 通过并行化的方式创建RDD
		val wordCountsRDD: RDD[(String, Int)] = sc.parallelize(list, numSlices = 2)

		/**
		  * 设计HBase的表：
		  * 	表的名称：ns1:htb_wordcount
		  * 	列簇：info
		  * 	RowKey: word
		  * 		唯一性，热点性，前缀匹配查询
		  *     列名：info:count
		  *
		  *  TODO: 将RDD数据保存到HBase表中，要求RDD中数据类型为二元组，其中Key：ImmutableBytesWritable， Value：Put
		  */
		val putsRDD: RDD[(ImmutableBytesWritable, Put)] = wordCountsRDD.mapPartitions{ iter =>
			iter.map{ case (word, count) =>
				// i. 创建RowKey
				val rowKey: Array[Byte] = Bytes.toBytes(word)

				// ii. 构建Put对象
				val put = new Put(rowKey)
				// 添加列
				put.addColumn(
					Bytes.toBytes("info"), Bytes.toBytes("count"), Bytes.toBytes(count.toString)
				)

				// iii. 返回二元组对象
				(new ImmutableBytesWritable(rowKey), put)
			}

		}


		// 调用函数，将数据保存到HBase表中
		val conf: Configuration = HBaseConfiguration.create()
		// TODO: 设置将数据保存到HBase那张表中
		conf.set(TableOutputFormat.OUTPUT_TABLE, "ns1:htb_wordcount")
		/*
			def saveAsNewAPIHadoopFile(
			  path: String,
			  keyClass: Class[_],
			  valueClass: Class[_],
			  outputFormatClass: Class[_ <: NewOutputFormat[_, _]],
		  	  conf: Configuration = self.context.hadoopConfiguration
		  	 ): Unit
		 */
		putsRDD.saveAsNewAPIHadoopFile(
			"datas/spark/hbase/wordcount-" + System.currentTimeMillis(),
			classOf[ImmutableBytesWritable], //
			classOf[Put], //
			classOf[TableOutputFormat[ImmutableBytesWritable]], //
			conf //
		)


		Thread.sleep(10000000)
		// 应用运行完成进行关闭
		sc.stop()
	}

}
