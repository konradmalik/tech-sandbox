package traits

trait ApplicationCoreModule extends EntitiesModule with ApplicationBoundaryModule {

  this: PersistenceGatewayModule =>

  final override object applicationBoundary extends ApplicationBoundary {
    // C
    override def createOne(todo: Todo.Data): Todo.Existing =
      createMany(Set(todo)).head

    override def createMany(todos: Set[Todo.Data]): Set[Todo.Existing] = {
      writeMany(todos)
    }

    private def writeMany[T <: Todo](todos: Set[T]): Set[Todo.Existing] = {
      persistenceGateway.writeMany {
        todos.map { todo =>
          todo.withUpdatedDescription(todo.description.trim)
        }
      }
    }

    // R
    override def readOneById(id: Id): Option[Todo.Existing] =
      readManyById(Set(id)).headOption

    override def readManyById(ids: Set[Id]): Set[Todo.Existing] =
      persistenceGateway.readManyById(ids)

    override def readManyByPartialDescription(partialDescription: String): Set[Todo.Existing] =
      if(partialDescription.isEmpty)
        Set.empty
      else
        persistenceGateway.readManyByPartialDescription(partialDescription.trim)

    override def readAll: Set[Todo.Existing] =
      persistenceGateway.readAll

    // U
    override def updateOne(todo: Todo.Existing): Todo.Existing =
      updateMany(Set(todo)).head

    override def updateMany(todos: Set[Todo.Existing]): Set[Todo.Existing] =
      writeMany(todos)

    // D
    override def deleteOne(todo: Todo.Existing): Unit =
      deleteMany(Set(todo))

    override def deleteMany(todos: Set[Todo.Existing]): Unit =
      persistenceGateway.deleteMany(todos)

    override def deleteAll(): Unit =
      persistenceGateway.deleteAll
  }
}
