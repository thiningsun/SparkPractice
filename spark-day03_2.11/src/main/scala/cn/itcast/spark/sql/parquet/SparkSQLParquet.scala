package cn.itcast.spark.sql.parquet

import org.apache.spark.sql.{DataFrame, SparkSession}

/**
  * SparkSQL读取列式存储的数据
  */
object SparkSQLParquet {
  def main(args: Array[String]): Unit = {
    // 创建SparkSession实例对象，使用的建造者模式
    val spark = SparkSession.builder()
      .appName("SparkSQLParquet")
      .master("local[2]")
      .getOrCreate()
    spark.sparkContext.setLogLevel("WARN")

    // For implicit conversions like converting RDDs to DataFrames
    import spark.implicits._

    val userDF: DataFrame = spark.read.parquet("/datas/resources/users.parquet")
    userDF.printSchema()
    userDF.show(10,truncate = false)

    println("========================================")
    val df = spark.read.load("/datas/resources/users.parquet")
    df.printSchema()
    df.show(10, truncate = false)

    df.count()


    Thread.sleep(10000000)

    // 应用程序结束，关闭
    spark.stop()

  }
}
