import sttp.client3.httpclient.zio._
import sttp.client3.ziojson._
import sttp.client3.{SttpBackend, UriContext, basicRequest}
import zhttp.service.server.ServerChannelFactory
import zhttp.service.{EventLoopGroup, Server, ServerChannelFactory}
import zio._
import zio.test._

// 서버를 테스트해보세요
// https://zio.dev/reference/test/
// https://sttp.softwaremill.com/en/latest/quickstart.html
// https://sttp.softwaremill.com/en/latest/backends/zio.html

class TestAppDriver(port: Int, backend: SttpBackend[Task, Any]) {
  def getById: Task[Todo] =
    basicRequest.get(uri"http://localhost:$port/todo/1")
      .response(asJsonAlways[Todo])
      .send(backend)
      .map(_.body)
      .right
      .mapError(_.merge)

  def getList: Task[List[Todo]] =
    basicRequest.get(uri"http://localhost:$port/todo/list")
      .response(asJsonAlways[List[Todo]])
      .send(backend)
      .map(_.body)
      .right
      .mapError(_.merge)

  def create(form: CreateForm): Task[Todo] =
    basicRequest.post(uri"http://localhost:$port/todo")
      .body(form)
      .response(asJsonAlways[Todo])
      .send(backend)
      .map(_.body)
      .right
      .mapError(_.merge)
}

object TestAppDriver {
  val layer: ZLayer[EventLoopGroup & ServerChannelFactory, Throwable, TestAppDriver] =
    ZLayer.scoped {
      (
        for {
          backend <- ZIO.service[SttpBackend[Task, Any]]
          server <- Server
            .app(TodoApp.route)
            .withPort(0)
            .make
        } yield new TestAppDriver(server.port, backend)
        ).provideSome(
        HttpClientZioBackend.layer(),
        TodoRepositoryInMemory.layer
      )
    }
}

object AppSpec extends ZIOSpecDefault {

  override def spec = suite("App")(
    test("request test") {
      assertTrue(true)
    },
    test("todo list") {
      val expectedList = List(
        Todo(1, "a"),
        Todo(2, "b"),
        Todo(3, "c"),
        Todo(4, "d")
      )

      for {
        response <- ZIO.serviceWithZIO[TestAppDriver](_.getList)
      } yield assertTrue(response == expectedList)
    },
    test("todo by id") {
      val expected = Todo(1, "a")
      for {
        response <- ZIO.serviceWithZIO[TestAppDriver](_.getById)
      } yield assertTrue(response == expected)
    },
    test("create todo") {
      val form = CreateForm("test - title")
      for {
        response <- ZIO.serviceWithZIO[TestAppDriver](app => app.create(form))
      } yield assertTrue(response.title == form.title)
    }
  ).provideSome[EventLoopGroup & ServerChannelFactory](
    TestAppDriver.layer
  ).provideShared(
    EventLoopGroup.auto(1),
    ServerChannelFactory.auto,
  )
}
