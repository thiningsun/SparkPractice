package cn.itcast.spark.sql.csv

import org.apache.spark.sql.types._
import org.apache.spark.sql.{DataFrame, SparkSession}

object SparkSQLCsv {
  def main(args: Array[String]): Unit = {
    // 创建SparkSession实例对象，使用的建造者模式
    val spark = SparkSession.builder()
      .appName("SparkSQLCsv")
      .master("local[2]")
      .getOrCreate()
    spark.sparkContext.setLogLevel("WARN")

    // For implicit conversions like converting RDDs to DataFrames
    import spark.implicits._

    // TODO: 读取csv格式的数据，各个字段之间使用 逗号隔开
    val mlDF: DataFrame = spark.read
      .option("header", "true")
      .option("sep", "\\t")
      .option("inferSchema", "true")
      .csv("datas/ml-100k/u.dat")

    mlDF.printSchema()
    mlDF.show(10, truncate = false)


    /**
      * StructType(
      * StructField("f1", IntegerType, true) ::
      * StructField("f2", LongType, false) ::
      * StructField("f3", BooleanType, false) :: Nil)
      */
    println("========================================")

    val ratingSchema: StructType = StructType(
      StructField("userId", StringType, nullable = true) ::
        StructField("movieId", StringType, nullable = true) ::
        StructField("rating", DoubleType, nullable = true) ::
        StructField("timestamp", LongType, nullable = true) :: Nil
    )

    val ratingsDF=spark.read
      .schema(ratingSchema)
      .option("sep","\\t")
      .csv("datas/ml-100k/u.data")

    ratingsDF.printSchema()
    ratingsDF.show(10, truncate = false)


    Thread.sleep(10000000)

    // 应用程序结束，关闭
    spark.stop()
  }
}
