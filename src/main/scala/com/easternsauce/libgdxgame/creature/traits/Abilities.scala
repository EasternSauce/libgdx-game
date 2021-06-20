package com.easternsauce.libgdxgame.creature.traits

import com.easternsauce.libgdxgame.ability._
import com.easternsauce.libgdxgame.ability.traits.{Ability, Attack}
import com.easternsauce.libgdxgame.creature.Creature
import com.easternsauce.libgdxgame.util.EsBatch

import scala.collection.mutable

trait Abilities {
  this: Creature =>

  var abilityMap: mutable.Map[String, Ability] = mutable.Map()
  var attackMap: mutable.Map[String, Attack] = mutable.Map()

  var slashAttack: SlashAttack = _
  var shootArrowAttack: ShootArrowAttack = _
  var thrustAttack: ThrustAttack = _

  var isAttacking = false

  var unarmedDamage = 30f

  def weaponDamage: Float = if (equipmentItems.contains(0)) equipmentItems(0).damage.get.toFloat else unarmedDamage

  def renderAbilities(batch: EsBatch): Unit = {
    for (ability <- abilityMap.values) {
      ability.render(batch)
    }
    currentAttack.render(batch)
  }

  def abilityActive: Boolean = {
    var abilityActive = false

    for (ability <- abilityMap.values) {
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
        case Some("slash")      => slashAttack
        case Some("shootArrow") => shootArrowAttack
        case Some("thrust")     => thrustAttack
        case _                  => throw new RuntimeException("Unrecognized attack type")
      }
    } else {
      slashAttack
    }
  }

  def defineStandardAbilities(): Unit = {
    slashAttack = new SlashAttack(this)
    shootArrowAttack = new ShootArrowAttack(this)
    thrustAttack = new ThrustAttack(this)

    attackMap += (slashAttack.id -> slashAttack)
    attackMap += (shootArrowAttack.id -> shootArrowAttack)
    attackMap += (thrustAttack.id -> thrustAttack)

    // TODO ?

  }

}
