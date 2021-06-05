package com.easternsauce.libgdxgame.spawns

import com.easternsauce.libgdxgame.area.traits.EnemySpawnSavedata
import com.easternsauce.libgdxgame.creature.traits.Creature

class EnemySpawnPoint(val posX: Float, val posY: Float, val creatureClass: String, val weaponType: Option[String]) {

  var enemyCreature: Creature = _

}

object EnemySpawnPoint {

  def loadFromSavedata(savedata: EnemySpawnSavedata): EnemySpawnPoint = {
    new EnemySpawnPoint(savedata.location.x, savedata.location.y, savedata.creatureClass, savedata.weaponType)
  }
}
