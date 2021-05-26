package com.easternsauce.libgdxgame.creature.traits

trait Enemy extends Creature with AggressiveAI {

  override val isEnemy: Boolean = true

}
