package controllers

import classes.MineField
import javax.inject._
import play.api.libs.json.{Json, Reads, Writes}
import play.api.mvc._
import services.World._

@Singleton
class MinesController @Inject()(cc: ControllerComponents) extends AbstractController(cc) {

  def newGame: Action[AnyContent] = Action {
    initGame()
    Ok
  }

  def home: Action[AnyContent] = Action {
    Ok(views.html.index("Minesweeper"))
  }

  case class PressPlaceRequest(xPos: Int, yPos:Int)
  case class MineResponse(xPos:Int, yPos:Int, neighborsWithMines:Int = 0)
  case class PressPlaceResponse(alive: Boolean, currentMine: MineField, neighborsDiscovered: List[MineField] = List.empty)
  implicit val pressRequestRead: Reads[PressPlaceRequest] = Json.reads[PressPlaceRequest]
  implicit val mineFieldWrite: Writes[MineField] = Json.writes[MineField]
  implicit val pressResponseWrite: Writes[PressPlaceResponse] = Json.writes[PressPlaceResponse]
  def pressPlace: Action[AnyContent] = Action { request =>
    request.body.asJson
      .map(_.as[PressPlaceRequest])
      .map(request => (request.xPos,request.yPos))
      .flatMap(coordinate => worldWithMines.find(_.coordinates == coordinate))
      .map(mineField => {
        if (mineField.hasMine) PressPlaceResponse(alive = false,currentMine = mineField)
        else {
          worldWithMines = worldWithMines.filterNot(_.equals(mineField))
          val mineFieldDiscovered = mineField.copy(discovered = true)
          worldWithMines = mineFieldDiscovered :: worldWithMines
          if (mineField.getMinesAroundMeCount == 0) {
            val discoveredNeighbors = mineField.discoverNeighbors
            PressPlaceResponse(alive = true, neighborsDiscovered = discoveredNeighbors, currentMine = mineField)
          }
          else PressPlaceResponse(alive = true,neighborsDiscovered = List.empty, currentMine = mineField)
        }
      })
      .map(Json.toJson(_))
      .map(result => Ok(result))
      .getOrElse(BadRequest)
    }

}
