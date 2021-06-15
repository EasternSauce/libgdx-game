package com.easternsauce.libgdxgame.items

import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.easternsauce.libgdxgame.system.Assets

import scala.collection.mutable

class ItemTemplate(val id: String, val name: String, val description: String, val textureRegion: TextureRegion) {

  val parameters: mutable.Map[String, ItemParameterValue] =
    mutable.Map(
      "stackable" -> ItemParameterValue(false),
      "consumable" -> ItemParameterValue(false),
      "equipable" -> ItemParameterValue(false),
      "equipableType" -> ItemParameterValue(),
      "worth" -> ItemParameterValue(0),
      "damage" -> ItemParameterValue(),
      "armor" -> ItemParameterValue(),
      "weaponSpeed" -> ItemParameterValue(),
      "poisonChance" -> ItemParameterValue(),
      "attackType" -> ItemParameterValue()
    )

  def worth: Option[Int] = parameters("worth").intValue

  def armor: Option[Int] = parameters("armor").intValue

  def damage: Option[Int] = parameters("damage").intValue

  def weaponSpeed: Option[Float] = parameters("weaponSpeed").floatValue

  def attackType: Option[String] = parameters("attackType").stringValue

  def stackable: Option[Boolean] = parameters("stackable").boolValue

  def consumable: Option[Boolean] = parameters("consumable").boolValue

  def setWorth(worth: Int): ItemTemplate = {
    parameters("worth") = ItemParameterValue(worth)

    this
  }

  def setEquipable(equipable: Boolean): ItemTemplate = {
    parameters("equipable") = ItemParameterValue(equipable)

    this
  }

  def setEquipableType(equipableType: String): ItemTemplate = {
    parameters("equipableType") = ItemParameterValue(equipableType)

    this
  }

  def setArmor(armor: Int): ItemTemplate = {
    parameters("armor") = ItemParameterValue(armor)

    this
  }

  def setDamage(damage: Int): ItemTemplate = {
    parameters("damage") = ItemParameterValue(damage)

    this
  }

  def setAttackType(attackType: String): ItemTemplate = {
    parameters("attackType") = ItemParameterValue(attackType)

    this
  }

  def setPoisonChance(poisonChance: Float): ItemTemplate = {
    parameters("poisonChance") = ItemParameterValue(poisonChance)

    this
  }

  def setStackable(stackable: Boolean): ItemTemplate = {
    parameters("stackable") = ItemParameterValue(stackable)

    this
  }

  def setConsumable(consumable: Boolean): ItemTemplate = {
    parameters("consumable") = ItemParameterValue(consumable)

    this
  }

}

object ItemTemplate {

  def apply(id: String, name: String, description: String, textureRegion: TextureRegion) =
    new ItemTemplate(id, name, description, textureRegion)

  private val itemTemplates: mutable.Map[String, ItemTemplate] = mutable.Map()

  private def addItemType(itemTemplate: ItemTemplate): Unit = {
    itemTemplates.put(itemTemplate.id, itemTemplate)
  }

  def loadItemTemplates(): Unit = {
    val icons: Array[Array[TextureRegion]] = Assets.atlas.findRegion("nice_icons").split(32, 32)

    addItemType(
      ItemTemplate("leatherArmor", "Leather Armor", "-", icons(7)(8))
        .setWorth(150)
        .setEquipable(true)
        .setEquipableType("body")
        .setArmor(13)
    )

    addItemType(
      ItemTemplate("ringmailGreaves", "Ringmail Greaves", "-", icons(8)(3))
        .setWorth(50)
        .setEquipable(true)
        .setEquipableType("boots")
        .setArmor(7)
    )

    addItemType(
      ItemTemplate("hideGloves", "Hide Gloves", "-", icons(8)(0))
        .setWorth(70)
        .setEquipable(true)
        .setEquipableType("gloves")
        .setArmor(5)
    )

    addItemType(
      ItemTemplate("crossbow", "Crossbow", "-", icons(6)(4))
        .setWorth(500)
        .setEquipable(true)
        .setEquipableType("weapon")
        .setDamage(45)
        .setAttackType("bow")
    )

    addItemType(
      ItemTemplate("ironSword", "Iron Sword", "-", icons(5)(2))
        .setWorth(100)
        .setEquipable(true)
        .setEquipableType("weapon")
        .setDamage(60)
        .setAttackType("sword")
    )

    addItemType(
      ItemTemplate("woodenSword", "Wooden Sword", "-", icons(5)(0))
        .setWorth(70)
        .setEquipable(true)
        .setEquipableType("weapon")
        .setDamage(45)
        .setAttackType("sword")
    )

    addItemType(
      ItemTemplate("leatherHelmet", "Leather Helmet", "-", icons(7)(2))
        .setWorth(80)
        .setEquipable(true)
        .setEquipableType("helmet")
        .setArmor(9)
    )

    addItemType(
      ItemTemplate("lifeRing", "Life Ring", "Increases life when worn", icons(8)(5))
        .setWorth(1000)
        .setEquipable(true)
        .setEquipableType("ring")
    )

    addItemType(
      ItemTemplate("poisonDagger", "Poison Dagger", "-", icons(5)(6))
        .setWorth(500)
        .setEquipable(true)
        .setEquipableType("weapon")
        .setDamage(40)
        .setAttackType("sword")
        .setPoisonChance(0.5f)
    )

    addItemType(
      ItemTemplate("healingPowder", "Healing Powder", "Quickly regenerates life", icons(20)(5))
        .setWorth(45)
        .setEquipable(true)
        .setEquipableType("consumable")
        .setStackable(true)
        .setConsumable(true)
    )

    addItemType(
      ItemTemplate("trident", "Trident", "-", icons(5)(8))
        .setWorth(900)
        .setEquipable(true)
        .setEquipableType("weapon")
        .setDamage(85)
        .setAttackType("trident")
    )

    addItemType(
      ItemTemplate("steelArmor", "Steel Armor", "-", icons(7)(4))
        .setWorth(200)
        .setEquipable(true)
        .setEquipableType("body")
        .setArmor(20)
    )

    addItemType(
      ItemTemplate("steelGreaves", "Steel Greaves", "-", icons(8)(3))
        .setWorth(150)
        .setEquipable(true)
        .setEquipableType("boots")
        .setArmor(13)
    )

    addItemType(
      ItemTemplate("steelGloves", "Steel Gloves", "-", icons(8)(1))
        .setWorth(130)
        .setEquipable(true)
        .setEquipableType("gloves")
        .setArmor(10)
    )

    addItemType(
      ItemTemplate("steelHelmet", "Steel Helmet", "-", icons(7)(1))
        .setWorth(170)
        .setEquipable(true)
        .setEquipableType("helmet")
        .setArmor(15)
    )

    addItemType(
      ItemTemplate("trident", "Trident", "-", icons(5)(8))
        .setWorth(900)
        .setEquipable(true)
        .setEquipableType("weapon")
        .setDamage(85)
        .setAttackType("trident")
    )

    addItemType(
      ItemTemplate("lifeRing", "Life Ring", "Increases life when worn", icons(8)(5))
        .setWorth(1400)
        .setEquipable(true)
        .setEquipableType("ring")
    )
  }

  def getItemTemplate(itemTemplateId: String): ItemTemplate = {
    if (itemTemplates.contains(itemTemplateId)) {
      itemTemplates(itemTemplateId)
    } else {
      throw new RuntimeException("item template doesn't exist: " + itemTemplateId)
    }
  }
}
