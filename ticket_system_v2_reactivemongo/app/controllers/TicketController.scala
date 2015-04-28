package controllers

import models._
import play.api.Play.current
import play.api.data.Form
import play.api.data.Forms._
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.json._
import play.api.libs.ws._
import play.api.mvc._

import play.modules.reactivemongo.MongoController
import play.modules.reactivemongo.json.collection.JSONCollection
import play.api.libs.iteratee.Enumerator

import reactivemongo.bson._
import reactivemongo.core.commands._
import reactivemongo.api.indexes.Index
import reactivemongo.api.indexes.IndexType
import play.modules.reactivemongo.json.BSONFormats._

object TicketController extends Controller with MongoController{

  def loginForm = Action {
    Ok(views.html.login("Hello world."))
  }

  val credentialForm: Form[Credential] = Form {
    mapping("username" -> text, "password" -> text)(Credential.apply)(Credential.unapply)
  }

  def credentialDB: JSONCollection = db.collection[JSONCollection]("credential")

  def ticketDB: JSONCollection = db.collection[JSONCollection]("tickets")

  def login = Action.async(parse.urlFormEncoded) {

    implicit request =>

      val credential = credentialForm.bindFromRequest.get

      credentialDB.save(credential).map(lastError =>
        Ok("Mongo LastError: %s".format(lastError))
      )

      for {
        responseA  <- WS.url("https://servicedesk.edusupportcenter.com/api/v1/login")
                      .withHeaders("Content-Type" -> "application/json", "Accept" -> "application/json").post(Json.toJson(credential))

        responseB  <- WS.url("https://servicedesk.edusupportcenter.com/api/v1/1609/case")
                      .withQueryString("token" -> (responseA.json \ "return_body" \ "token").as[String], "_pageSize_" -> "1000", "_queue_" -> "14").get()
      } yield {

        val items = (responseB.json \ "return_body" \ "items").as[List[Ticket]]

        // Always use the latest data set
        if(!items.isEmpty) {
          ticketDB.drop()
        }

        // Ensure unique
        val indexes: Index = Index(List("school_bucket" -> IndexType.Ascending,
          "school_number" ->IndexType.Ascending, "ticket_number" ->IndexType.Ascending), unique = true)

        ticketDB.indexesManager.ensure(indexes)

        ticketDB.bulkInsert(Enumerator.enumerate(items))(Ticket.ticketWrites, defaultContext)

        Ok(views.html.tickets("Hello world."))
      }

  }

  def getSchools = Action.async {
    implicit request => {

      // Get first ticket for each school
      val groupBy = Aggregate("tickets",
        Seq(
          GroupField("school_number")
            (
              "id" -> First("id"),
              "ticket_number" -> First("ticket_number"),
              "school_number" -> First("school_number"),
              "school_bucket" -> First("school_bucket"),
              "school_name" -> First("school_name"),
              "count" -> SumValue(1)
            ),
          Sort(List(Descending("count"))),
          Match(BSONDocument("school_bucket" -> "1"))
        )
      )

      db.command(groupBy).map(
        s => {
          val result = s.toList.map(doc => toJSON(doc))
          Ok(Json.toJson(result))
        }
      )

    }
  }

}