import zio.{Ref, Task, ZLayer}

class TodoRepositoryInMemory(todoList: Ref[List[Todo]]) extends TodoRepository {
  def getAll: Task[List[Todo]] = todoList.get

  def findById(id: Long): Task[Option[Todo]] =
    todoList.get.map(_.find(_.id == id))

  def create(form: CreateForm): Task[Todo] = todoList.modify { list =>
    val id = list.length + 1
    val newTodo = Todo(id, form.title)
    (newTodo, list :+ newTodo)
  }
}

object TodoRepositoryInMemory {
  val layer: ZLayer[Any, Nothing, TodoRepositoryInMemory] = ZLayer {
    for {
      ref <- Ref.make(
        List(
          Todo(1, "a"),
          Todo(2, "b"),
          Todo(3, "c"),
          Todo(4, "d")
        )
      )
    } yield new TodoRepositoryInMemory(ref)
  }
}
