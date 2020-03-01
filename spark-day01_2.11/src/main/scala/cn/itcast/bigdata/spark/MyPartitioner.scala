package cn.itcast.bigdata.spark

import org.apache.spark.Partitioner

class MyPartitioner(val num:Int) extends Partitioner {
  override def numPartitions: Int = num

  override def getPartition(key: Any): Int = {
    val len: Int = key.toString.length
    //根据单词长度对分区个数取模
    len%num
  }

}
