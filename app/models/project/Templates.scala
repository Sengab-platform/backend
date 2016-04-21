package models.project

import play.api.libs.json.Json

object Templates {


  case class TemplateOne(question_title: String) extends TemplateBody

  case class TemplateTwo(image_title: String) extends TemplateBody

  case class TemplateThree(questions: List[Question]) extends TemplateBody

  case class Question(id: String, title: String)

  case class TemplateFour(image_title: String) extends TemplateBody

  object TemplateOne {
    implicit val TemplateOneF = Json.format[TemplateOne]
  }

  object TemplateTwo {
    implicit val TemplateTwoF = Json.format[TemplateTwo]
  }

  object Question {
    implicit val questionF = Json.format[Question]
  }

  object TemplateThree {
    implicit val TemplateThreeF = Json.format[TemplateThree]
  }

  object TemplateFour {
    implicit val TemplateFourF = Json.format[TemplateFour]
  }


}
