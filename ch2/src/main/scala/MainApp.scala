import zhttp.http._
import zhttp.service.{EventLoopGroup, Server, ServerChannelFactory}
import zio._

object MainApp extends ZIOAppDefault {

  val serverLayer: ZLayer[Any, Throwable, Http[TodoRepo, Throwable, Request, Response]] =
    ZLayer {
      ZIO.attempt(TodoApp.route)
    }

  def run: ZIO[Any with Scope, Any, Any] = (
    for {
      httpApp <- ZIO.service[Http[TodoRepo, Throwable, Request, Response]]
      - <- Server.start(8080, httpApp)
    } yield ()
  ).provide(
    TodoRepositoryInMemory.layer,
    serverLayer
  )
}
