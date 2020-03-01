package cn.itcast.bidata.spark.demo02

import java.sql.{Connection, DriverManager, PreparedStatement}

import org.apache.spark.broadcast.Broadcast
import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}

/**
  * 通过日志信息（运行商或者网站自己生成）和城市ip段信息来判断用户的ip段，统计热点经纬度。
  * 思路：
  * 	a. 加载IP地址信息库，获取起始IP地址Long类型值、结束IP地址Long类型值和对应的经度与维度
  * 	b. 读取日志数据，提取IP字段信息，转换为Long类型值
  * 	c. 依据提取IP地址Long类型值到IP地址信息库中查找出对用经度和维度
  * 	d. 按照经度和维度分组聚合统计，出现的次数，将结果保存到MySQL数据库表中
  */
object SparkIpLocation {

  /**
    * 将IPv4格式值转换为Long类型值
    *
    * @param ip
    *           IPv4格式的值
    * @return
    */
  def ipToLong(ip: String): Long = {
    val fragments = ip.split("[.]")
    var ipNum = 0L
    for (i <- 0 until fragments.length){
      ipNum =  fragments(i).toLong | ipNum << 8L
    }
    // 返回
    ipNum
  }


  /**
    * 通过二分查找获取IP地址对应索引（ip地址信息库数组）
    *
    * @param ipLong
    * @param ipInfo
    * @return
    */
  def binarySearch(ipLong: Long, ipInfo: Array[(Long, Long, String, String)]): Int = {
    // 定义起始和结束缩影
    var startIndex = 0
    var endIndex = ipInfo.length - 1

    // 循环查找，知道查到为止
    while (startIndex <= endIndex){
      // 利用起始和结束索引计算middleIndex
      val middleIndex = endIndex + (startIndex - endIndex) / 2
      // 依据索引获取对应的起始和结束IP地址Long类型的值
      val (startIp, endIp, _, _) = ipInfo(middleIndex)  // 拆箱
      // todo 判断IP地址范围,在最小值与最大值中间,即为找到
      if(ipLong >= startIp && ipLong <= endIp){
        return middleIndex
      }
      // 当ipLong 小于startIp时，从数组左边继续折半查找
      if(ipLong < startIp){
        endIndex = middleIndex - 1
      }
      // 当IpLong 大于 endIp时，从数组右边继续折半查找
      if(ipLong > endIp){
        startIndex = middleIndex + 1
      }
    }
    // 如果没有查找返回-1
    -1
  }

  /**
    * 将RDD中各个分区的数据保存造MySQL数据库中
    *
    * @param datas
    */
  def saveToMySQL(datas: Iterator[((String, String), Int)]): Unit = {
    // a. 加载驱动类
    Class.forName("com.mysql.jdbc.Driver")
    // 声明连接
    var conn: Connection = null
    var pstmt: PreparedStatement = null
    try{
      // b. 获取连接
      conn = DriverManager.getConnection(
        "jdbc:mysql://localhost:3306/test", "root", "root"
      )
      // 编写SQL
      val sqlStr = "INSERT INTO tb_iplocation(longitude, latitude, total_count) VALUES (?, ?, ?)"
      pstmt = conn.prepareCall(sqlStr)
      // c. 插入数据到表：采用批量插入
      datas.foreach{ case ((longitude, latitude), cnt) =>
        pstmt.setString(1, longitude)
        pstmt.setString(2, latitude)
        pstmt.setLong(3, cnt)
        // 加入批次
        pstmt.addBatch()
      }
      // 批量插入
      pstmt.executeBatch()
    }catch {
      case e: Exception => e.printStackTrace()
    }finally {
      if(null != pstmt) pstmt.close()
      if(null != conn) conn.close()
    }
  }


  def main(args: Array[String]): Unit = {

    // TODO: 1、构建SparkContext实例对象，用于读取数据和调度Job执行
    val sc: SparkContext = {
      val sparkConf = new SparkConf()
        .setMaster("local[2]")
        .setAppName("SparkPvUvTopK")
      // 调用getOrCreate函数创建SparkContext上下文实例对象，当存在的时候就获取，否则创建新的
      val context: SparkContext = SparkContext.getOrCreate(sparkConf)

      // 设置日志级别
      context.setLogLevel("WARN")
      // 返回
      context
    }


    // TODO: 2、加载IP地址信息库，获取起始IP地址Long类型值、结束IP地址Long类型值和对应的经度与维度
    val ipInfoRDD: RDD[(Long, Long, String, String)] = sc
      .textFile("datas/ips/ip.txt", minPartitions = 2)
      // 提取字段信息
      .mapPartitions{ datas =>
      // 针对分区数据操作
      datas.map{ info =>
        val arr = info.split("\\|")
        // 以四元组形式返回
        (arr(2).toLong, arr(3).toLong, arr(arr.length - 2), arr(arr.length - 1))
      }
    }
    // 将RDD数据转存到数组中
    val ipInfoArray: Array[(Long, Long, String, String)] = ipInfoRDD.collect()
    // TODO: 广播变量将IP地址信息库的数据广播到Executor中
    val ipInfoBroadcast: Broadcast[Array[(Long, Long, String, String)]] = sc.broadcast(ipInfoArray)

    // TODO: 3、读取日志数据，提取IP字段信息，转换为Long类型值
    val ipCountRDD: RDD[((String, String), Int)] = sc
      // 读取日志数据，设置分区数目
      .textFile("datas/ips/20090121000132.394251.http.format", minPartitions = 2)
      // 对RDD中每个分区数据操作
      .mapPartitions{iter =>
      iter
        .map{ log =>
          // i. 获取IP字段值
          val ipValue: String = log.split("\\|")(1)
          // ii. 将IPv4格式值装换Long类型
          val ipLong: Long = ipToLong(ipValue)
          // iii. 到IP地址信息库中查找出对应经度和维度信息
          val index = binarySearch(ipLong, ipInfoBroadcast.value)
          // 返回
          index
        }
        // 过滤索引为-1，表明未查找到经度和维度信息
        .filter(index => -1 != index)
        // 依据索引获取对应经度和维度
        .map{ index =>
        val (_, _, longitude, laitude) = ipInfoBroadcast.value(index)
        // 返回元组信息
        ((longitude, laitude), 1)
      }
    }
      // 按照经度和维度分组聚合统计
      .reduceByKey(_ + _)

     ipCountRDD.foreach(println)
    //todo 将数据保存到mysql
    ipCountRDD
      // 降低分区数
      .coalesce(1)
      // 对分区操作，将每个分区中的数据保存操MySQL表中
      .foreachPartition(saveToMySQL)


    // 为了监控4040 页面
    Thread.sleep(1000000)

    // 应用结束时，关闭SparkContext
    if(!sc.isStopped) sc.stop()
  }

}
