package controllers

import play.api.mvc._

class Application extends Controller {

  def index = Action {
    Ok("Welcome to Sengab")
  }

  //  Project Requests

  //  list all projects (paginated)
  def listProjects(filter: String, offset: Int, limit: Int) = TODO

  //  get specific project
  def getProject(projectId: String) = TODO

  //  add project
  def addProject() = TODO

  //  search in projects (paginated)
  def searchProjects(keyword: String, offset: Int, limit: Int) = TODO


  //  list stats of a project
  def getProjectStats(projectId: String) = TODO

  // list results of a project (paginated)
  def getProjectResults(projectId: String, offset: Int, limit: Int) = TODO

  //  User Requests

  //  get user profile
  def getUSer(userId: String) = TODO

  //  list User’s Activity (paginated)
  def getUserActivities(userId: String, offset: Int, limit: Int) = TODO

  //  list all projects that the user enrolled in (paginated)
  def getUserEnrolledProjects(userId: String, offset: Int, limit: Int) = TODO

  //  list projects created by a specific user (paginated)
  def getUserCreatedProjects(userId: String, offset: Int, limit: Int) = TODO

  // Category Requests

  //  list all categories
  def getCategories(offset: Int, limit: Int) = TODO

  //  list projects of a category (paginated)
  def getProjectsForCategory(categoryId: String) = TODO

  //  Enrollment Requests

  //  enroll in a project
  def enrollInProject() = TODO

  //  withdraw from project
  def WithdrawFromProject() = TODO

  //  Contribution && Requests

  //  submit a contribution in a project
  def contributeInProject() = TODO

  //  Individuals Requests

  //  get more feed
  def getMoreFeed(projectId: String) = TODO
}
