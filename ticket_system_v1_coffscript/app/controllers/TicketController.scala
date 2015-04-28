package controllers

import controllers.Application._
import models._
import play.api.Play.current
import play.api.data.Form
import play.api.data.Forms._
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.json.Json
import play.api.libs.ws._
import play.api.mvc._

import scala.concurrent._
import scala.concurrent.duration.Duration

import reactivemongo.api._
import play.modules.reactivemongo.MongoController
import play.modules.reactivemongo.json.collection.JSONCollection

object TicketController extends Controller with MongoController{

  def index = Action {
    Ok(views.html.index("Hello world."))
  }

  val credentialForm: Form[Credential] = Form {
    mapping("username" -> text, "password" -> text)(Credential.apply)(Credential.unapply)
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