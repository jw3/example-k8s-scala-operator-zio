package k8z

import io.fabric8.kubernetes.client.{ConfigBuilder, DefaultKubernetesClient, KubernetesClient}
import zio._
import zio.config.getConfig

object client {
  type ApiConfig = zio.config.ZConfig[config.ApiConfig]

  type Client = Has[Client.Service]
  object Client {
    trait Service {
      def get(): Task[KubernetesClient]
    }

    def in(ns: String): ZLayer[ApiConfig, Throwable, Has[Service]] = (for {
      cfg <- getConfig[client.config.ApiConfig]
      client = new DefaultKubernetesClient(
        new ConfigBuilder()
          .withMasterUrl(cfg.toUrl)
          .withOauthToken(cfg.token)
          .withTrustCerts(true) // todo;; move to config
          .withNamespace(ns)
          .build)
    } yield new Service {
      def get(): Task[KubernetesClient] = Task.succeed(client)
    }).toLayer

    def impl: RIO[Client, KubernetesClient] = ZIO.accessM(_.get.get())
  }

  object config {
    import zio.config._
    import ConfigDescriptor._

    val ApiTokenKey = "KUBERNETES_API_TOKEN"
    val PodTokenPath = "/var/run/secrets/kubernetes.io/serviceaccount/token"

    case class ApiConfig(host: String, port: Int, token: String) {
      def toUrl: String = s"https://$host:$port"
    }

    val token: ConfigDescriptor[String] = string("Foo_bar").default("invalidToken")
    val master: ConfigDescriptor[ApiConfig] =
      (
        string("KUBERNETES_SERVICE_HOST").default("localhost") |@|
          int("KUBERNETES_SERVICE_PORT").default(16443) |@|
          string(ApiTokenKey).default(PodTokenPath)
      )(ApiConfig.apply, ApiConfig.unapply)
  }
}
