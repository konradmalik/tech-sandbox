package traits

trait ApplicationBoundaryModule {

  this: EntitiesModule =>

  def applicationBoundary: ApplicationBoundary

  trait ApplicationBoundary {
    // C
    def createOne(todo: Todo.Data): Todo.Existing
    def createMany(todos: Set[Todo.Data]): Set[Todo.Existing]

    // R
    def readOneById(id: Id): Option[Todo.Existing]
    def readManyById(ids: Set[Id]): Set[Todo.Existing]
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
}
