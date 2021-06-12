package com.easternsauce.libgdxgame.creature.traits

import com.easternsauce.libgdxgame.RpgGame
import com.easternsauce.libgdxgame.items.{Item, ItemTemplate}

import scala.collection.mutable

trait Inventory {
  val equipmentItems: mutable.Map[Int, Item] = mutable.Map()
  val inventoryItems: mutable.Map[Int, Item] = mutable.Map()

  val game: RpgGame

  def currentWeapon: Item = {
    equipmentItems(RpgGame.equipmentTypeIndices("weapon"))
  }

  def isWeaponEquipped: Boolean = {
    equipmentItems.contains(RpgGame.equipmentTypeIndices("weapon"))
  }

  def tryPickUpItem(item: Item): Boolean = {
    val template: ItemTemplate = item.template
    val stackable: Boolean = template.stackable.get

    if (stackable) {
      var foundFreeSlot: Int = -1
      inventoryItems.foreach {
        case (key, value) =>
          if (foundFreeSlot == -1 && (value.template == template)) {
            // stackable and same type item exists in inventory
            foundFreeSlot = key
            inventoryItems(foundFreeSlot).quantity =
              inventoryItems(foundFreeSlot).quantity + item.quantity

            return true
          }
      }
    }
    for (i <- 0 until game.inventoryWindow.inventoryTotalSlots) {
      val lootPile = item.lootPile.get

      if (!inventoryItems.contains(i)) { // if slot empty
        inventoryItems += (i -> item)
        lootPile match {
//          case treasure: Treasure => //register treasure picked up, dont spawn it again for this save
//            try {
//              val writer: FileWriter =
//                new FileWriter("saves/treasure_collected.sav", true)
//              val area: Area = item.lootPileBackref.area
//              writer.write(
//                "treasure " + area.id + " " + area.treasureList
//                  .indexOf(treasure) + "\n"
//              )
//              writer.close()
//            } catch {
//              case e: IOException =>
//                e.printStackTrace()
//            } TODO treasure saves
          case _ =>
        }

        return true
      }
    }
    false
  }
}
