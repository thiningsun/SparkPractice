package cn.itcast.spark.sql.json

import org.apache.spark.sql.{DataFrame, Dataset, SparkSession}

object SparkSQLJson {
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

    // TODO: 2、读取本地文件系统上JSON格式数据
    val githubDF: DataFrame = spark.read.json("datas/json/2015-03-01-4.json.gz")
    githubDF.printSchema()
    githubDF.show(2,truncate = false)

    println("=======================================")

    // 只有一个字段，名称为value,类型为字符串
    val gitDF: Dataset[String] = spark.read.textFile("datas/json/2015-03-01-4.json.gz")
    import org.apache.spark.sql.functions._
    gitDF
      // def get_json_object(e: Column, path: String): Column
      .select(
          get_json_object($"value", "$.id").as("id"),
          get_json_object($"value", "$.type").as("type"),
          get_json_object($"value", "$.public").as("public"),
          get_json_object($"value", "$.created_at").as("created_at")
      )
      .show(10,truncate = false)


    Thread.sleep(10000000)

    // 应用结束的时候，关闭资源
    spark.stop()
  }
}
