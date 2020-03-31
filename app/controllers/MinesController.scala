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
    def getNeighbors : List[MineField] = {
      val neighborsPositions: scala.List[(Int, Int)] = getNeighborsCoordinates(xPos, yPos)
      val neighbors = neighborsPositions.flatMap(tuple => {
        val (xPos, yPos) = tuple
        worldWithMines.find(mine => mine.xPos == xPos && mine.yPos == yPos)
      })
      neighbors
    }

  }

  def calculateNeighborsWithMinesCount(xPos: Int, yPos: Int): Int = {
    intersectNeighborsWithPositionWithMines(xPos, yPos).length
  }

  def initWorld : List[MineField] = {
      val allPositions = (1 to 100).toList
      positionsWithMines = random.shuffle(allPositions).take(20).map(each => (each % 10, each / 10))
      allPositions.foldLeft(List.empty[MineField])((world,act) => {
        val xPos = act % 10
        val yPos = act / 10
        if (positionsWithMines.contains((xPos,yPos))) {
          val neighborsWithMinesCount = calculateNeighborsWithMinesCount(xPos,yPos)
          MineField(hasMine = true, discovered = false, xPos, yPos, neighborsWithMinesCount) :: world
        }
        else MineField(xPos = xPos,yPos = yPos) :: world
      })
    }

  def newGame: Action[AnyContent] = Action {
    worldWithMines = initWorld
    Ok("Juego Nuevo")
  }


  case class PressPlaceRequest(xPos: Int, yPos:Int)
  case class PressPlaceResponse(alive: Boolean, revealAdjacentMinesSquare: Int)
  implicit val pressRequestRead: Reads[PressPlaceRequest] = Json.reads[PressPlaceRequest]
  implicit val pressResponseWrite: Writes[PressPlaceResponse] = Json.writes[PressPlaceResponse]

  //todo - solo calcula el primer nivel de adyacencia
  //returns a value that represent how far adjacents mines ARE (0,1,2,..)
  def calculateAdjacentMines(xPos: Int, yPos: Int): Int = {
    val currentMine = worldWithMines
      .filter(_.xPos == xPos)
      .filter(_.yPos == yPos)
      .head
    val minesAround = currentMine.getNeighbors.foldLeft(0)((acc,mine) => acc + mine.neighborsWithMines)
    if(minesAround == 0) 1
    else 0
  }

  //Esta funcion hace un AND entre las listas de vecinos con la listas de los lugares donde estan las minas
  private def intersectNeighborsWithPositionWithMines(xPos: Int, yPos: Int): List[(Int, Int)] = {
    val possibleCoordinates: scala.List[(Int, Int)] = getNeighborsCoordinates(xPos, yPos)
    positionsWithMines.intersect(possibleCoordinates)
  }

  private def getNeighborsCoordinates(xPos: Int, yPos: Int) = {
    val possibleCoordinates: List[(Int, Int)] = List((xPos - 1, yPos), (xPos - 1, yPos - 1), (xPos - 1, yPos + 1), (xPos, yPos + 1), (xPos, yPos - 1), (xPos + 1, yPos + 1), (xPos + 1, yPos), (xPos - 1, yPos + 1))
    possibleCoordinates
  }

  def pressPlace: Action[AnyContent] = Action { request =>
    request.body.asJson
      .map(_.as[PressPlaceRequest])
      .map(request => {
        val hasMine = worldWithMines
          .filter(_.xPos == request.xPos)
          .find(_.yPos == request.yPos)
          .exists(_.hasMine)
        if (hasMine) PressPlaceResponse(alive = false,0)
        else {
          val result : Int = calculateAdjacentMines(request.xPos,request.yPos)
          PressPlaceResponse(alive = true,result)
        }
      })
      .map(Json.toJson(_))
      .map(result => Ok(result))
      .getOrElse(BadRequest)
    }

}
