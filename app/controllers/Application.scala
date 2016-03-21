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

  def getProjectResults(projectId: String, offset: Int, limit: Int) = TODO

  def getUSer(userId : String) = TODO

  def getUserActivities(userId : String, offset : Int ,limit : Int) = TODO

  def getUserEnrolledProjects(userId: String, offset : Int , limit : Int) = TODO

  def getUserCreatedProjects(userId: String, offset : Int ,limit : Int) = TODO

  def getCategories(offset : Int ,limit : Int) = TODO

  def getCategory(categoryId : String) = TODO

  def enrollInProject() = TODO

  def WithdrawFromProject() = TODO
}
