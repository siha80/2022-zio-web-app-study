import zio.json.{DeriveJsonCodec, JsonCodec}

final case class CreateForm(title: String)
object CreateForm {
  implicit val CreateFormCodec: JsonCodec[CreateForm] = DeriveJsonCodec.gen
}
