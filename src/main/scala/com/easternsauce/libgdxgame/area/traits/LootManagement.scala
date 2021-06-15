package com.easternsauce.libgdxgame.area.traits

import com.easternsauce.libgdxgame.GameSystem._
import com.easternsauce.libgdxgame.area.Area
import com.easternsauce.libgdxgame.items.{Item, LootPile}

import scala.collection.mutable
import scala.collection.mutable.ListBuffer

trait LootManagement {
  this: Area =>

  var lootPileList: ListBuffer[LootPile] = ListBuffer()

  def spawnLootPile(x: Float, y: Float, dropTable: mutable.Map[String, Float]): Unit = {
    val lootPile = LootPile(this, x, y)
    for ((key, value) <- dropTable) {
      if (Random.nextFloat < value) {
        val item = Item.generateFromTemplate(key, Some(lootPile))
        lootPile.itemList += item
      }
    }
    if (lootPile.itemList.nonEmpty) lootPileList += lootPile
  }

  def spawnLootPile(x: Float, y: Float, item: Item): Unit = {
    val lootPile = LootPile(this, x, y)
    lootPile.itemList += item
    item.lootPile = Some(lootPile)
    lootPileList += lootPile
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
