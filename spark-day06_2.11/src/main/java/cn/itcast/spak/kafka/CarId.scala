package cn.itcast.spak.kafka

trait CarId {
  var id: Int

  def currentId(): Int //定义了一个抽象方法
  def test(msg: String): Unit = {
    println(msg)
  }
}

trait Car {
  var id: Int

  def driver(): Int
}

class BYDCarId extends CarId with Car {
  override def driver(): Int = {
    30
  }

  //使用extends关键字
  override var id = 10000 //BYD汽车编号从10000开始
  def currentId(): Int = {
    id += 1; id
  } //返回汽车编号
  override def test(msg: String): Unit = super.test(msg)
}

class BMWCarId extends CarId { //使用extends关键字
  override var id = 20000 //BMW汽车编号从10000开始
def currentId(): Int = {
  id += 1; id
} //返回汽车编号
}

object TraitLearn {
  def main(args: Array[String]): Unit = {
    val myCarId1 = new BYDCarId()
    val myCarId2 = new BMWCarId()
    myCarId1.test("zhangsan")
    printf("My first CarId is %d.\n", myCarId1.currentId)
    myCarId1.test("123")
    printf("My second CarId is %d.\n", myCarId2.currentId)
    myCarId2.test("abcd")

    val i: Int = myCarId1.driver()
    println(i)
  }
}
