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
  case class MineField(hasMine:Boolean = false, discovered:Boolean = false, coordinates: (Int,Int), neighborsWithMines:Int = 0) {

    private def getNeighborsCoordinates: List[(Int, Int)] =
      List((coordinates._1 - 1, coordinates._2),
          (coordinates._1 - 1, coordinates._2 - 1),
          (coordinates._1 - 1, coordinates._2 + 1),
          (coordinates._1, coordinates._2 + 1),
          (coordinates._1, coordinates._2 - 1),
          (coordinates._1 + 1, coordinates._2 + 1),
          (coordinates._1 + 1, coordinates._2),
          (coordinates._1 - 1, coordinates._2 + 1))

    def getNeighbors : List[MineField] = {
      getNeighborsCoordinates
        .flatMap(tuple => worldWithMines.find(_.coordinates == tuple))
    }


    def getMinesAroundMeCount : Int = getNeighbors.count(_.hasMine)

  }

  def initWorld : List[MineField] = {
    val allPositions = (1 to 100).toList
    positionsWithMines = random.shuffle(allPositions).take(20).map(each => (each % 10, each / 10))
    worldWithMines = allPositions.foldLeft(List.empty[MineField])((world, act) => {
      val coordinates = (act % 10, act / 10)
      if (positionsWithMines.contains(coordinates)) {
        MineField(hasMine = true, discovered = false, coordinates) :: world
      }
      else MineField(coordinates = coordinates) :: world
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
      .map(request => (request.xPos,request.yPos))
      .flatMap(coordinate => worldWithMines.find(_.coordinates == coordinate))
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
