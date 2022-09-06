import zio.json.{DeriveJsonCodec, JsonCodec}

final case class Todo(id: Long, title: String)
object Todo {
  implicit val TodoJsonCodec: JsonCodec[Todo] = DeriveJsonCodec.gen
}

