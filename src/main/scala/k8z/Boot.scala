package k8z

import zio._
import zio.config._
import zio.system.System

object Boot extends scala.App {
  val namespace: String = "kube-system"

  val cfg: Layer[ReadError[String], client.ApiConfig] = System.live >>> ZConfig.fromSystemEnv(client.config.master)
  val deps = cfg >+> client.Client.make() >+> pods.Pods.in(namespace)

  val app = for {
    c <- getConfig[client.config.ApiConfig]
    p <- pods.Pods.get()
    _ <- ZIO.succeed(p.filter(_.getStatus.getPhase == "Running").foreach(pp => println(s"${pp.getMetadata.getName}  ${pp.getStatus.getPhase}")))
    _ <- IO.effect(println(c.toUrl))
    _ <- ZIO.succeed(1)
  } yield ()

  Runtime.unsafeFromLayer(deps).unsafeRun(app)
}
