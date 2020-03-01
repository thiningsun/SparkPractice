package cn.itcast.spark.structed

import java.sql.{Connection, DriverManager, PreparedStatement}

import org.apache.spark.sql.streaming.{OutputMode, StreamingQuery}
import org.apache.spark.sql.{DataFrame, ForeachWriter, Row, SparkSession}

/**
  * 结构化流式处理模块，从Kafka 读取数据（仅支持Kafka New Consumer API），直接获取Topic中的数据
  * 		实时统计各省份销售订单额
  */
object StructuredToMysql {
  def main(args: Array[String]): Unit = {
    // TODO: 1、构建SparkSession实例对象，读取数据源的数据，封装在DataFrame/Dataset数据结构中
    val spark: SparkSession = SparkSession.builder()
      .master("local[3]")
      .appName("StructuredToMysql")
      .config("spark.sql.shuffle.partitions",3)
      .getOrCreate()

    spark.sparkContext.setLogLevel("WARN")

    // 隐式导入，主要用于RDD转换DataFrame
    import spark.implicits._
    //导入SparkSQL的函数库
    import org.apache.spark.sql.functions._

    // TODO: 2、从Kafka Topic中实时获取数据
    val kafkaStreamDF: DataFrame =spark.readStream
      .format("kafka")
      .option("kafka.bootstrap.servers", "node01:9092,node02:9092,node03:9092")
      .option("subscribe","order")
      .load()

   val provinceAmtStreamDF: DataFrame =kafkaStreamDF
//        .selectExpr("CAST(value AS STRING")
        .selectExpr("CAST(value AS STRING)")
        .as[String]
        .filter($"value".isNotNull and(length(trim($"value"))>0))
      //数据格式 orderId,proviceId,orderPrice
        .mapPartitions{ orders =>
            orders
              .filter(order => order.trim.split(",").length == 3)
              .map{order =>
              val Array(_,provinceId,orderAmt) = order.trim.split(",")
              (provinceId.toInt,orderAmt.toDouble)
         }
    }
      .withColumnRenamed("_1","provinceId").withColumnRenamed("_2","orderAmt")
    // 按照省份ID进行累加订单金额
      .groupBy($"provinceId").agg(sum($"orderAmt").as("amtTotal"))

    // TODO: 将结果打印控制台
    val quary: StreamingQuery =provinceAmtStreamDF.writeStream
      .outputMode(OutputMode.Update())
      .foreach( new ForeachWriter[Row]{

        var conn: Connection = _
        var pstmt: PreparedStatement = _
        // 打开连接
        override def open(partitionId: Long, version: Long): Boolean = {
          //a.加载驱动类
          Class.forName("com.mysql.jdbc.Driver")
          //b.获取连接
          conn=DriverManager.getConnection(
            "jdbc:mysql://node01:3306/test","root","root"
          )
          // 编写SQL
          val sqlStr="INSERT INTO result_province_amt(province_id, order_amt) VALUES (?, ?) ON DUPLICATE KEY UPDATE order_amt = VALUES(order_amt)"
          pstmt=conn.prepareCall(sqlStr)
          true
        }

        // 如何输出结果
        override def process(row: Row): Unit = {
          // 从Row中获取各个字段的值
          val provinceId = row.getInt(0)
          val amtTotal = row.getDouble(1)

          pstmt.setInt(1, provinceId)
          pstmt.setDouble(2, amtTotal)

          // 加入批次
          pstmt.addBatch()
        }

        override def close(errorOrNull: Throwable): Unit = {
          // 批量插入
          pstmt.executeBatch()
          // 关闭连接
          if(null != pstmt) pstmt.close()
          if(null != conn) conn.close()
        }
      })
      .start()

    quary.awaitTermination()

  }
}
