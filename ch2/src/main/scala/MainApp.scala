import zhttp.http._
import zhttp.service.Server
import zio._

case class HelloApp() {
  val route: Http[Any, Nothing, Request, Response] = Http.collectZIO[Request] {
    case Method.GET -> !! / "hello" =>
      ZIO.succeed(Response.text("hello"))
  }
}

object HelloApp {
  val layer: ZLayer[Any, Nothing, HelloApp] = ZLayer.succeed(HelloApp.apply())
}

object MainApp extends ZIOAppDefault {
  val prog: ZIO[TodoApp with HelloApp, Throwable, Unit] =
    for {
      helloApp <- ZIO.service[HelloApp]
      todoApp <- ZIO.service[TodoApp]
      _ <- Server.start(8080, todoApp.route ++ helloApp.route)
    } yield ()

  def run: ZIO[Any with Scope, Any, Any] =
    prog.provide(
      TodoRepositoryInMemory.layer,
      TodoApp.layer,
      HelloApp.layer
    )
}
