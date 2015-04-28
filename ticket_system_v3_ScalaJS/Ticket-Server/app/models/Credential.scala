package models

import play.api.libs.json.Json

case class Credential(username: String, password: String)

object Credential {
  implicit val credentialFormat = Json.format[Credential]
}