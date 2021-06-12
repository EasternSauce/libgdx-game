package com.easternsauce.libgdxgame.area.traits

import com.easternsauce.libgdxgame.RpgGame
import com.easternsauce.libgdxgame.area.Area
import com.easternsauce.libgdxgame.items.{Item, LootPile}

import scala.collection.mutable
import scala.collection.mutable.ListBuffer

trait LootManagement {
  var lootPileList: ListBuffer[LootPile] = ListBuffer()

  def spawnLootPile(area: Area, x: Float, y: Float, dropTable: mutable.Map[String, Float]): Unit = {
    val lootPile = LootPile(area, x, y)
    for ((key, value) <- dropTable) {
      if (RpgGame.Random.nextFloat < value) {
        val item = Item.generateFromTemplate(key, Some(lootPile))
        lootPile.itemList += item
      }
    }
    if (lootPile.itemList.nonEmpty) area.lootPileList += lootPile
  }

  def updateLoot(): Unit = {
    lootPileList.foreach(
      lootPile =>
        if (!lootPile.bodyCreated) {
          lootPile.initBody()
          lootPile.bodyCreated = true
        }
    )

  }
}
