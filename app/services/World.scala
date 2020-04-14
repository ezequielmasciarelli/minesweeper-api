package services

import classes.MineField
import scala.math.Integral.Implicits._

object World {
  var worldWithMines : List[MineField] = _

  def initGame() : List[MineField] = {
    val allPositions = (0 to 99).toList
    val toCoordinates : Int => (Int,Int) = _ /% 10
    val coordinatesWithMines = scala.util.Random.shuffle(allPositions).take(10).map(toCoordinates)
    val iHaveAMine : ((Int,Int)) => Boolean = coordinatesWithMines.contains
    allPositions.foldLeft(List.empty[MineField])((world, act) => {
      val coordinates: (Int, Int) = toCoordinates(act)
      val neighborsWithMinesCount: Int = MineField.getNeighborsCoordinates(coordinates).intersect(coordinatesWithMines).length
      if (iHaveAMine(coordinates)) MineField(hasMine = true, coordinates = coordinates,neighborsWithMines = neighborsWithMinesCount) :: world
      else MineField(coordinates = coordinates, neighborsWithMines = neighborsWithMinesCount) :: world
    })
  }
}
