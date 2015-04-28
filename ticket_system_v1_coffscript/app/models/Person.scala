package models

import play.api.libs.json.Json

case class Person(name: String)

object Person {
  implicit val personalFormat = Json.format[Person]
}
