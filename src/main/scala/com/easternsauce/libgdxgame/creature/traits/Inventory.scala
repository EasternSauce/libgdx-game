package com.easternsauce.libgdxgame.creature.traits

import com.easternsauce.libgdxgame.LibgdxGame
import com.easternsauce.libgdxgame.items.Item

import scala.collection.mutable

trait Inventory {
  val equipmentItems: mutable.Map[Int, Item] = mutable.Map()
  val inventoryItems: mutable.Map[Int, Item] = mutable.Map()

  def currentWeapon: Item = {
    equipmentItems(LibgdxGame.equipmentTypeIndices("weapon"))
  }

  def isWeaponEquipped: Boolean = {
    equipmentItems.contains(LibgdxGame.equipmentTypeIndices("weapon"))
  }
}
