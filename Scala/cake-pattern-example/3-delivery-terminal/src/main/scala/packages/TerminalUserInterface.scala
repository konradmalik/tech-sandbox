package packages

import scala.io._
import scala.util._

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class TerminalUserInterface(applicationBoundary: ApplicationBoundary, pattern: DateTimeFormatter) {
  def run(): Unit = {
    var shouldKeepLooping = true

    while(shouldKeepLooping) {
      withPrompt {
        case "c"                         => create()
        case "d"                         => delete()
        case "da"                        => deleteAll()
        case "sa"                        => showAll()
        case "sd"                        => searchByPartialDescription()
        case "sid"                       => searchById()
        case "ud"                        => updateDescription()
        case "udl"                       => updateDeadline()
        case "e" | "q" | "exit" | "quit" => exit()
        case _                           =>
      }
    }

    def withPrompt(onUserInput: String => Unit): Unit = {
      val userInput = StdIn.readLine(menu).toLowerCase.trim

      onUserInput(userInput)
    }

    def menu: String =
      s"""|
          |$hyphens
          |
          |c                   => create new todo
          |d                   => delete todo
          |da                  => delete all todos
          |sa                  => show all todos
          |sd                  => search by partial description
          |sid                 => search by id
          |ud                  => update description
          |udl                 => update deadline
          |e | q | exit | quit => exit the application
          |anything else       => show the main menu
          |
          |Please enter a command: """.stripMargin

    def hyphens: String =
      randomColor + ("-" * 100) + Console.RESET

    def randomColor: String = {
      val randomIndex = Random.nextInt(colors.size)
      colors(randomIndex)
    }

    lazy val colors: Vector[String] =
      Vector(
        Console.BLUE,
        Console.CYAN,
        Console.MAGENTA,
        Console.RED,
        Console.YELLOW,
      )

    def exit(): Unit = {
      println("\nUntil next time!\n")
      shouldKeepLooping = false
    }
  }

  private def create() = {
    withDescriptionPrompt { description =>
      withDeadlinePrompt { deadline =>
        val createdTodo = Todo.Data(description, deadline)
        applicationBoundary.createOne(createdTodo)

        println(Console.GREEN + "Successfully created the new todo" + Console.RESET)
      }
    }
  }

  private def withDescriptionPrompt(onSuccess: String => Unit): Unit = {
    val userInput = StdIn.readLine("Please enter a description: ").trim

    onSuccess(userInput)
  }

  private def withDeadlinePrompt(onSuccess: LocalDateTime => Unit): Unit = {
    val pattern = "yyyy-M-d H:m"

    val format = Console.MAGENTA + pattern + Console.RESET
    val formatter = DateTimeFormatter.ofPattern(pattern)

    val userInput = StdIn.readLine(s"Please enter a deadline in the following format $format: ").trim

    Try(LocalDateTime.parse(userInput, formatter)).toOption match {
      case Some(deadline) => onSuccess(deadline)
      case None           => println(s"\n${Console.YELLOW + userInput + Console.RED} does not match the required format $format${Console.RESET}")
    }
  }

  private def delete() = {
    withIdPrompt { id =>
      withReadOne(id) { todo =>
        applicationBoundary.deleteOne(todo)

        println(Console.GREEN + "Successfully deleted that todo" + Console.RESET)
      }
    }
  }

  private def withIdPrompt(onSuccess: String => Unit): Unit = {
    val userInput = StdIn.readLine("Please enter the id: ").trim

    createIdFromString(userInput) match {
      case Some(id) => onSuccess(id)
      case None     => println(s"\n${Console.YELLOW + userInput + Console.RED} is not a valid id${Console.RESET}")
    }
  }

  private def createIdFromString(userInput: String): Option[String] =
    if(userInput.isEmpty || userInput.contains(" "))
      None
    else
      Some(userInput)

  private def withReadOne(id: String)(onSuccess: Todo.Existing => Unit): Unit = {
    applicationBoundary.readOneById(id) match {
      case Some(todo) => onSuccess(todo)
      case None       => displayNoTodosFoundMessage()
    }
  }

  private def deleteAll() = {
    applicationBoundary.deleteAll()
  }
  private def showAll() = {
    val zeroOrMany = applicationBoundary.readAll

    displayZeroOrMany(zeroOrMany)
  }

  private def displayZeroOrMany(todos: Set[Todo.Existing]): Unit = {
    if(todos.isEmpty)
      displayNoTodosFoundMessage()
    else {
      val renderedSize: String =
        Console.GREEN + todos.size + Console.RESET

      val uxMatters =
        if(todos.size == 1)
          "todo"
        else
          "todos"

      println(s"\nFound $renderedSize $uxMatters:\n")

      todos
        .toSeq
        .sortBy(_.deadline)(OldestFirst)
        .map(renderedWithPattern)
        .foreach(println)
    }
  }

  private def displayNoTodosFoundMessage(): Unit = {
    println(s"\n${Console.YELLOW}No todos found${Console.RESET}")
  }

  private lazy val OldestFirst: Ordering[LocalDateTime] = _ compareTo _

  private def renderedWithPattern(todo: Todo.Existing): String = {
    def renderedId: String =
      Console.GREEN + todo.id + Console.RESET

    def renderedDesciption: String =
      Console.MAGENTA + todo.description + Console.RESET

    def renderedDeadline: String =
      Console.YELLOW + todo.deadline.format(pattern) + Console.RESET

    s"$renderedId $renderedDesciption is due on $renderedDeadline"
  }

  private def searchByPartialDescription(): Unit = {
    withDescriptionPrompt { description =>
        val zeroOrMany =
          applicationBoundary.readManyByPartialDescription(description)

        displayZeroOrMany(zeroOrMany)
    }
  }
  private def searchById(): Unit = {
    withIdPrompt { id =>
        val zeroOrOne =
          applicationBoundary.readOneById(id)

        val zeroOrMany = zeroOrOne.toSet

        displayZeroOrMany(zeroOrMany)
    }
  }
  private def updateDescription(): Unit = {
    withIdPrompt { id => {
        withReadOne(id) { todo =>
          withDescriptionPrompt { description =>
            val updateTodo =
              todo.withUpdatedDescription(description)

            applicationBoundary.updateOne(updateTodo)

            println(Console.GREEN + "Successfully updated the description" + Console.RESET)
          }
        }
      }
    }
  }

  private def updateDeadline(): Unit = {
    withIdPrompt { id => {
        withReadOne(id) { todo =>
          withDeadlinePrompt { deadline =>
            val updateTodo =
              todo.withUpdatedDeadline(deadline)

            applicationBoundary.updateOne(updateTodo)

            println(Console.GREEN + "Successfully updated the deadline" + Console.RESET)
          }
        }
      }
    }
  }
}
