package cn.itcast.spark.sql.day04

import org.apache.spark.SparkConf

object StreamingWordCount {
  def main(args: Array[String]): Unit = {
    // TODO: 1、构建Streaming应用上下文对象

    val sparkConf: SparkConf = new SparkConf()
      .setMaster("local[3]")
      .setAppName("StreamingWordCount")

  }
}
