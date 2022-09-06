import zio.{Task, ZIO}

trait TodoRepo {
  def create(form: CreateForm): Task[Todo]
  def getAll: Task[List[Todo]]
  def findById(id: Long): Task[Option[Todo]]
}

object TodoRepo {
  def create(form: CreateForm) =
    ZIO.serviceWithZIO[TodoRepo](_.create(form))
  def getAll = ZIO.serviceWithZIO[TodoRepo](_.getAll)
  def findById(id: Long) = ZIO.serviceWithZIO[TodoRepo](_.findById(id))
}

