package com.easternsauce.libgdxgame.creature.traits

import com.easternsauce.libgdxgame.ability.misc.templates.Ability
import com.easternsauce.libgdxgame.creature.Creature
import com.easternsauce.libgdxgame.util.EsBatch

trait Abilities {
  this: Creature =>

  var isAttacking = false

  var unarmedDamage = 30f

  def weaponDamage: Float = if (equipmentItems.contains(0)) equipmentItems(0).damage.get.toFloat else unarmedDamage

  def renderAbilities(batch: EsBatch): Unit = {
    for (ability <- abilities.values) {
      ability.render(batch)
    }
    currentAttack.render(batch)
  }

  def abilityActive: Boolean = {
    var abilityActive = false

    for (ability <- abilities.values) {
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
        case Some(id) => abilities(id)
        case None     => throw new RuntimeException("Unrecognized attack type")
      }
    } else {
      abilities("slash")
    }
  }

}
