package com.easternsauce.libgdxgame.creature.traits

import com.badlogic.gdx.audio.Music
import com.badlogic.gdx.physics.box2d.Body
import com.easternsauce.libgdxgame.creature.{Creature, CreatureParameters, Enemy}
import com.easternsauce.libgdxgame.system.GameSystem

abstract class Boss(override val id: String, override val params: CreatureParameters = CreatureParameters())
    extends Enemy(id = id, params = params) {

  override val isKnockbackable = false
  override val isBoss = true

  val encounterMusic: Option[Music] = None

  val name: String

  val bossMusic: Option[Music]

  override def aggroOnCreature(otherCreature: Creature): Unit = {
    super.aggroOnCreature(otherCreature)

    if (otherCreature.isPlayer) GameSystem.bossfightManager.startBossfight(this)
  }

  override def onDeath(): Unit = {
    super.onDeath()

    GameSystem.bossfightManager.stopBossfight()

  }
}
