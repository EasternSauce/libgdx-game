package com.easternsauce.libgdxgame.creature.traits

import com.easternsauce.libgdxgame.items.Item

import scala.collection.mutable

trait Inventory {
  val equipmentItems: mutable.Map[Int, Item] = mutable.Map()
  val inventoryItems: mutable.Map[Int, Item] = mutable.Map()

}
