package com.easternsauce.libgdxgame.spawns

import com.badlogic.gdx.math.Vector2
import com.easternsauce.libgdxgame.area.Area
import com.easternsauce.libgdxgame.area.traits.EnemySpawnSavedata

class EnemySpawnPoint(
  val id: String,
  val area: Area,
  val posX: Float,
  val posY: Float,
  val creatureClass: String,
  val weaponType: Option[String],
  val blockades: List[Vector2]
)

object EnemySpawnPoint {

  def loadFromSavedata(area: Area, savedata: EnemySpawnSavedata): EnemySpawnPoint = {
    new EnemySpawnPoint(
      savedata.id,
      area,
      savedata.location.x,
      savedata.location.y,
      savedata.creatureClass,
      savedata.weaponType,
      savedata.blockades match {
        case Some(blockades) => blockades.map(blockade => new Vector2(blockade.x, blockade.y))
        case None            => List()
      }
    )
  }
}
