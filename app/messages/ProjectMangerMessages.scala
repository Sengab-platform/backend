package messages

import models.models.Project

object ProjectMangerMessages {

  trait ProjectMessage

  case class CreateProject(p: Project) extends ProjectMessage

}
