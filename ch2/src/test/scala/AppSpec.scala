import sttp.client3.httpclient.zio._
import sttp.client3.ziojson._
import sttp.client3.{SttpBackend, UriContext, asStringAlways, basicRequest}
import zhttp.service.server.ServerChannelFactory
import zhttp.service.{EventLoopGroup, Server, ServerChannelFactory}
import zio._
import zio.test.Assertion.equalTo
import zio.test._

// 서버를 테스트해보세요
// https://zio.dev/reference/test/
// https://sttp.softwaremill.com/en/latest/quickstart.html
// https://sttp.softwaremill.com/en/latest/backends/zio.html

class TestAppDriver(port: Int, backend: SttpBackend[Task, Any]) {
  def hello: Task[String] = {
    for {
      res <- basicRequest.get(uri"http://localhost:$port/hello")
        .response(asStringAlways)
        .send(backend)
    } yield res.body
  }

  def getById(id: Long): Task[Todo] =
    basicRequest.get(uri"http://localhost:$port/todo/$id")
      .response(asJsonAlways[Todo])
      .send(backend)
      .map(_.body)
      .right
      .mapError(_.merge)

  def getList: Task[List[Todo]] =
    basicRequest
      .get(uri"http://localhost:$port/todo/list")
      .response(asJsonAlways[List[Todo]])
      .send(backend)
      .map(_.body)
      .right
      .mapError(_.merge)

  def create(form: CreateForm): Task[Todo] =
    basicRequest
      .post(uri"http://localhost:$port/todo")
      .body(form)
      .response(asJsonAlways[Todo])
      .send(backend)
      .map(_.body)
      .right
      .mapError(_.merge)
}

object TestAppDriver {
  def hello = ZIO.serviceWithZIO[TestAppDriver](_.hello)
  def getById(id: Long) = ZIO.serviceWithZIO[TestAppDriver](_.getById(id))
  def getList = ZIO.serviceWithZIO[TestAppDriver](_.getList)
  def create(title: String) = ZIO.serviceWithZIO[TestAppDriver](_.create(CreateForm(title)))

  val layer: ZLayer[Server.Start, Throwable, TestAppDriver] =
    ZLayer.scoped {
      for {
        start <- ZIO.service[Server.Start]
        backend <- HttpClientZioBackend()
      } yield new TestAppDriver(start.port, backend)
    }
}

object AppSpec extends ZIOSpec[EventLoopGroup & ServerChannelFactory] {

  override def bootstrap
      : ZLayer[Scope, Any, EventLoopGroup & ServerChannelFactory] =
    EventLoopGroup.auto(1) ++ ServerChannelFactory.auto

  override def spec = suite("App")(
    test("hello") {
      assertZIO(TestAppDriver.hello)(equalTo("hello"))
    },

    test("todo list") {
      val expectedList = List(
        Todo(1, "a"),
        Todo(2, "b"),
        Todo(3, "c"),
        Todo(4, "d")
      )

      assertZIO(TestAppDriver.getList)(equalTo(expectedList))
    },
    test("todo by id") {
      val expected = Todo(1, "a")
      assertZIO(TestAppDriver.getById(expected.id))(equalTo(expected))
    },
    test("create todo") {
      val form = CreateForm("test - title")
      for {
        oldList <- TestAppDriver.getList
        _ <- assertTrue(!oldList.exists(_.title == form.title))
        newItem <- TestAppDriver.create(form.title)
        newList <- TestAppDriver.getList
      } yield assertTrue(newList.contains(newItem))
    }
  ).provideSome[Scope with Environment](
    TestAppDriver.layer,
    TodoRepositoryInMemory.layer,
    TodoApp.layer,
    HelloApp.layer,
    ZLayer {
      for {
        todoApp <- ZIO.service[TodoApp]
        helloApp <- ZIO.service[HelloApp]
        start <- Server.app(todoApp.route ++ helloApp.route).withPort(0).make
      } yield start
    }
  )
}
