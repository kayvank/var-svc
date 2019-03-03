package utils

import FailedComputationWrapper._
import org.specs2.mutable.Specification
import scala.util._
import com.typesafe.scalalogging.LazyLogging
import model.Exceptions._

class FailedComputationWrapperSpec extends Specification with LazyLogging {
  "Failed computation adapter specs".title

  "should correctly convert Try[computation] to Task[computation] for all computation types" >> {
    val successComputation = Try("successComputation")
    val failedComputation = Try(new UserComputationException("some failure"))
    successComputation.asTask.run === "successComputation" && 
      failedComputation.asTask.run === new UserComputationException("some failure")
  }
}
