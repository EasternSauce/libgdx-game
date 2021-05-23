package com.easternsauce.libgdxgame.creature

trait Enemy extends Creature with AggressiveAI {

  override val isEnemy: Boolean = true

}
