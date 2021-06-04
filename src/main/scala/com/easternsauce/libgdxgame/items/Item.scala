package com.easternsauce.libgdxgame.items

import com.easternsauce.libgdxgame.RpgGame
import com.easternsauce.libgdxgame.saving.ItemSavedata

class Item private (
  val template: ItemTemplate,
  val damage: Option[Int] = None,
  val armor: Option[Int] = None,
  var lootPile: Option[LootPile] = None
) {

  var quantity: Int = 0

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
  def generateFromTemplate(templateId: String): Item = {
    val template = ItemTemplate.getItemTemplate(templateId)
    val damage = if (template.damage.nonEmpty) {
      Some(Math.ceil(template.damage.get * (0.75f + 0.25f * RpgGame.Random.nextFloat())).toInt)
    } else None

    val armor = if (template.armor.nonEmpty) {
      Some(Math.ceil(template.armor.get * (0.75f + 0.25f * RpgGame.Random.nextFloat())).toInt)
    } else None

    new Item(template, damage, armor)
  }

  def loadFromSavedata(savedata: ItemSavedata): Item = {
    new Item(ItemTemplate.getItemTemplate(savedata.template), savedata.damage, savedata.armor)
  }
}
