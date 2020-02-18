package graphql

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server._
import sangria.ast.Document
import sangria.execution._
import sangria.marshalling.sprayJson._
import sangria.parser.QueryParser
import spray.json.{ JsObject, JsString, JsValue }

import scala.concurrent.ExecutionContext
import scala.util.{ Failure, Success }

object GraphQLServer {

  private val repository = ShopRepository.createDatabase()

  def endpoint(requestJSON: JsValue)(implicit e: ExecutionContext): Route = {

    val JsObject(fields) = requestJSON

    val JsString(query) = fields("query")

    val operation = fields.get("operationName") collect {
      case JsString(op) ⇒ op
    }

    val vars = fields.get("variables") match {
      case Some(obj: JsObject) => obj
      case _ => JsObject.empty
    }

    QueryParser.parse(query) match {
      case Success(queryAst) =>
        complete(executeGraphQLQuery(queryAst, operation, vars))
      case Failure(error) =>
        complete(BadRequest, JsObject("error" -> JsString(error.getMessage)))
    }
  }

  private val rejectComplexQueries: QueryReducer[ShopRepository, ShopRepository] = QueryReducer.rejectComplexQueries(300, (_: Double, _: ShopRepository) => TooComplexQuery)

  private def executeGraphQLQuery(query: Document, op: Option[String], vars: JsObject)(implicit e: ExecutionContext) = {
    Executor.execute(
      SchemaDef.ShopSchema,
      query,
      repository,
      variables = vars,
      operationName = op,
      deferredResolver = SchemaDef.deferredResolver,
      queryReducers = rejectComplexQueries :: Nil)
      .map(OK -> _)
      .recover {
        case error: QueryAnalysisError => BadRequest -> error.resolveError
        case error: ErrorWithResolver => InternalServerError -> error.resolveError
      }
  }

  case object TooComplexQuery extends Exception

}