package controllers

import controllers.TicketController._
import models._
import play.api.data.Form
import play.api.data.Forms._
import play.api.libs.json.{JsValue, JsArray, Json}
import play.api.libs.ws._
import play.api.mvc._

import scala.concurrent.Await
import scala.concurrent.duration.Duration
import scala.util.{Failure, Success}

import play.api.Play.current
import play.api.libs.concurrent.Execution.Implicits.defaultContext

object Application extends Controller {

  def index = Action {
    Ok(views.html.index("Hello world."))
  }

  val personForm: Form[Person] = Form {
    mapping("name" -> text)(Person.apply)(Person.unapply)
  }

  val credentialForm: Form[Credential] = Form {
    mapping("username" -> text, "password" -> text)(Credential.apply)(Credential.unapply)
  }

  def addPerson = Action {
    implicit request =>
      val person = personForm.bindFromRequest.get
      PersonDAO.save(person)
      Redirect(routes.Application.index())
  }

  def getPersons = Action {
    val persons = PersonDAO.query[Person].fetch
    Ok(Json.toJson(persons))
  }

  def login = Action.async(parse.urlFormEncoded) {
    implicit request =>

      val credential = credentialForm.bindFromRequest.get

      WS.url("https://servicedesk.edusupportcenter.com/api/v1/login")
        .withHeaders("Content-Type" -> "application/json", "Accept" -> "application/json")
        .post(Json.toJson(credential))
        .map(
            response => {
              val token = (response.json \ "return_body" \ "token").as[String]
              val result =  WS.url("https://servicedesk.edusupportcenter.com/api/v1/1609/case")
                              .withQueryString("token" -> token, "_pageSize_" -> "1000", "_queue_" -> "14").get()
                              .map(
                                response => {
                                  val tickets = (response.json \ "return_body" \ "items").as[List[Ticket]]
                                  Ok(Json.toJson(tickets)(Ticket.listWritesWithNewFields))
                                }
                              )
              Await.result(result, Duration.Inf)
            }
        )
  }

  def getCredential = Action {
    val credential = CredentialDAO.query[Credential].fetch
    Ok(Json.toJson(credential))
  }

  def getTickets = Action {
    Ok
  }

}