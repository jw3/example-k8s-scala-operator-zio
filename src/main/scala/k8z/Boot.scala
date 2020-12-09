package k8z

import zio._
import zio.config._
import zio.system.System

object Boot extends scala.App {
  val namespace: String = "kube-system"

  val cfg: Layer[ReadError[String], client.ApiConfig] = System.live >>> ZConfig.fromSystemEnv(client.config.master)
  val deps = cfg >+> client.Client.in(namespace) >+> pods.Pods.live()

  val app = for {
    c <- getConfig[client.config.ApiConfig]
    _ <- IO.effect(println(c.toUrl))
    p <- pods.Pods.get().map(_.filter(_.getStatus.getPhase == "Running"))
  } yield {
    p.foreach(pp => println(s"${pp.getMetadata.getName}  ${pp.getStatus.getPhase}"))
  }

  Runtime.unsafeFromLayer(deps).unsafeRun(app)
}
