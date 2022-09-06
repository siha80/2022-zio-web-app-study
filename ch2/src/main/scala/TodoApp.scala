import zhttp.http.{Http, Method, Request, Response, Status}
import zio._
import zhttp.http._
import zio.json._

object TodoApp {
  def route =
    Http.collectZIO[Request] {
      case Method.GET -> !! / "todo" / "list" =>
        TodoRepo.getAll.map(list => Response.json(list.toJson))
      case Method.GET -> !! / "todo" / id =>
        id.toLongOption match {
          case Some(id) =>
            for {
              item <- TodoRepo.findById(id)
            } yield {
              item match {
                case Some(i) => Response.json(i.toJson)
                case None    => Response.status(Status.NotFound)
              }
            }
          case None => ZIO.succeed(Response.status(Status.BadRequest))
        }
      case req @ Method.POST -> !! / "todo" =>
        for {
          form <- req.bodyAsString.map(_.fromJson[CreateForm])
          r <- form match {
            case Left(e) =>
              ZIO.succeed(Response.text(e).setStatus(Status.BadRequest))
            case Right(f) =>
              TodoRepo.create(f).map(todo => Response.json(todo.toJson))
          }
        } yield r
    }
}
