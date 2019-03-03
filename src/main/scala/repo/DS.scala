package repo

import doobie.imports._

import scalaz.concurrent.Task
import utils.Global._
import doobie.hikari.hikaritransactor._
import utils.Monitor
import Monitor._
import scalaz._
import Scalaz._

object DS {
  val jdbcUrl = cfg.getString("db.jdbc.url")
  val jdbcUser = cfg.getString("db.jdbc.user")
  val jdbcPass = cfg.getString("db.jdbc.password")
  val jdbcDriver = cfg.getString("db.jdbc.driver")
  println(s"jdbcUrl = $jdbcUrl")

  implicit lazy val hxa: HikariTransactor[Task] =
    HikariTransactor[Task](
      jdbcDriver,
      jdbcUrl,
      jdbcUser,
      jdbcPass).unsafePerformSync
  val connectionPoolThreads = cfg.getInt("db.connection.pool.threads")
  val _ = (hxa.configure(hx =>
    Task.delay(hx.setMaximumPoolSize(connectionPoolThreads)))).unsafePerformSync

  implicit val queryByIsrc: String => ConnectionIO[Option[String]] = isrc => {
    sql"select record from video_data where isrc=$isrc limit 1".query[String].option
  }


  def findVideoByIsrc(isrc: String): HikariTransactor[Task] => Task[Option[String]] =
    (tx: HikariTransactor[Task]) => {
      val _ = counterdbAccess.increment()
      queryByIsrc(isrc).transact(tx)
    }

  def connectionStatus = {
    val program3 = sql"select 42".query[Int].unique
    (program3.transact(hxa).unsafePerformSync == 42)
  }

}
