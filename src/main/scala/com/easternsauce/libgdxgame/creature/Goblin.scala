package com.easternsauce.libgdxgame.creature

import com.badlogic.gdx.audio.Sound
import com.badlogic.gdx.physics.box2d.Body
import com.easternsauce.libgdxgame.ability.misc.Ability
import com.easternsauce.libgdxgame.area.Area
import com.easternsauce.libgdxgame.creature.traits.AnimationParams
import com.easternsauce.libgdxgame.system.Assets
import com.easternsauce.libgdxgame.util.EsDirection
import com.softwaremill.quicklens.ModifyPimp

case class Goblin(
  override val id: String,
  override val area: Option[Area] = None,
  override val b2Body: Option[Body] = None,
  override val standardAbilities: Map[String, Ability] = Map(),
  override val additionalAbilities: Map[String, Ability] = Map()
) extends Enemy(id = id, area = area, b2Body = b2Body, standardAbilities = standardAbilities, additionalAbilities = additionalAbilities) {
  override type Self = Goblin

  override val creatureWidth = 2.85f
  override val creatureHeight = 2.85f

  override val maxLife = 190f

  override val onGettingHitSound: Option[Sound] = Some(Assets.sound(Assets.evilYellingSound))

  override val dropTable = Map(
    "ironSword" -> 0.03f,
    "poisonDagger" -> 0.07f,
    "healingPowder" -> 0.3f,
    "steelArmor" -> 0.03f,
    "steelGreaves" -> 0.05f,
    "steelGloves" -> 0.05f,
    "steelHelmet" -> 0.05f
  )

  override val animationParams: AnimationParams = AnimationParams(
    regionName = "goblin",
    textureWidth = 32,
    textureHeight = 32,
    animationFrameCount = 3,
    frameDuration = 0.25f,
    neutralStanceFrame = 1,
    dirMap = Map(EsDirection.Up -> 3, EsDirection.Down -> 0, EsDirection.Left -> 1, EsDirection.Right -> 2)
  )

  def copy(
    id: String = id,
    area: Option[Area] = area,
    b2Body: Option[Body] = b2Body,
    standardAbilities: Map[String, Ability] = standardAbilities,
    additionalAbilities: Map[String, Ability] = additionalAbilities
  ): Self =
    Goblin(id = id, area = area, b2Body = b2Body, standardAbilities = standardAbilities, additionalAbilities = additionalAbilities)._temp_copyVars(this)

  init()
}


object Goblin {
  def apply(
             id: String,
             area: Option[Area],
             b2Body: Option[Body],
             standardAbilities: Map[String, Ability],
             additionalAbilities: Map[String, Ability]
           ): Goblin = {
    val creature0 = new Goblin(id, area, b2Body, standardAbilities, additionalAbilities)
    val creature1 = if (standardAbilities.isEmpty) {
      creature0
        .modify(_.standardAbilities)
        .setTo(creature0.defaultStandardAbilities)
    } else creature0
    val creature2 = if (additionalAbilities.isEmpty) {
      creature1
        .modify(_.additionalAbilities)
        .setTo(creature1.defaultAdditionalAbilities)
    } else creature1

    creature2
  }
}
