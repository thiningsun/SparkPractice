package cn.itcast.spark.core

import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.hbase.{Cell, CellUtil, HBaseConfiguration, NoTagsKeyValue}
import org.apache.hadoop.hbase.client.Result
import org.apache.hadoop.hbase.io.ImmutableBytesWritable
import org.apache.hadoop.hbase.mapreduce.TableInputFormat
import org.apache.hadoop.hbase.util.Bytes
import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}

/**
  * SparkCore从HBase表中读取数据
  */
object SparkReadHBase {
	def main(args: Array[String]): Unit = {
		// TODO: 1、SparkContext实例对象，读取数据和调度Job执行
		val sc: SparkContext = SparkContext.getOrCreate(
			new SparkConf()
				.setMaster("local[4]").setAppName("SparkReadHBase")
				// TODO: 设置Spark Application序列化为Kryo方式, 默认对基本数据使用Kryo序列化，其他类需要注册
				.set("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
				// TODO: 表示如果有数据类型没有被注册使用Kryo序列化，程序将报错
		    .set("spark.kryo.registrationRequired", "true")
		    	// 注册使用Kryo序列化的类型（看RDD中数据类型如果不是基本数据类型，就需要注册）
		    .registerKryoClasses(
					Array(
						classOf[ImmutableBytesWritable], classOf[Result], classOf[Array[Cell]]
					)
				)
		)
		// TODO: 2、从HBase表中读取数据
		/*
		  def newAPIHadoopRDD[K, V, F <: NewInputFormat[K, V]](
			  conf: Configuration = hadoopConfiguration,
			  fClass: Class[F],
			  kClass: Class[K],
			  vClass: Class[V]
		  ): RDD[(K, V)]
		 */
		// 从HBase表中读取数据的话，HBaseConfiguration创建Configuration对象
		val conf: Configuration = HBaseConfiguration.create()
		// 设置从哪张表读取数据
		conf.set(TableInputFormat.INPUT_TABLE, "ns1:sale_orders")
		// 调用newAPIHadoopRDD读取表的数据
		// TODO: SparkCore从HBase表中读取数据，RDD的分区数目就等于Table的Region数目
		val hbaseRDD: RDD[(ImmutableBytesWritable, Result)] = sc.newAPIHadoopRDD(
			conf, //
			classOf[TableInputFormat], //
			classOf[ImmutableBytesWritable], //
			classOf[Result]
		)

		println(s"Count = ${hbaseRDD.count()}")

		// 获取RDD中前5条数据，进行打印
		hbaseRDD.take(5).foreach{ case (rowKey, result) =>
			println(s"RowKey = ${Bytes.toString(rowKey.get())}")
			// 循环遍历, 获取每一列的数据
			for(cell <- result.rawCells()){
				// 获取列簇CF
				val cf = Bytes.toString(CellUtil.cloneFamily(cell))
				// 获取列名称
				val column = Bytes.toString(CellUtil.cloneQualifier(cell))
				// 获取值
				val value = Bytes.toString(CellUtil.cloneValue(cell))
				// 获取版本号，默认使用timestamp，插入表的时间戳，long类型数据
				val version = cell.getTimestamp
				println(s"\t $cf:$column = $value -> version: $version")
			}
		}

		Thread.sleep(10000000)
		// 应用运行完成进行关闭
		sc.stop()
	}

}
