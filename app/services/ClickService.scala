package services

import classes.CaseClasses.PressPlaceResponse
import classes.MineField
import services.World.worldWithMines

object ClickService {

  private val die: MineField => PressPlaceResponse = mineField => PressPlaceResponse(alive = false, currentMine = mineField)

  private val live: MineField => PressPlaceResponse = mineField => {
    worldWithMines = worldWithMines.filterNot(_.equals(mineField))
    val mineFieldDiscovered = mineField.copy(discovered = true)
    worldWithMines = mineFieldDiscovered :: worldWithMines
    val discoveredNeighbors = mineField.discoverNeighbors
    if (worldWithMines.filterNot(_.discovered).forall(_.hasMine))
      PressPlaceResponse(alive = true, neighborsDiscovered = discoveredNeighbors, currentMine = mineField, win = true)
    else
      PressPlaceResponse(alive = true, neighborsDiscovered = discoveredNeighbors, currentMine = mineField)
  }

  val click: MineField => PressPlaceResponse = mineField => {
    if (mineField.hasMine) die(mineField)
    else live(mineField)
  }

}
