package com.easternsauce.libgdxgame.ability

trait Attack extends Ability with AbilityActiveAnimation with AbilityWindupAnimation {

  override protected val isAttack = true

  override def onStop() {
    super.onStop()

    creature.isAttacking = false
  }
}
