package cn.itcast.spak.kafka

import scala.actors.threadpool.locks.{Lock, ReentrantLock}
import scala.actors.threadpool.{ExecutorService, Executors}

class MyThread2 extends Runnable {
  private var num: Int = 5
  private var lock: Lock = null

  def setLock(lock: Lock): Unit = {
    this.lock = lock
  }

/*  def setnum(num: Int): Unit = {
    this.num = num
  }*/
  override def run(): Unit = {
    while (true) {
      lock.lock()
      if (num > 0) {
        try
          Thread.sleep(200)
        catch {
          case e: InterruptedException =>
            e.printStackTrace()
        }
        num-= 1
        System.out.println(Thread.currentThread.getName + "卖了票，当前剩余" + num + "张")
        if (lock.tryLock() == true) lock.unlock()
        println(Thread.currentThread.getName + "释放了锁"+num)
      }
      else {
        System.out.println(Thread.currentThread.getName + "票卖光了")
        if (lock.tryLock() == true) lock.unlock()
        return //todo: break is not supported
      }
    }
  }
}

/*class MyThread2{
  private var num2: Int = 50
}*/

object MyThread3 {

  def main(args: Array[String]): Unit = {
    //创建线程池
    val threadPool: ExecutorService = Executors.newFixedThreadPool(5)
    val lock: ReentrantLock = new ReentrantLock()
    var num: Int = 50

    try {
      //提交5个线程
      for (i <- 1 to 10) {
        //threadPool.submit(new ThreadDemo("thread"+i))
        val thread = new MyThread2()
//        thread.setnum(num)
        thread.setLock(lock)
        threadPool.submit(thread)
      }
    } finally {
      threadPool.shutdown()
    }
  }

}
