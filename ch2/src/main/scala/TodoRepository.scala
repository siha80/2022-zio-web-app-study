import zio.{Task, ZIO}

trait TodoRepository {
  def create(form: CreateForm): Task[Todo]
  def getAll: Task[List[Todo]]
  def findById(id: Long): Task[Option[Todo]]
}

//object TodoRepository {
//  def create(form: CreateForm) =
//    ZIO.serviceWithZIO[TodoRepository](_.create(form))
//  def getAll = ZIO.serviceWithZIO[TodoRepository](_.getAll)
//  def findById(id: Long) = ZIO.serviceWithZIO[TodoRepository](_.findById(id))
//}

