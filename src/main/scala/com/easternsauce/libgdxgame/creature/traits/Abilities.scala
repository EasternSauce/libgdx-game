package com.easternsauce.libgdxgame.creature.traits

import com.easternsauce.libgdxgame.ability.traits.{Ability, Attack}
import com.easternsauce.libgdxgame.ability.{BowAttack, SwordAttack, TridentAttack, UnarmedAttack}
import com.easternsauce.libgdxgame.creature.Creature
import com.easternsauce.libgdxgame.util.EsBatch

import scala.collection.mutable
import scala.collection.mutable.ListBuffer

trait Abilities {
  this: Creature =>

  var abilityList: mutable.ListBuffer[Ability] = ListBuffer()
  var attackList: mutable.ListBuffer[Attack] = ListBuffer()

  var swordAttack: SwordAttack = _
  var unarmedAttack: UnarmedAttack = _
  var bowAttack: BowAttack = _
  var tridentAttack: TridentAttack = _

  var isAttacking = false

  var unarmedDamage = 15f

  def weaponDamage: Float = if (equipmentItems.contains(0)) equipmentItems(0).damage.get else unarmedDamage

  def renderAbilities(batch: EsBatch): Unit = {
    for (ability <- abilityList) {
      ability.render(batch)
    }
    currentAttack.render(batch)
  }

  def abilityActive: Boolean = {
    var abilityActive = false

    for (ability <- abilityList) {
      if (!abilityActive && ability.active) {
        abilityActive = true

      }
    }

    if (currentAttack.active) return true

    abilityActive
  }

  def currentAttack: Ability = {
    if (isWeaponEquipped) {
      currentWeapon.template.attackType match {
        case Some("sword")   => swordAttack
        case Some("bow")     => bowAttack
        case Some("trident") => tridentAttack
        case _               => throw new RuntimeException("Unrecognized attack type")
      }
    } else {
      unarmedAttack
    }
  }

  def defineStandardAbilities(): Unit = {
    swordAttack = new SwordAttack(this)
    unarmedAttack = new UnarmedAttack(this)
    bowAttack = new BowAttack(this)
    tridentAttack = new TridentAttack(this)

    attackList += swordAttack
  }

}
