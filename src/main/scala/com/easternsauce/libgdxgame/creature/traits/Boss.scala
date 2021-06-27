package com.easternsauce.libgdxgame.creature.traits

import com.easternsauce.libgdxgame.creature.Enemy

trait Boss extends Enemy {

  override val isKnockbackable = false
  override val isBoss = true
}
