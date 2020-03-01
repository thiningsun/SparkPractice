package cn.itcast.bigdata.spark.demo01

import org.apache.spark.rdd.RDD
import org.apache.spark.storage.StorageLevel
import org.apache.spark.{SparkConf, SparkContext}

object SparkPvUvTopK {
  def main(args: Array[String]): Unit = {

    //todo 第一步,创建实例用于读取数据
    val sparkConf: SparkConf = new SparkConf()
      .setMaster("local[2]")
      .setAppName("SparkPvUvTopK")
    val sc: SparkContext = SparkContext.getOrCreate(sparkConf)
    sc.setLogLevel("WARN")

    // TODO: 2、读取点击流日志数据(本地文件系统读取）
    val logsRDD: RDD[String] = sc.textFile("datas/logs/access.log",minPartitions = 2)

    // 提取字段信息
    val filterRDD = logsRDD.mapPartitions(iter => iter
      .filter(log => null != log && log.trim.split("\\s+").length >= 11)
      .map { log =>
        val arr = log.trim.split("\\s+")
        (arr(0), arr(6), arr(10))
      }
    )
    // 由于后续需求使用此RDD多次，进行缓存操作
    filterRDD.persist(StorageLevel.MEMORY_AND_DISK)

    //PVC统计
    val totolPV = filterRDD
      .filter(tuple => null != tuple._2 && tuple._2.trim.length > 0)
      .count()
    println(s"统计pv为: $totolPV")

    // TODO: uv统计
    val totolUv: Long = filterRDD
      .mapPartitions(iter => iter.map(item => item._1))
      .distinct()
      .count()
    println(s"统计的UV 为: $totolUv")

    // TODO: Refer 统计，获取TopKey （Key = 5）
    val top5ReferUrl: Array[(String, Int)] = filterRDD.mapPartitions(
      iter => iter.map(
        iter => (iter._3, 1)))
      .reduceByKey((a, b) => a + b)
      .sortBy(tuple => tuple._2, false)
      .take(6)

    top5ReferUrl.foreach(x => println(x))

    //释放资源
    filterRDD.unpersist()
    Thread.sleep(1000000)

    if(!sc.isStopped) {
      sc.stop()
    }





  }

}
