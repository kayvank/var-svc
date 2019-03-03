package utils

import java.util.concurrent.Executors
import utils.Global._
import scala.concurrent.ExecutionContext

object CustomExecutor {
  val threadPoolSize = cfgVevo.getInt("thread.pool.size")

  implicit val ec = ExecutionContext.fromExecutor(
    Executors.newFixedThreadPool(threadPoolSize))
}
