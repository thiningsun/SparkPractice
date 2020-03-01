package cn.itcast.bigdata.spark.sort

import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}

import scala.collection.mutable.ListBuffer

/**
  * 针对数据进行分组、排序、TopKey操作，主要使用groupByKey和aggregateByKey函数
  */
object SparkGroupSortTopKey {

	def main(args: Array[String]): Unit = {

		// TODO: 1、构建SparkContext实例对象，用于读取数据和调度Job执行
		val sc: SparkContext = {
			val sparkConf = new SparkConf()
				.setMaster("local[2]")
				.setAppName("SparkGroupSortTopKey")
			// 调用getOrCreate函数创建SparkContext上下文实例对象，当存在的时候就获取，否则创建新的
			val context: SparkContext = SparkContext.getOrCreate(sparkConf)

			// 设置日志级别
			context.setLogLevel("WARN")
			// 返回
			context
		}

		// 加载数据
		val datasRDD: RDD[(String, Int)] = sc
			.textFile("G:\\Apeixun\\A资料文件\\Spark\\spark_day02_core\\07_数据\\exampleDatas\\group\\group.data", minPartitions = 2)
	    	.mapPartitions{ iter =>
				iter.map{ data =>
					val arr = data.split("\\s")
					// 返回二元组形式
					(arr(0), arr(1).toInt)
				}
			}


		// 方法一：使用groupByKey函数完成功能
		val groupTop3: RDD[(String, List[Int])] = datasRDD
			// 按照Key进行分组
	    	.groupByKey()  // RDD[(String, Iterable[Int])]
			// 对每个分组中Values进行降序排序
	    	.mapPartitions{ iter =>
				// val xx: Iterator[(String, Iterable[Int])] = iter
				iter.map{ case (key: String, iter: Iterable[Int]) =>
					// 对每个分组中values降序排序
					val topList = iter.toList.sortBy(x => - x).take(3)
					// 返回
					(key, topList)
				}
			}
		groupTop3.foreachPartition(_.foreach(println))

		println("===========================================================")

		/*
		def aggregateByKey[U: ClassTag]
			// 表示的是聚合时中间临时变量的初始值
			(zeroValue: U)  -> 列表ListBuffer,可变列表
			(
				seqOp: (U, V) => U,
				combOp: (U, U) => U
			): RDD[(K, U)]
		 */
		datasRDD
	    	.aggregateByKey(new ListBuffer[Int]())(
				// 各个分区聚合函数seqOp: (U, V) => U
				(u: ListBuffer[Int], v: Int) => {
					// 将每个元素加入到ListBuffer中
					u += v
					// 降序排序，获取最大的三个
					u.sortBy(x => - x).take(3)
				},
				// 对各个分区聚合结果的聚合函数
				(u1: ListBuffer[Int], u2: ListBuffer[Int]) => {
					// 合并两个ListBuffer
					u1 ++= u2
					// 降序排序，获取最大的三个
					u1.sortBy(x => - x).take(3)
				}
			)
			.foreachPartition(_.foreach(println))


		// 为了监控4040 页面
		Thread.sleep(1000000)

		// 应用结束时，关闭SparkContext
		if(!sc.isStopped) sc.stop()
	}

}
