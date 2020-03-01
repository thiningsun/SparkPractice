package cn.itcast.spak.kafka

import org.apache.spark.sql.{DataFrame, SparkSession}


/**
  * SparkSQL从Hive表读取数据，在IDAE中开发测试
  */
object SparkSQLGroupFuns {
  def main(args: Array[String]): Unit = {
    // TODO: 1、构架SparkSession实例对象，采用建造者模式build Parttern
    val spark: SparkSession = SparkSession.builder()
      .appName(this.getClass.getSimpleName.stripSuffix("$"))
      .master("local[4]")
      .config("spark.sql.shuffle.partitions", "4")
      .getOrCreate()
    spark.sparkContext.setLogLevel("WARN")

    // TODO: 隐式转换（隐式转换函数）
    import org.apache.spark.sql.functions._
    import spark.implicits._
    val orders: Seq[MemberOrderInfo] = Seq(
      MemberOrderInfo("深圳","钻石会员","钻石会员1个月",25),
      MemberOrderInfo("深圳","钻石会员","钻石会员1个月",25),
      MemberOrderInfo("深圳","钻石会员","钻石会员3个月",70),
      MemberOrderInfo("深圳","钻石会员","钻石会员12个月",300),
      MemberOrderInfo("深圳","铂金会员","铂金会员3个月",60),
      MemberOrderInfo("深圳","铂金会员","铂金会员3个月",60),
      MemberOrderInfo("深圳","铂金会员","铂金会员6个月",120),
      MemberOrderInfo("深圳","黄金会员","黄金会员1个月",15),
      MemberOrderInfo("深圳","黄金会员","黄金会员1个月",15),
      MemberOrderInfo("深圳","黄金会员","黄金会员3个月",45),
      MemberOrderInfo("深圳","黄金会员","黄金会员12个月",180),
      MemberOrderInfo("北京","钻石会员","钻石会员1个月",25),
      MemberOrderInfo("北京","钻石会员","钻石会员1个月",30),
      MemberOrderInfo("北京","铂金会员","铂金会员3个月",60),
      MemberOrderInfo("北京","黄金会员","黄金会员3个月",45),
      MemberOrderInfo("上海","钻石会员","钻石会员1个月",25),
      MemberOrderInfo("上海","钻石会员","钻石会员1个月",25),
      MemberOrderInfo("上海","铂金会员","铂金会员3个月",60),
      MemberOrderInfo("上海","黄金会员","黄金会员3个月",45)
    )

    val ordersDF: DataFrame = spark.createDataFrame(orders).toDF()
    // 使用SQL分析|也可以DSL分析
    // a. 注册为临时视图
    ordersDF.createOrReplaceTempView("view_tmp_orders")
    // b.1 使用group by 对area, memberType, product统计情况


    spark.udf.register("tmp",(price:Int)=>price*10)
    spark.sql(
      """
        |select area,memberType,product,tmp(price),count(1) AS cnt  from view_tmp_orders
        |group by area,memberType,product,price
        |order by area,memberType,product,price
      """.stripMargin).show(10,truncate = false)

    //Thread.sleep(10000000)
    // 应用结束的时候，关闭资源
    spark.stop()
  }

}
case class MemberOrderInfo(area:String, memberType:String, product:String, price:Int)
