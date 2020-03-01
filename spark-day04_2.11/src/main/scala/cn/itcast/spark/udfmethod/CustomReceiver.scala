package cn.itcast.spark.udfmethod

import org.apache.spark.storage.StorageLevel
import org.apache.spark.streaming.receiver.Receiver

import scala.tools.nsc.io.Socket
/**
  * 自定义Receiver从TCP Socket接收数据，需要两个参数
  * 		hostname:port
  */
class CustomReceive(val host: String, val port: Int)
  extends Receiver[String](storageLevel =StorageLevel.MEMORY_AND_DISK){

  // TODO: 表示启动Streaming应用的时候，如何接收数据
  override def onStart(): Unit = {

    // 启动一个线程一直接收数据
    new Thread(
      new Runnable {
        override def run(): Unit = {
          // 接收数据封装到一个函数
          receive()
        }
      }
    ).start()

  }

  def receive(): Unit = {
    var socket: Socket = null
    var userInput: String = null

  }


  override def onStop(): Unit = {
  }
}
