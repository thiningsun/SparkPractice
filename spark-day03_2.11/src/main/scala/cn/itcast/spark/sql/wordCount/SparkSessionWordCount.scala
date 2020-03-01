package cn.itcast.spark.sql.wordCount

import org.apache.spark.sql.{DataFrame, Dataset, SparkSession}

object SparkSessionWordCount {
  def main(args: Array[String]): Unit = {
    val spark = SparkSession.builder()
      .appName("SparkSessionWordCount")
      .master("local[4]")
      .config("spark.eventLog.enabled","true")
      .config("spark.eventLog.dir","hdfs://node01:9000/spark/eventLogs/")
      .config("spark.eventLog.compress","true")
      .getOrCreate()
    spark.sparkContext.setLogLevel("WARN")

    //todo For implicit conversions like converting RDDs to DataFrames
    import  spark.implicits._
    // TODO: 使用SparkSession读取数据
    val inputRDD: Dataset[String] = spark.read.textFile("datas/wordcount/wordcount.txt")
    inputRDD.printSchema()
    inputRDD.show(10)


    val wordcountDF: DataFrame = inputRDD
      .flatMap(line => line.split("\\s+").filter(word => null != word && word.length > 0))
      .groupBy("value").count()

    wordcountDF.printSchema()
    wordcountDF.show()


    Thread.sleep(10000000)

    // 应用程序结束，关闭
    spark.stop()
  }
}
