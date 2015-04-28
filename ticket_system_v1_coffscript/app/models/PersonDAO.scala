package models

import sorm._

object PersonDAO extends Instance(entities = Seq(Entity[Person]()), url = "jdbc:h2:mem:test")