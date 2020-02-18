package packages

import java.time.format.DateTimeFormatter
import java.time.LocalDateTime

object TodoApp extends App {
  val persistenceGateway: PersistenceGateway = InMemoryPersistenceGateway
  val applicationBoundary: ApplicationBoundary = new ApplicationCore(persistenceGateway)
  val pattern: DateTimeFormatter = DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy H:m")
  val userInterface: TerminalUserInterface = new TerminalUserInterface(applicationBoundary, pattern)

  applicationBoundary.createOne(
    Todo.Data(
      description = "First One",
      deadline    = LocalDateTime.of(1955, 11, 5, 12, 34)
    )
  )

  applicationBoundary.createOne(
    Todo.Data(
      description = "Second One",
      deadline    = LocalDateTime.of(1957, 11, 5, 12, 34)
    )
  )

  userInterface.run()
}
