package traits

import java.time.LocalDateTime

trait EntitiesModule {

  type Id
  def createIdFromString(input: String): Option[Id]

  sealed trait Todo {
    protected type ThisType <: Todo

    def description: String
    def deadline: LocalDateTime
    def withUpdatedDescription(newDescription: String): ThisType
    def withUpdatedDeadline(newDeadline: LocalDateTime): ThisType
  }

  case object Todo {
    final case class Data(description: String, deadline: LocalDateTime) extends Todo {
      override protected type ThisType = Data

      override def withUpdatedDescription(newDescription: String): ThisType =
        copy(description = newDescription)

      override def withUpdatedDeadline(newDeadline: LocalDateTime): ThisType =
        copy(deadline = newDeadline)
    }
    final case class Existing(id: Id, data: Data) extends Todo {
      override protected type ThisType = Existing

      override def description: String =
        data.description

      override def withUpdatedDescription(newDescription: String): ThisType =
        copy(data = data.withUpdatedDescription(newDescription))

      override def deadline: LocalDateTime =
        data.deadline

      override def withUpdatedDeadline(newDeadline: LocalDateTime): ThisType =
        copy(data = data.withUpdatedDeadline(newDeadline))
    }
  }

}
