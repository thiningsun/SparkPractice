package cn.itcast.bigdata.spark.first

import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}

import scala.collection.mutable.ListBuffer

object InputFormat {
  def main(args: Array[String]): Unit = {
    val sparkConf = new SparkConf().setMaster("local[2]").setAppName("InputFormat")

    // 建议使用getOrCreate获取SparkContext实例
    val sc: SparkContext = SparkContext.getOrCreate(sparkConf)
    sc.setLogLevel("WARN")

    // 加载数据
    val datasRDD: RDD[(String, Int)] = sc
      .textFile("datas/group/group.data", minPartitions = 2)
      .mapPartitions{ iter =>
//        val  xx: Iterator[String] =iter
        iter.map{ data =>
          val arr = data.split("\\s")
          // 返回二元组形式
          (arr(0), arr(1).toInt)
        }
      }
    // 方法一：使用groupByKey 分组函数完成功能

    val groupTop3 = datasRDD
      .groupByKey()
      // 对每个分组中Values进行降序排序
      .mapPartitions {
      iter =>

        iter.map {
          case (key: String, iter: Iterable[Int]) =>
            val topList = iter.toList.sortBy(x => -x).take(3)
            (key, topList)
        }
    }
    groupTop3.foreachPartition(_.foreach(println))


    println("===========================================================")

    datasRDD
      .aggregateByKey(new ListBuffer[Int]())(
        (u:ListBuffer[Int],v:Int) =>{
          u +=v
          u.sortBy(x => -x)
            .take(3)
        },(u1:ListBuffer[Int],u2:ListBuffer[Int]) =>{
          u1 ++=u2
          u1.sortBy(x => -x).take(3)
        }
      )
      .foreachPartition(_.foreach(println))

    Thread.sleep(1000000)

    // 应用结束时，关闭SparkContext
    if(!sc.isStopped) sc.stop()
  }

}
