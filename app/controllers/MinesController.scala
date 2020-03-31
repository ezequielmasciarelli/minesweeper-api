package controllers

import javax.inject._
import play.api.libs.json.{Json, Reads, Writes}
import play.api.mvc._

import scala.util.Random

@Singleton
class MinesController @Inject()(cc: ControllerComponents) extends AbstractController(cc) {

  val random: Random.type = scala.util.Random
  var worldWithMines : List[MineField]
  var positionsWithMines : List[Int]
  case class MineField(hasMine:Boolean = false, discovered:Boolean = false, xPos:Int, yPos:Int)

    def initWorld : List[MineField] = {
      val allPositions = (1 to 100).toList
      positionsWithMines = random.shuffle(allPositions).take(20)
      allPositions.foldLeft(List.empty[MineField])((world,act) => {
        val xPos = act % 10
        val yPos = act / 10
        if (positionsWithMines.contains(act)) MineField(hasMine = true, discovered = false, xPos, yPos) :: world
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

  //returns a value that represent how far adjacents mines ARE (0,1,2,..)
  def calculateAdjacentMines(xPos: Int, yPos: Int): Int = {
    calculateAdjacentMinesRecursive(xPos,yPos,0)
  }

  def calculateAdjacentMinesRecursive(xPos: Int, yPos: Int, count: Int): Int = {
    val possibleCoordinates: List[(Int,Int)] = List((xPos-1,yPos),(xPos-1,yPos-1),(xPos-1,yPos+1),(xPos,yPos+1),(xPos,yPos-1),(xPos+1,yPos+1),(xPos+1,yPos),(xPos-1,yPos+1))
    val positionsWithMinesCord = positionsWithMines.map(each => {
      val xPos = each % 10
      val yPos = each / 10
      (xPos,yPos)
    })
    val matchedCoordinates = positionsWithMinesCord.intersect(possibleCoordinates)
    if(matchedCoordinates.nonEmpty) {
      count
    }
    else {
      calculateAdjacentMinesRecursive(xPos,yPos,count + 1)
    }
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
