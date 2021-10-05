package com.easternsauce.libgdxgame.creature.traits

import com.easternsauce.libgdxgame.creature.Creature
import com.easternsauce.libgdxgame.items.Item
import com.easternsauce.libgdxgame.saving.{CreatureSavedata, ItemSavedata, PlayerSpawnPointSavedata, PositionSavedata}
import com.easternsauce.libgdxgame.system.GameSystem.{allAreaCreaturesMap, areaMap, setPlayer}
import com.softwaremill.quicklens.ModifyPimp

trait SavefileParser {
  this: Creature =>

  def saveToData(): CreatureSavedata = {
    CreatureSavedata(
      creatureClass = creatureType,
      id = id,
      spawnPointId = spawnPointId,
      life = life,
      area = area.get.id,
      isPlayer = isPlayer,
      position = PositionSavedata(pos.x, pos.y),
      playerSpawnPoint =
        if (playerSpawnPoint.nonEmpty)
          Some(PlayerSpawnPointSavedata(playerSpawnPoint.get.area.id, playerSpawnPoint.get.id))
        else None,
      inventoryItems = inventoryItems.map {
        case (index, item) => ItemSavedata(index, item.template.id, item.quantity, item.damage, item.armor)
      }.toList,
      equipmentItems = equipmentItems.map {
        case (index, item) => ItemSavedata(index, item.template.id, item.quantity, item.damage, item.armor)
      }.toList
    )
  }

  def loadFromSavedata(creatureData: CreatureSavedata): Creature = {

    val creature = assignToArea(areaMap(creatureData.area), creatureData.position.x, creatureData.position.y)

    if (creatureData.isPlayer) setPlayer(creature)


    creature.setPosition(creatureData.position.x, creatureData.position.y)
    creature.life = creatureData.life

    creature.spawnPointId = creatureData.spawnPointId

    if (creatureData.playerSpawnPoint.nonEmpty) {
      val area = areaMap(creatureData.playerSpawnPoint.get.area)
      creature.playerSpawnPoint = Some(area.playerSpawns.filter(_.id == creatureData.playerSpawnPoint.get.id).head)
    }



    creatureData.inventoryItems.foreach(item => creature.inventoryItems += (item.index -> Item.loadFromSavedata(item)))
    creatureData.equipmentItems.foreach(item => creature.equipmentItems += (item.index -> Item.loadFromSavedata(item)))

    allAreaCreaturesMap += (id -> this)

    creature
  }
}
