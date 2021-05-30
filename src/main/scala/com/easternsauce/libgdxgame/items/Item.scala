package com.easternsauce.libgdxgame.items

class Item(val template: ItemTemplate, var lootPile: Option[LootPile] = None) {

  var damage: Option[Float] = None
  var armor: Option[Float] = None
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
