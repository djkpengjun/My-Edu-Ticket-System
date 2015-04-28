package models


import play.api.libs.json._

import play.api.libs.json.Writes._

import play.api.libs.functional.syntax._


case class Ticket(id: Int, ticket_number: String, detail: String)

object Ticket {

  implicit val ticketFormat = Json.format[Ticket]

  implicit val ticketReader = Json.reads[Ticket]

  val writesWithNewField: Writes[Ticket] =
    (
      (__ \ "id").write[Int] ~
        (__ \ "ticket_number").write[String] ~
        (__ \ "school_bucket").write[String] ~
        (__ \ "school_number").write[String] ~
        (__ \ "school_name").write[String]
      )(
        (t: Ticket) => {

          // Do some parsing and transformation to extract school_id, school_name, school_bucket
          val bucket_pattern = """.*school_bucket:\s*(\d).*""".r
          val number_pattern = """.*school_id:\s*(\d+).*""".r
          val name_pattern = """.*school_name:\s*([^<]+).*""".r

          var school_bucket = "Missing"
          var school_number = "Missing"
          var school_name = "Missing"

          t.detail match {
            case bucket_pattern(school_match) => school_bucket = school_match
            case _ => 0
          }
          t.detail match {
            case number_pattern(school_match) => school_number = school_match
            case _ => 0
          }
          t.detail match {
            case name_pattern(school_match) => school_name = school_match
            case _ => 0
          }

          (t.id, t.ticket_number, school_bucket, school_number, school_name)
        }
      )

  val listWritesWithNewFields: Writes[List[Ticket]] = Writes.traversableWrites[Ticket](writesWithNewField)
}
