package com.easternsauce.libgdxgame.ability.attack

import com.easternsauce.libgdxgame.ability.misc.Ability

trait Attack extends Ability {

  override protected val isAttack = true

  override def onStop(): Unit = {
    super.onStop()

    creature.isAttacking = false
  }
}
