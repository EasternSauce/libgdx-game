package com.easternsauce.libgdxgame.creature.traits

import com.badlogic.gdx.audio.Music
import com.badlogic.gdx.physics.box2d.Body
import com.easternsauce.libgdxgame.ability.misc.Ability
import com.easternsauce.libgdxgame.area.Area
import com.easternsauce.libgdxgame.creature.{Creature, Enemy}
import com.easternsauce.libgdxgame.system.GameSystem

abstract class Boss(
  override val id: String,
  override val area: Option[Area] = None,
  override val b2Body: Option[Body] = None,
  override val standardAbilities: Map[String, Ability] = Map(),
  override val additionalAbilities: Map[String, Ability] = Map()
) extends Enemy(id = id, area = area, b2Body = b2Body, standardAbilities = standardAbilities, additionalAbilities = additionalAbilities) {

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
