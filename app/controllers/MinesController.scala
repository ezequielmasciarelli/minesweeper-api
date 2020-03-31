package controllers

import javax.inject._
import play.api.libs.json.{Json, Reads, Writes}
import play.api.mvc._

import scala.util.Random

@Singleton
class MinesController @Inject()(cc: ControllerComponents) extends AbstractController(cc) {

  val random: Random.type = scala.util.Random
  var worldWithMines : List[MineField] = _
  var positionsWithMines : List[(Int,Int)] = _
  case class MineField(hasMine:Boolean = false, discovered:Boolean = false, xPos:Int, yPos:Int, neighborsWithMines:Int = 0) {

    private def getNeighborsCoordinates(xPos: Int, yPos: Int): List[(Int, Int)] = {
      val possibleCoordinates: List[(Int, Int)] = List((xPos - 1, yPos), (xPos - 1, yPos - 1), (xPos - 1, yPos + 1), (xPos, yPos + 1), (xPos, yPos - 1), (xPos + 1, yPos + 1), (xPos + 1, yPos), (xPos - 1, yPos + 1))
      possibleCoordinates
    }

    def getNeighbors : List[MineField] = {
      val neighborsPositions: List[(Int, Int)] = getNeighborsCoordinates(xPos, yPos)
      val neighbors = neighborsPositions.flatMap(tuple => {
        val (xPos, yPos) = tuple
        worldWithMines.find(mine => mine.xPos == xPos && mine.yPos == yPos)
      })
      neighbors
    }


    def getMinesAroundMeCount : Int = getNeighbors.count(_.hasMine)

  }

  def initWorld : List[MineField] = {
    val allPositions = (1 to 100).toList
    positionsWithMines = random.shuffle(allPositions).take(20).map(each => (each % 10, each / 10))
    worldWithMines = allPositions.foldLeft(List.empty[MineField])((world, act) => {
      val xPos = act % 10
      val yPos = act / 10
      if (positionsWithMines.contains((xPos, yPos))) {
        MineField(hasMine = true, discovered = false, xPos, yPos) :: world
      }
      else MineField(xPos = xPos, yPos = yPos) :: world
    })
    //Esto se debe hacer al final para poder inicializar la cantidad de minas en el minefield, se podria calcular en runtime tambien
    worldWithMines.map(each => each.copy(neighborsWithMines = each.getMinesAroundMeCount))
  }

  def newGame: Action[AnyContent] = Action {
    worldWithMines = initWorld
    Ok("Juego Nuevo")
  }


  case class PressPlaceRequest(xPos: Int, yPos:Int)
  case class MineResponse(xPos:Int, yPos:Int, neighborsWithMines:Int = 0)
  case class PressPlaceResponse(alive: Boolean, neighborsDiscovered: List[MineField] = List.empty)
  implicit val pressRequestRead: Reads[PressPlaceRequest] = Json.reads[PressPlaceRequest]
  implicit val mineFieldWrite: Writes[MineField] = Json.writes[MineField]
  implicit val pressResponseWrite: Writes[PressPlaceResponse] = Json.writes[PressPlaceResponse]

  def pressPlace: Action[AnyContent] = Action { request =>
    request.body.asJson
      .map(_.as[PressPlaceRequest])
      .flatMap(request => {
        worldWithMines
          .filter(_.xPos == request.xPos)
          .find(_.yPos == request.yPos)
      })
      .map(mineField => {
        if (mineField.hasMine) PressPlaceResponse(alive = false)
        else {
          if (mineField.getMinesAroundMeCount == 0) {
            PressPlaceResponse(alive = true, neighborsDiscovered = mineField.getNeighbors)
          }
          else PressPlaceResponse(alive = true)
        }
      })
      .map(Json.toJson(_))
      .map(result => Ok(result))
      .getOrElse(BadRequest)
    }

}
