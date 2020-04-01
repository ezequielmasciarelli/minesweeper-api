package controllers

import javax.inject._
import play.api.libs.json.{Json, Reads, Writes}
import play.api.mvc._

import scala.collection.mutable
import scala.collection.mutable.ListBuffer
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
          (coordinates._1 + 1, coordinates._2 - 1))

    def getNeighbors : List[MineField] =
      getNeighborsCoordinates
        .flatMap(tuple => worldWithMines.find(_.coordinates == tuple))

    def getMinesAroundMeCount : Int = getNeighbors.count(_.hasMine)

    def discoverNeighbors: List[MineField] = {
      val neighbors: mutable.ListBuffer[MineField] = ListBuffer()
      discoverOtherNeighbors(neighbors)
      neighbors.toList
    }

    private def discoverOtherNeighbors(neighbors: ListBuffer[MineField]) : Unit = {
      val neighborsCords = getNeighbors.map(_.coordinates)
      val myNeighbors = worldWithMines
        .filter(mine => neighborsCords.contains(mine.coordinates))
        .filterNot(_.discovered)
      val discoveredNeighbors = myNeighbors
        .map(_.copy(discovered = true))
      neighbors ++= discoveredNeighbors
      worldWithMines = worldWithMines.filterNot(myNeighbors.contains(_))
      worldWithMines ++= discoveredNeighbors
      discoveredNeighbors
        .filter(_.neighborsWithMines == 0)
        .foreach(_.discoverOtherNeighbors(neighbors))
      neighbors.toList
    }

  }

  def initWorld : List[MineField] = {
    val allPositions = (1 to 100).toList
    positionsWithMines = random.shuffle(allPositions).take(5).map(each => (each % 10, each / 10))
    worldWithMines = allPositions.foldLeft(List.empty[MineField])((world, act) => {
      val coordinates = (act % 10, act / 10)
      if (positionsWithMines.contains(coordinates)) {
        MineField(hasMine = true, discovered = false, coordinates) :: world
      }
      else MineField(coordinates = coordinates) :: world
    })
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
          worldWithMines = worldWithMines.filterNot(_.equals(mineField))
          val mineFieldDiscovered = mineField.copy(discovered = true)
          worldWithMines = mineFieldDiscovered :: worldWithMines
          if (mineField.getMinesAroundMeCount == 0) {
            val discoveredNeighbors = mineField.discoverNeighbors
            PressPlaceResponse(alive = true, neighborsDiscovered = mineFieldDiscovered :: discoveredNeighbors)
          }
          else PressPlaceResponse(alive = true,neighborsDiscovered = List(mineFieldDiscovered))
        }
      })
      .map(Json.toJson(_))
      .map(result => Ok(result))
      .getOrElse(BadRequest)
    }

}
