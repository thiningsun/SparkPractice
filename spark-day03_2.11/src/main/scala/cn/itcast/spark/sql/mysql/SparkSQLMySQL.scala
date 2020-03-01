package cn.itcast.spark.sql.mysql
import java.util.Properties

import org.apache.spark.TaskContext
import org.apache.spark.sql.{DataFrame, SparkSession}

object SparkSQLMySQL {
  def main(args: Array[String]): Unit = {
    // TODO: 1、构架SparkSession实例对象，采用建造者模式build Parttern
    val spark: SparkSession = SparkSession.builder()
      .appName("SparkSQLRDDToDataset")
      .master("local[2]")
      .config("spark.sql.shuffle.partitions", "4")
      .getOrCreate()
    spark.sparkContext.setLogLevel("WARN")
    // TODO: 隐式转换（隐式转换函数）
    import spark.implicits._

    val url = "jdbc:mysql://node01:3306/userdb"

    val tableName = "emp"
    val prpo: Properties = new Properties()
    prpo.put("user","root")
    prpo.put("password","root")
    // TODO: 方式一：单分区读取MySQL表中的数据

    /**
      * url: String,
      table: String,
      columnName: String,
      lowerBound: Long,
      upperBound: Long,
      numPartitions: Int,
      */
    val soDF: DataFrame = spark.read.jdbc(url,tableName,prpo)
    // 查看各个分区的数据量
    soDF.foreachPartition(iter =>
      println(s"p-${TaskContext.getPartitionId()}, count = ${iter.size}")
    )

    println("===========================================================")

    // TODO: 方式二：指定分区数目，按照指定列名分区，读取MySQL表中的数据
    val soDF2= spark.read.jdbc(
      url, tableName,  //
      "id", // 按照订单金额作为分区列
      1201, // 订单销售额大于等于20
      1205, // 订单金额小于200
      4, // 设置分区数，每个分区：20-65, 65-110, 110-155, 155-200
      prpo
    )
    soDF2.foreachPartition{ iter =>
      println(s"p-${TaskContext.getPartitionId()}, count = ${iter.size}")
    }




    Thread.sleep(10000000)

    // 应用结束的时候，关闭资源
    spark.stop()
  }
}
