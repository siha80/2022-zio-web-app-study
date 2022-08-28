import zhttp.http._
import zio._
import zio.json._

// web server를 만들어 보세요
// https://github.com/dream11/zio-http/blob/main/example/src/main/scala/example/HelloWorld.scala


final case class Todo(id: Long, title: String)

object Todo {
  implicit val TodoJsonCodec: JsonCodec[Todo] = DeriveJsonCodec.gen
}

object App extends ZIOAppDefault {
  val TodoList = List(
    Todo(1, "a"),
    Todo(2, "b"),
    Todo(3, "c"),
    Todo(4, "d"),
  )

  def app = Http.collect[Request] {
    case Method.GET -> !! / "todo" / "list" =>
      Response.json(TodoList.toJson)
    case Method.GET -> !! / "todo" / id =>
      id.toLongOption match {
        case Some(id) =>
          val item = TodoList.find(i => i.id == id)
          item match {
            case Some(i) => Response.json(i.toJson)
            case None => Response.status(Status.NotFound)
          }
        case None => Response.status(Status.BadRequest)
      }
  }

  def run =
    zhttp.service.Server.start(8080, app)
}
