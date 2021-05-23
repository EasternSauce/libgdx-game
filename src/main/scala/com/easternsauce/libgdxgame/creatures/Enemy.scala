package com.easternsauce.libgdxgame.creatures

trait Enemy extends Creature with AggressiveAI {

  override val isEnemy: Boolean = true

  override def update(): Unit = {
    super.update()

    lookForTarget()

    if (targetFound) {
      walkTo(aggroedOn.get.pos)
    }

  }
}
