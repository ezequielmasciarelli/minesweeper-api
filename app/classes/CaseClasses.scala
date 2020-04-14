package classes

import play.api.libs.json.{Json, Reads, Writes}

object CaseClasses {
  case class PressPlaceRequest(xPos: Int, yPos:Int)
  case class MineResponse(xPos:Int, yPos:Int, neighborsWithMines:Int = 0)
  case class PressPlaceResponse(alive: Boolean, currentMine: MineField, neighborsDiscovered: List[MineField] = List.empty, win: Boolean = false)
  implicit val pressRequestRead: Reads[PressPlaceRequest] = Json.reads[PressPlaceRequest]
  implicit val mineFieldWrite: Writes[MineField] = Json.writes[MineField]
  implicit val pressResponseWrite: Writes[PressPlaceResponse] = Json.writes[PressPlaceResponse]
}
