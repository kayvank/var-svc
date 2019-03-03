package utils

import kamon.Kamon
import kamon.metric.instrument.{Counter, Gauge}

import scalaz.concurrent.Task

object Monitor {

  val counterHttpReq =
    Kamon.metrics.counter("vc-http-request")

  val counterByIsrc200s =
    Kamon.metrics.counter("vc-isrc-200")

  val counterRelated200s =
    Kamon.metrics.counter("vc-related-200")

  val counter400s =
    Kamon.metrics.counter("vc-400")

  val counter500s =
    Kamon.metrics.counter("vc-500")

  val counterDbAcess =
    Kamon.metrics.counter("vc-db-request")

  val counterNoneLiftigniter =
    Kamon.metrics.counter("vc-nonLiftigniter-recommendation")

  val counterLiftigniter =
    Kamon.metrics.counter("vc-liftigniter-recommendation")

  val counterdbAccess =
    Kamon.metrics.counter("vc-database-access")

  val counterInternalTimeout =
    Kamon.metrics.counter("vc-internal-timeout")

}
