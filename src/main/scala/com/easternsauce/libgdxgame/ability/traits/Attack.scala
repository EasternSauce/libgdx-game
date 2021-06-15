package com.easternsauce.libgdxgame.ability.traits

trait Attack extends Ability {

  override protected val isAttack = true

  override def onStop(): Unit = {
    super.onStop()

    creature.isAttacking = false
  }
}
