package controllers

import javax.inject._
import play.api.libs.json.{Json, Reads, Writes}
import play.api.mvc._

import scala.util.Random

@Singleton
class MinesController @Inject()(cc: ControllerComponents) extends AbstractController(cc) {

  val random: Random.type = scala.util.Random
  var worldWithMines : List[Boolean] = initWorld
  var worldWithMines2 : List[Boolean] = initWorld
  case class MineField(hasMine: Boolean = false, discovered: Boolean = false)

    def initWorld : List[Boolean] = {
      (0 to 20).foldLeft(Array.ofDim[Boolean](100))((world, _) => {
        val indexOfPlacesWithoutMines = world.zipWithIndex.filter(_._1 == false).map(_._2).toList
        val newMinePos = random.shuffle(indexOfPlacesWithoutMines).head
        world(newMinePos) = true
        world
      }).toList
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
      .map(request => request.xPos + request.yPos * 10)
      .map(worldPosition => {
        if (worldWithMines(worldPosition)) PressPlaceResponse("BOOM!")
        else PressPlaceResponse("Safaste!")
      })
      .map(Json.toJson(_))
      .map(result => Ok(result))
      .getOrElse(BadRequest)
    }

}
