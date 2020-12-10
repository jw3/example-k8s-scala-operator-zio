package k8z

import io.fabric8.kubernetes.api.model.Pod
import io.fabric8.kubernetes.client.{Watcher, WatcherException}
import k8z.client.Client
import zio._

// todo;; locked to pods for now...
object watch {
  sealed trait WatchEvent
  case class Created() extends WatchEvent
  case class Updated() extends WatchEvent
  case class Deleted() extends WatchEvent
  case class Failure() extends WatchEvent

  type Watching = Has[Watching.Service]
  object Watching {
    trait Service {
      def all(): Task[Queue[WatchEvent]]
      def named(name: String): Task[Queue[WatchEvent]]
    }

    def live(): ZLayer[Client, Throwable, Has[Service]] =
      (for {
        c <- Client.impl.map(_.pods())
      } yield
        new Service {
          def all(): Task[Queue[WatchEvent]] =
            for {
              // todo;; streams?
              q <- Queue.unbounded[WatchEvent]
              _ = c.watch(new Watcher[Pod] {
                val rt = Runtime.default
                def eventReceived(action: Watcher.Action, resource: Pod): Unit = action match {
                  case Watcher.Action.ADDED =>
                    rt.unsafeRun(q.offer(Created()))
                  case Watcher.Action.MODIFIED =>
                    rt.unsafeRun(q.offer(Updated()))
                  case Watcher.Action.DELETED =>
                    rt.unsafeRun(q.offer(Deleted()))
                  case Watcher.Action.ERROR =>
                    rt.unsafeRun(q.offer(Failure()))
                }
                def onClose(cause: WatcherException): Unit = q.offer(Failure())
              })
            } yield q

          def named(name: String): Task[Queue[WatchEvent]] =
            for {
              q <- Queue.unbounded[WatchEvent]
              _ = Option(c.withName(name)) match {
                case None => q.offer(Failure())
                case Some(p) =>
                  p.watch(new Watcher[Pod] {
                    def eventReceived(action: Watcher.Action, resource: Pod): Unit = action match {
                      case Watcher.Action.ADDED    => q.offer(Created())
                      case Watcher.Action.MODIFIED => q.offer(Updated())
                      case Watcher.Action.DELETED  => q.offer(Deleted())
                      case Watcher.Action.ERROR    => q.offer(Failure())
                    }
                    def onClose(cause: WatcherException): Unit = q.offer(Failure())
                  })
              }
            } yield q
        }).toLayer

    def all(): RIO[Watching with Client, Queue[WatchEvent]] = ZIO.accessM(_.get.all())
    def named(name: String): RIO[Watching with Client, Queue[WatchEvent]] = ZIO.accessM(_.get.named(name))
  }
}
