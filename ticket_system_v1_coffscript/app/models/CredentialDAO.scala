package models

import sorm._

object CredentialDAO extends Instance(entities = Seq(Entity[Credential]()), url = "jdbc:h2:mem:test")
