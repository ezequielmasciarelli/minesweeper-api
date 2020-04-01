package services

import classes.MineField

import scala.util.Random

object World {
  val random: Random.type = scala.util.Random

  var worldWithMines : List[MineField] = _
  var positionsWithMines : List[(Int,Int)] = _

  def initGame() : Unit = {
    val allPositions = (0 to 99).toList
    positionsWithMines = random.shuffle(allPositions).take(10).map(each => (each % 10, each / 10))
    worldWithMines = allPositions.foldLeft(List.empty[MineField])((world, act) => {
      val coordinates = (act % 10, act / 10)
      if (positionsWithMines.contains(coordinates)) {
        MineField(hasMine = true, discovered = false, coordinates) :: world
      }
      else MineField(coordinates = coordinates) :: world
    })
    worldWithMines = worldWithMines.map(each => each.copy(neighborsWithMines = each.getMinesAroundMeCount))
  }
}
