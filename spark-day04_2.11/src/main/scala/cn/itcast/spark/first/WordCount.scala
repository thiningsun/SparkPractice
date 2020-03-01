package cn.itcast.spark.first

import org.apache.spark.sql.{DataFrame, Dataset, SparkSession}

object WordCount {
  def main(args: Array[String]): Unit = {
    val spark=SparkSession.builder()
        .master("local[2]")
      .appName("WordCount")
      .getOrCreate()

    spark.sparkContext.setLogLevel("WARN")
    import spark.implicits._

    val lines: DataFrame = spark.readStream
      .format("socket")
      .option("host", "node03")
      .option("port", 9999)
      .load()
    val words: Dataset[String] = lines.as[String]
      .flatMap(_.split(" "))
      .filter(lines => lines.trim.length>0)

    val wordCounts: DataFrame = words.groupBy("value").count()

    val query = wordCounts.writeStream
      .outputMode("complete")
      .format("console")
      .start()

    query.awaitTermination()
  }

}
