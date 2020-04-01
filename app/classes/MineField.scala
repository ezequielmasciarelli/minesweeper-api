package classes

import services.World._

import scala.collection.mutable
import scala.collection.mutable.ListBuffer

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
