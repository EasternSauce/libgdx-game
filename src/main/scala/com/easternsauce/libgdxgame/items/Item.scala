package com.easternsauce.libgdxgame.items

import com.easternsauce.libgdxgame.area.traits.TreasureSavedata
import com.easternsauce.libgdxgame.saving.ItemSavedata
import com.easternsauce.libgdxgame.system.GameSystem._

class Item private (
  val template: ItemTemplate,
  var quantity: Int = 1,
  val damage: Option[Int] = None,
  val armor: Option[Int] = None,
  var lootPile: Option[LootPile] = None
) {

  val name: String = template.name

  val description: String = template.description

  def getItemInformation(trader: Boolean): String = {
    if (trader)
      (if (this.damage.nonEmpty)
         "Damage: " + damage.get + "\n"
       else "") +
        (if (this.armor.nonEmpty)
           "Armor: " + armor.get + "\n"
         else "") +
        this.description +
        "\n" + "Worth " + template.worth.get + " Gold" + "\n"
    else
      (if (this.damage.nonEmpty)
         "Damage: " + damage.get + "\n"
       else "") +
        (if (this.armor.nonEmpty)
           "Armor: " + armor.get + "\n"
         else "") +
        this.description +
        "\n" + "Worth " + (template.worth.get * 0.3).toInt + " Gold" + "\n"
  }

}

object Item {
  def generateFromTemplate(templateId: String, lootPile: Option[LootPile] = None): Item = {
    val template = ItemTemplate.getItemTemplate(templateId)
    val damage = if (template.damage.nonEmpty) {
      Some(Math.ceil(template.damage.get * (0.75f + 0.25f * randomGenerator.nextFloat())).toInt)
    } else None

    val armor = if (template.armor.nonEmpty) {
      Some(Math.ceil(template.armor.get * (0.75f + 0.25f * randomGenerator.nextFloat())).toInt)
    } else None

    new Item(template, quantity = 1, damage = damage, armor = armor, lootPile = lootPile)
  }

  def loadFromSavedata(savedata: ItemSavedata): Item = {
    new Item(ItemTemplate.getItemTemplate(savedata.template), savedata.quantity, savedata.damage, savedata.armor)
  }

  def loadFromSavedata(savedata: TreasureSavedata): Item = {
    new Item(ItemTemplate.getItemTemplate(savedata.template), savedata.quantity, savedata.damage, savedata.armor)
  }
}
