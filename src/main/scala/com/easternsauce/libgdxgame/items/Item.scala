package com.easternsauce.libgdxgame.items

import com.easternsauce.libgdxgame.LibgdxGame

class Item(val template: ItemTemplate, var lootPile: Option[LootPile] = None) {

  var damage: Option[Int] = None
  var armor: Option[Int] = None
  var quantity: Int = 0

  val name: String = template.name

  val description: String = template.description

  if (template.damage.nonEmpty) {
    damage = Some(Math.ceil(template.damage.get * (0.75f + 0.25f * LibgdxGame.Random.nextFloat())).toInt)
  }

  if (template.armor.nonEmpty) {
    armor = Some(Math.ceil(template.armor.get * (0.75f + 0.25f * LibgdxGame.Random.nextFloat())).toInt)
  }

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
