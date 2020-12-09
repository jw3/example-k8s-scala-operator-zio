package k8z

import io.fabric8.kubernetes.api.model.{Pod, ReplicationController}
import io.fabric8.kubernetes.client.{Watcher, WatcherException}
import k8z.client.Client
import zio.{Has, RIO, Task, URIO, ZIO, ZLayer}

import scala.jdk.CollectionConverters._

object pods {
  type Pods = Has[Pods.Service]

  object Pods {
    trait Service {
      def get(): Task[List[Pod]]
      def get(name: String): Task[Option[Pod]]
      def create(pod: Pod*): Task[Pod]
      def apply(pod: Pod*): Task[Pod]
      def delete(pod: Pod*): Task[Boolean]
    }

    def in(ns: String): ZLayer[Client, Throwable, Has[Service]] = (for {
      pods <- Client.impl.map(_.pods().inNamespace(ns))
    } yield new Service {

//      pods.withName("bar").watch(new Watcher[Pod] {
//        def eventReceived(action: Watcher.Action, resource: Pod): Unit = action match {
//          case Watcher.Action.ADDED =>
//          case Watcher.Action.MODIFIED =>
//          case Watcher.Action.DELETED =>
//          case Watcher.Action.ERROR =>
//        }
//
//        def onClose(cause: WatcherException): Unit = ???
//      })

      def get(): Task[List[Pod]] = Task.succeed(pods.list.getItems.asScala.toList)
      def get(name: String): Task[Option[Pod]] = Task.succeed(Option(pods.withName(name).fromServer.get))
      def create(pod: Pod*): Task[Pod] = Task.succeed(pods.create(pod: _*))
      def apply(pod: Pod*): Task[Pod] = Task.succeed(pods.createOrReplace(pod: _*))
      def delete(pod: Pod*): Task[Boolean] = Task.succeed(pods.delete(pod: _*))
    }).toLayer

    def get(): RIO[Pods with Client, List[Pod]] = ZIO.accessM(_.get.get())
    def get(name: String): RIO[Pods with Client, Option[Pod]] = ZIO.accessM(_.get.get(name))
    def create(pod: Pod*): RIO[Pods with Client, Pod] = ZIO.accessM(_.get.create(pod: _*))
    def apply(pod: Pod*): RIO[Pods with Client, Pod] = ZIO.accessM(_.get.apply(pod: _*))
    def delete(pod: Pod*): RIO[Pods with Client, Boolean] = ZIO.accessM(_.get.delete(pod: _*))

  }
}
