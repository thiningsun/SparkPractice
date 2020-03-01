package cn.itcast.spark.sql.hive

import org.apache.spark.sql.{DataFrame, SparkSession}

object SparkSQLHive {
  def main(args: Array[String]): Unit = {
    // TODO: 1、构架SparkSession实例对象，采用建造者模式build Parttern
    val spark: SparkSession = SparkSession.builder()
      .appName("SparkSQLRDDToDataset")
      .master("local[2]")
      .config("spark.sql.shuffle.partitions", "4")
      // 表示HIVE表中数据存储在HDFS上仓库目录
      .config("spark.sql.warehouse.dir", "/user/hive/warehouse")
      // TODO: 告知与Hive集成
      .enableHiveSupport()
      .getOrCreate()
    spark.sparkContext.setLogLevel("WARN")
    // TODO: 隐式转换（隐式转换函数）
    import spark.implicits._
    import org.apache.spark.sql.functions._


    // 读取HIVE中的数据，emp表
    val empDF: DataFrame = spark.table("db_hive.emp")

    // 获取各个部门的平均工资
    val salAvgDF = empDF
      .groupBy($"deptno")
      .agg(round(avg($"sal"), 2).as("avg_sal"))
      .orderBy($"deptno")

    salAvgDF.show(10, truncate = false)

    Thread.sleep(10000000)

    // 应用结束的时候，关闭资源
    spark.stop()
  }
}
