package com.easternsauce.libgdxgame.creatures

trait Enemy extends Creature with AggressiveAI {

  override val isEnemy: Boolean = true

}
