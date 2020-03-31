package controllers

import javax.inject._
import play.api.libs.json.{Json, Reads, Writes}
import play.api.mvc._

import scala.util.Random

@Singleton
class MinesController @Inject()(cc: ControllerComponents) extends AbstractController(cc) {

  val random: Random.type = scala.util.Random
  var worldWithMines : List[MineField] = initWorld
  case class MineField(hasMine:Boolean = false, discovered:Boolean = false, xPos:Int, yPos:Int)

    def initWorld : List[MineField] = {
      val allPositions = (1 to 100).toList
      val positionsWithMines = random.shuffle(allPositions).take(20)
      allPositions.foldLeft(List.empty[MineField])((world,act) => {
        val xPos = act / 10
        val yPos = act % 10
        if (positionsWithMines.contains(act)) MineField(hasMine = true, discovered = false, xPos, yPos) :: world
        else MineField(xPos = xPos,yPos = yPos) :: world
      })
    }

  def newGame: Action[AnyContent] = Action {
    worldWithMines = initWorld
    Ok("Juego Nuevo")
  }


  case class PressPlaceRequest(xPos: Int, yPos:Int)
  case class PressPlaceResponse(result: String)
  implicit val pressRequestRead: Reads[PressPlaceRequest] = Json.reads[PressPlaceRequest]
  implicit val pressResponseWrite: Writes[PressPlaceResponse] = Json.writes[PressPlaceResponse]
  def pressPlace: Action[AnyContent] = Action { request =>
    request.body.asJson
      .map(_.as[PressPlaceRequest])
      .map(request => {
        val hasMine = worldWithMines
          .filter(_.xPos == request.xPos)
          .find(_.yPos == request.yPos)
          .exists(_.hasMine)
        if (hasMine) PressPlaceResponse("BOOM!")
        else PressPlaceResponse("Safaste!")
      })
      .map(Json.toJson(_))
      .map(result => Ok(result))
      .getOrElse(BadRequest)
    }

}
