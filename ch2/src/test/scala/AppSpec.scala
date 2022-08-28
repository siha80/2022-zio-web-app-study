import sttp.client3.{HttpClientSyncBackend, UriContext, basicRequest}
import sttp.model.StatusCode
import zhttp.service.Server
import zio.{Scope, ZIO, ZLayer}
import zio.json._
import zio.test._

// 서버를 테스트해보세요
// https://zio.dev/reference/test/
// https://sttp.softwaremill.com/en/latest/quickstart.html
// https://sttp.softwaremill.com/en/latest/backends/zio.html

object AppSpec extends ZIOSpecDefault {
  override def spec = suite("App")(
    test("request test") {
      assertTrue(true)
    },
    test("todo list") {
      val backend = HttpClientSyncBackend()
      val request = basicRequest.get(uri"http://localhost:8080/todo/list")
      val response = request.send(backend)

      assertTrue(response.code.code == 200)

      val expectedList = List(
        Todo(1, "a"),
        Todo(2, "b"),
        Todo(3, "c"),
        Todo(4, "d"),
      )

      val expected = expectedList.toJson
      assertTrue(response.body match {
        case Right(r) => r == expected
        case _ => false
      }
      )
    },
    test("todo by id") {
      val backend = HttpClientSyncBackend()
      val request = basicRequest.get(uri"http://localhost:8080/todo/1")
      val response = request.send(backend)

      val expected = Todo(1, "a")
      val rightBody = response.body.flatMap(_.fromJson[Todo])
      assertTrue(response.code == StatusCode.Ok) &&
        assertTrue(rightBody == Right(expected))
    }
  )
}
