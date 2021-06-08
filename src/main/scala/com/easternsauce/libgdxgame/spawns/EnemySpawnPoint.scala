package com.easternsauce.libgdxgame.spawns

import com.easternsauce.libgdxgame.area.Area
import com.easternsauce.libgdxgame.area.traits.EnemySpawnSavedata
import com.easternsauce.libgdxgame.creature.traits.Creature

class EnemySpawnPoint(
  val id: String,
  val area: Area,
  val posX: Float,
  val posY: Float,
  val creatureClass: String,
  val weaponType: Option[String]
) {

  var enemyCreature: Creature = _

}

object EnemySpawnPoint {

  def loadFromSavedata(area: Area, savedata: EnemySpawnSavedata): EnemySpawnPoint = {
    new EnemySpawnPoint(savedata.id, area, savedata.location.x, savedata.location.y, savedata.creatureClass, savedata.weaponType)
  }
}
