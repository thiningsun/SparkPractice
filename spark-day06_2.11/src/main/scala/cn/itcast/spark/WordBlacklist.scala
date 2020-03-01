package cn.itcast.spark

import org.apache.spark.broadcast.Broadcast

import scala.reflect.ClassTag

object WordBlacklist {

  @volatile private var instance:Broadcast[Seq[String]] =null

  def mkarry[T:ClassTag](x:Int): Unit ={
    println("====="+x)
  }

  def main(args: Array[String]): Unit = {
    mkarry[String](5)

    var seq:Seq[String]=Seq("aa","bb","cc","dd")
    var set:Set[String]=Set("aaa","bbb","ccc","ddd","eeeeee")

    println(seq)
    println(set)
  }
}
