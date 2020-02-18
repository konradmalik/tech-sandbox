package traits

import java.time.format.DateTimeFormatter
import java.time.LocalDateTime

object TodoApp extends App {
  type Cake = ApplicationBoundaryModule with PersistenceGatewayModule with TerminalUserInterfaceModule

  val cake: Cake = new ApplicationCoreModule
                    with InMemoryPersistenceGatewayModule
                    with TerminalUserInterfaceModule {
    final override val pattern: DateTimeFormatter = DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy H:m")

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

  }

  cake.userInterface.run()
}
