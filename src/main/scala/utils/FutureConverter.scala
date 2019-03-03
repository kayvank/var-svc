package utils

import scalaz.concurrent.Task
import scala.concurrent.{Promise, Future}
import scala.util._

object FutureConverter {

  import CustomExecutor._

  final implicit class FutureExtensionOps[A](x: => Future[A]) {

    import scalaz.Scalaz._

    def asTask: Task[A] = {
      Task.async {
        register =>
          x.onComplete {
            case Success(v) => register(v.right)
            case Failure(ex) => register(ex.left)
          }
      }
    }
  }

  final implicit class TaskExtensionOps[A](x: => Task[A]) {

    import scalaz.{\/-, -\/}

    val p: Promise[A] = Promise()

    def runFuture(): Future[A] = {
      x.unsafePerformAsync {
        case -\/(ex) =>
          p.failure(ex); ()
        case \/-(r) => p.success(r); ()
      }
      p.future
    }
  }

}
