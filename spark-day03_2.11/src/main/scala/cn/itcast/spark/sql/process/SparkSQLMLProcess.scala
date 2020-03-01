package cn.itcast.spark.sql.process

import org.apache.spark.sql.{DataFrame, Dataset, SparkSession}

/**
  * 针对电影评分数据进行处理分析
  */
object SparkSQLMLProcess {
  def main(args: Array[String]): Unit = {

    //创建实例对象,使用建造者模式
    val spark: SparkSession = SparkSession.builder()
      .appName("SparkSQLMLProcess")
      .master("local[2]")
      .getOrCreate()
    spark.sparkContext.setLogLevel("WARN")
    // For implicit conversions like converting RDDs to DataFrames
    import spark.implicits._

    // TODO： 1、读取电影评分数据
    val rawRatingsDS: Dataset[String] = spark.read.textFile("datas/ml-1m/ratings.dat")

    rawRatingsDS.printSchema()
    rawRatingsDS.show(10,truncate = false)

    println("=======================================")
    // 对Dataset进行处理，使用mapPartitions对每个分区操作

    val ratingsDS = rawRatingsDS.mapPartitions{ iter =>
      iter.map { data =>
        val Array(userId, movieId, rating, timestamp) = data.split("::")
        MLRating(userId, movieId, rating.toLong, timestamp.toLong)
      }
    }
    ratingsDS.printSchema()
    ratingsDS.show(10, truncate = false)

    /**
      *  需求：统计各个电影平均评分，要求每个电影至少被评分1000次以上，获取电影评分最高的Top10电影信息
         电影平均评分及电影评分次数，先按照电影评分次数降序，再按照电影平均评分降序
      */
    // 首先使用DSL编程分析
    /*

     */
    // TODO: 导入SparkSQL自带函数库，
    import   org.apache.spark.sql.functions._

    ratingsDS
      .select($"movieId",$"rating")// 提取要分析的字段
      .groupBy($"movieId")
      .agg(count($"movieId").as("rating_cnt"),round(avg($"rating"),2).as("rating_avg"))
      .filter($"rating_cnt" >1000)
      .orderBy($"rating_cnt".desc,$"rating_avg".desc)
      .limit(10)
      .show(10,truncate = false)

    println("=======================================")

    // TODO: 使用SQL分析
    // a. 将Dataset/DataFrame 注册为临时驶入
    ratingsDS.createOrReplaceTempView("view_tmp_ratings")

    // b. 编写SQL，使用sparkSession#.sql执行
    spark.sql(
      """
        			  |SELECT
        			  |  movieId, COUNT(movieId) AS rating_cnt, ROUND(AVG(rating), 2) AS rating_avg
        			  |FROM
        			  |  view_tmp_ratings
        			  |GROUP BY
        			  |  movieId
        			  |HAVING
        			  |  rating_cnt > 1000
        			  |ORDER BY
        			  |  rating_cnt DESC, rating_avg DESC
        			  |LIMIT
        			  |  10
      			""".stripMargin)
      .show(10, truncate = 10)


Thread.sleep(10000000)

// 应用程序结束，关闭
spark.stop()
}
}
