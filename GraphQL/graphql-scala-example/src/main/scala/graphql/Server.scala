package graphql

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer
import com.typesafe.scalalogging.LazyLogging
import spray.json.JsValue

import scala.concurrent.{ Await, ExecutionContext, Future }
import scala.language.postfixOps
import scala.util.{ Failure, Success }

object Server extends App with LazyLogging {

  scala.sys.addShutdownHook(() -> shutdown())

  import scala.concurrent.duration._

  // set up ActorSystem and other dependencies here
  implicit val system: ActorSystem = ActorSystem("graphQLAkkaHttpServer")
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  implicit val executionContext: ExecutionContext = system.dispatcher

  val route: Route =
    (post & path("graphql")) {
      entity(as[JsValue]) { requestJson =>
        GraphQLServer.endpoint(requestJson)
      }
    } ~ {
      getFromResource("graphiql.html")
    }

  val serverBinding: Future[Http.ServerBinding] = Http().bindAndHandle(route, "localhost", 8080)

  serverBinding.onComplete {
    case Success(bound) =>
      logger.info(s"Server online!")
      logger.info(s"open a browser at http://${bound.localAddress.getHostString}:${bound.localAddress.getPort}")
      logger.info(s"or POST queries to http://${bound.localAddress.getHostString}:${bound.localAddress.getPort}/graphql")
    case Failure(e) =>
      Console.err.println(s"Server could not start!")
      e.printStackTrace()
      system.terminate()
  }

  private def shutdown(): Unit = {
    logger.info("Terminating...")
    system.terminate()
    Await.result(system.whenTerminated, 30 seconds)
    logger.info("Terminated server")
  }
}
