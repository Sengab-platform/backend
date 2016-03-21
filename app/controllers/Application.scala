package controllers

import play.api.mvc._

class Application extends Controller {

  def index = Action {
    Ok("Welcome to Sengab")
  }

  def addProject() = TODO

  def listProjects(filter: String, offset: Int, limit: Int) = TODO

  def searchProjects(keyword: String, offset: Int, limit: Int) = TODO

  def getProject(projectId: String) = TODO

  def getProjectStats(projectId: String) = TODO

  def getProjectResults(project_id: String, offset: Int, limit: Int) = TODO

  def getUSer(user_id : String) = TODO

  def getUserActivities(user_id : String, offset : Int ,limit : Int) = TODO

  def getUserEnrolledProjects(user_id: String, offset : Int , limit : Int) = TODO

  def getUserCreatedProjects(user_id: String, offset : Int ,limit : Int) = TODO

  def getCategories(offset : Int ,limit : Int) = TODO

  def getCategory(category_id : String) = TODO
}
