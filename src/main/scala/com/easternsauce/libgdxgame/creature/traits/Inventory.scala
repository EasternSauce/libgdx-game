package com.easternsauce.libgdxgame.creature.traits

import com.easternsauce.libgdxgame.creature.Creature
import com.easternsauce.libgdxgame.items.{Item, ItemTemplate}
import com.easternsauce.libgdxgame.system.GameSystem._
import com.easternsauce.libgdxgame.system.InventoryMapping

import scala.collection.mutable

trait Inventory {
  this: Creature =>

  val equipmentItems: mutable.Map[Int, Item] = mutable.Map()
  val inventoryItems: mutable.Map[Int, Item] = mutable.Map()

  def currentWeapon: Item = {
    equipmentItems(InventoryMapping.primaryWeaponIndex)
  }

  def isWeaponEquipped: Boolean = {
    equipmentItems.contains(InventoryMapping.primaryWeaponIndex)
  }

  def tryPickUpItem(item: Item): Boolean = {
    val template: ItemTemplate = item.template
    val stackable: Boolean = template.stackable.get

    if (stackable) {
      var foundFreeSlot: Int = -1
      equipmentItems.foreach {
        case (key, value) =>
          if (foundFreeSlot == -1 && (value.template == template)) {
            // stackable and same type item exists in inventory
            foundFreeSlot = key
            equipmentItems(foundFreeSlot).quantity =
              equipmentItems(foundFreeSlot).quantity + item.quantity

            return true
          }
      }
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
    for (i <- 0 until inventoryWindow.inventoryTotalSlots) {
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

  def useItem(item: Item): Unit = {
    item.template.id match {
      case "healingPowder" =>
        startHealing(0.14f)
        onItemConsumeSound.play(0.5f)
    }
  }

  def swapPrimaryAndSecondaryWeapons(): Unit = {
    if (equipmentItems.contains(InventoryMapping.secondaryWeaponIndex)) {
      val primaryWeapon = equipmentItems(InventoryMapping.primaryWeaponIndex)
      val secondaryWeapon = equipmentItems(InventoryMapping.secondaryWeaponIndex)

      equipmentItems(InventoryMapping.secondaryWeaponIndex) = primaryWeapon
      equipmentItems(InventoryMapping.primaryWeaponIndex) = secondaryWeapon

    }
  }

  def promoteSecondaryToPrimaryWeapon(): Unit = {
    if (
      !equipmentItems
        .contains(InventoryMapping.primaryWeaponIndex) &&
      equipmentItems
        .contains(InventoryMapping.secondaryWeaponIndex)
    ) {
      equipmentItems(InventoryMapping.primaryWeaponIndex) = equipmentItems(InventoryMapping.secondaryWeaponIndex)
      equipmentItems.remove(InventoryMapping.secondaryWeaponIndex)
    }
  }
}
