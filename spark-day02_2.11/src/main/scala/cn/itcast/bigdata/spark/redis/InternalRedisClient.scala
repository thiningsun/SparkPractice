package cn.itcast.bigdata.spark.redis

import org.apache.commons.pool2.impl.GenericObjectPoolConfig
import redis.clients.jedis.JedisPool

object InternalRedisClient extends Serializable {

  @transient private var pool: JedisPool = null

  def makePool(redisHost: String, redisPort: Int, redisTimeout: Int,
               maxTotal: Int, maxIdle: Int, minIdle: Int): Unit = {
    makePool(redisHost, redisPort, redisTimeout, maxTotal, maxIdle, minIdle, true, false, 10000)
  }

  def makePool(redisHost: String, redisPort: Int, redisTimeout: Int,
               maxTotal: Int, maxIdle: Int, minIdle: Int, testOnBorrow: Boolean,
               testOnReturn: Boolean, maxWaitMillis: Long): Unit = {
    if (pool == null) {
      val poolConfig = new GenericObjectPoolConfig()
      poolConfig.setMaxTotal(maxTotal)
      poolConfig.setMaxIdle(maxIdle)
      poolConfig.setMinIdle(minIdle)
      poolConfig.setTestOnBorrow(testOnBorrow)
      poolConfig.setTestOnReturn(testOnReturn)
      poolConfig.setMaxWaitMillis(maxWaitMillis)
      pool = new JedisPool(poolConfig, redisHost, redisPort, redisTimeout, "root_jinkun")

      val hook = new Thread {
        override def run = pool.destroy()
      }
      sys.addShutdownHook(hook.run)
    }
  }

  def getPool: JedisPool = {
    assert(pool != null)
    pool
  }
}
