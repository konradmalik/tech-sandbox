package packages

trait ApplicationBoundary {
  // C
  def createOne(todo: Todo.Data): Todo.Existing
  def createMany(todos: Set[Todo.Data]): Set[Todo.Existing]

  // R
  def readOneById(id: String): Option[Todo.Existing]
  def readManyById(ids: Set[String]): Set[Todo.Existing]
  def readManyByPartialDescription(partialDescription: String): Set[Todo.Existing]
  def readAll: Set[Todo.Existing]

  // U
  def updateOne(todo: Todo.Existing): Todo.Existing
  def updateMany(todos: Set[Todo.Existing]): Set[Todo.Existing]

  // D
  def deleteOne(todo: Todo.Existing): Unit
  def deleteMany(todos: Set[Todo.Existing]): Unit
  def deleteAll(): Unit
}
