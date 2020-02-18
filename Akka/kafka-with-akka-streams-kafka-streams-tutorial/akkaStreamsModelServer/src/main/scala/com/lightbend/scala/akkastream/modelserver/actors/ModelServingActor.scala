package com.lightbend.scala.akkastream.modelserver.actors

import java.util.concurrent.TimeUnit

import akka.actor.{Actor, Props}
import com.lightbend.model.winerecord.WineRecord
import com.lightbend.scala.akkastream.modelserver.actors.persistence.FilePersistence
import com.lightbend.scala.modelServer.model.{Model, ModelToServeStats, ModelWithDescriptor, ServingResult}

/**
 * Actor that handles messages to update a model and to score records using the current model.
 * @param dataType indicating either the record type or model parameters. Used as a file name.
 */
class ModelServingActor(dataType : String) extends Actor {

  println(s"Creating model serving actor $dataType")
  private var currentModel: Option[Model] = None
  private var newModel: Option[Model] = None
  var currentState: Option[ModelToServeStats] = None
  private var newState: Option[ModelToServeStats] = None

  override def preStart {
    val state = FilePersistence.restoreState(dataType)
    newState = state._2
    newModel = state._1
  }

  override def receive: PartialFunction[Any, Unit] = {
    case model : ModelWithDescriptor =>
      // Update model
      println(s"Updated model: $model")
      newState = Some(ModelToServeStats(model.descriptor))
      newModel = Some(model.model)
      FilePersistence.saveState(dataType, newModel.get, newState.get)
      sender() ! "Done"

    case record : WineRecord =>
      // Process data
      newModel.foreach { model =>
        // Update model
        // close current model first
        currentModel.foreach(_.cleanup())
        // Update model
        currentModel = newModel
        currentState = newState
        newModel = None
      }

      currentModel match {
        case Some(model) =>
          val start = System.nanoTime()
          val quality = model.score(record).asInstanceOf[Double]
          val duration = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start)
          currentState = currentState.map(_.incrementUsage(duration))
          sender() ! ServingResult(quality, duration)

        case None =>
          sender() ! ServingResult.noModel
      }

    case _ : GetState => {
      // State query
      sender() ! currentState.getOrElse(ModelToServeStats.empty)
    }
  }
}

object ModelServingActor{
  def props(dataType : String) : Props = Props(new ModelServingActor(dataType))
}

/** Used as an Actor message. */
case class GetState(dataType : String)
