package controllers

import classes.CaseClasses.PressPlaceRequest
import javax.inject._
import play.api.libs.json.Json
import play.api.mvc._
import services.ClickService
import services.World._

@Singleton
class MinesController @Inject()(cc: ControllerComponents) extends AbstractController(cc) {

  def newGame: Action[AnyContent] = Action {
    worldWithMines = initGame()
    Ok
  }

  def home: Action[AnyContent] = Action {
    Ok(views.html.index("Minesweeper"))
  }

  def pressPlace: Action[AnyContent] = Action { request =>
    request.body.asJson
      .map(_.as[PressPlaceRequest])
      .map(request => (request.xPos, request.yPos))
      .flatMap(coordinates => worldWithMines.find(_.coordinates == coordinates))
      .map(ClickService.click)
      .map(Json.toJson(_))
      .map(result => Ok(result))
      .getOrElse(BadRequest)
    }

}
