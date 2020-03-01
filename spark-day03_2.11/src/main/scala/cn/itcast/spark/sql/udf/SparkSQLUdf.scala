package cn.itcast.spark.sql.udf

import org.apache.spark.sql.SparkSession

object SparkSQLUdf {
  def main(args: Array[String]): Unit = {
    val spark: SparkSession = SparkSession.builder()
      .appName("SparkSQLUdf")
      .master("local[2]")
      .config("spark.sql.shuffle.pratitions","4")
      .config("spark.sql.warehourse.dir","/user/hive/warehouse")
      // TODO: 告知与Hive集成
      .enableHiveSupport()
      .getOrCreate()
    spark.sparkContext.setLogLevel("WARN")
    // TODO: 隐式转换（隐式转换函数）
    import org.apache.spark.sql.functions._
    import spark.implicits._

    // TODO: 将某列值的字母转换为小写字母
    //
    val lowerFunc=(word:String) =>{
      word.toLowerCase
    }

    spark.udf.register(
      "to_lower",
      lowerFunc
    )

    // 读取Hive中emp表的ename值
    spark.sql(
      """
        			  |SELECT dname, to_lower_case(dname) AS name FROM db_hive.emp
      			""".stripMargin)
      .show(20, truncate = false)

    println("====================================================")



    Thread.sleep(10000000)

    // 应用结束的时候，关闭资源
    spark.stop()
  }

}
