package controllers

import javax.inject._
import play.api.mvc._
import scala.util.Random

@Singleton
class MinesController @Inject()(cc: ControllerComponents) extends AbstractController(cc) {

  val random: Random.type = scala.util.Random

  def newGame: Action[AnyContent] = Action {
    val worldWithMines = (0 to 20).foldLeft(Array.ofDim[Boolean](100))((world,_) => {
      val indexOfPlacesWithoutMines = world.zipWithIndex.filter(_._1 == false).map(_._2).toList
      val newMinePos = random.shuffle(indexOfPlacesWithoutMines).head
      world(newMinePos) = true
      world
    })
    Ok
  }

}
