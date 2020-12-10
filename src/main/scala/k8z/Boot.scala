package k8z

import k8z.watch._
import zio._
import zio.config._
import zio.system.System

object Boot extends scala.App {
  val namespace: String = "kube-system"

  val cfg: Layer[ReadError[String], client.ApiConfig] = System.live >>> ZConfig.fromSystemEnv(client.config.master)
  val deps = cfg >+> client.Client.in(namespace) >+> pods.Pods.live() >+> watch.Watching.live()

  val app = for {
    c <- getConfig[client.config.ApiConfig]
    _ <- IO.effect(println(c.toUrl))

    q <- Watching.all()
    l = for {
      e <- q.take
    } yield
      e match {
        case Created() => println("... created ...")
        case Deleted() => println("... deleted ...")
        case _         =>
      }
    _ <- l.forever
  } yield ()

  Runtime.unsafeFromLayer(deps).unsafeRun(app)
}
