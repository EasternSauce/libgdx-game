package com.easternsauce.libgdxgame.creature

import com.badlogic.gdx.audio.Sound
import com.badlogic.gdx.physics.box2d.Body
import com.easternsauce.libgdxgame.ability.misc.Ability
import com.easternsauce.libgdxgame.ability.other.ExplodeAbility
import com.easternsauce.libgdxgame.ability.parameters.SoundParameters
import com.easternsauce.libgdxgame.area.Area
import com.easternsauce.libgdxgame.creature.traits.{AbilityUsage, AnimationParams}
import com.easternsauce.libgdxgame.system.Assets
import com.easternsauce.libgdxgame.util.EsDirection
import com.softwaremill.quicklens.ModifyPimp

case class Ghost(
  override val id: String,
  override val area: Option[Area] = None,
  override val b2Body: Option[Body] = None,
  override val standardAbilities: Map[String, Ability] = Map(),
  override val additionalAbilities: Map[String, Ability] = Map()
) extends Enemy(id = id, area = area, b2Body = b2Body, standardAbilities = standardAbilities, additionalAbilities = additionalAbilities) {
  override type Self = Ghost

  override val creatureWidth = 2.85f
  override val creatureHeight = 2.85f

  override val maxLife = 300f

  override val onGettingHitSound: Option[Sound] = Some(Assets.sound(Assets.evilYellingSound))

  override val dropTable = Map(
    "ironSword" -> 0.03f,
    "poisonDagger" -> 0.005f,
    "healingPowder" -> 0.3f,
    "steelArmor" -> 0.03f,
    "steelGreaves" -> 0.05f,
    "steelGloves" -> 0.05f,
    "steelHelmet" -> 0.05f
  )

  override val defaultAdditionalAbilities: Map[String, Ability] =
    Map({
      val explodeAbility = ExplodeAbility(
        creature = this,
        soundParameters = SoundParameters(
          channelSound = Some(Assets.sound(Assets.darkLaughSound)),
          channelSoundVolume = Some(0.2f),
          activeSound = Some(Assets.sound(Assets.explosionSound)),
          activeSoundVolume = Some(0.5f)
        )
      )

      explodeAbility
    }.asMapEntry)

  override val abilityUsages: Map[String, AbilityUsage] =
    Map("explode" -> AbilityUsage(weight = 100f, minimumDistance = 6f, lifeThreshold = 0.5f))

  override val animationParams: AnimationParams = AnimationParams(
    regionName = "ghost",
    textureWidth = 32,
    textureHeight = 32,
    animationFrameCount = 3,
    frameDuration = 0.25f,
    neutralStanceFrame = 1,
    dirMap = Map(EsDirection.Up -> 3, EsDirection.Down -> 0, EsDirection.Left -> 1, EsDirection.Right -> 2)
  )

  def copy(id: String = id, area: Option[Area] = area, b2Body: Option[Body] = b2Body,         standardAbilities: Map[String, Ability] = standardAbilities, additionalAbilities: Map[String, Ability] = additionalAbilities): Self =
    Ghost(id = id, area = area, b2Body = b2Body, standardAbilities = standardAbilities, additionalAbilities = additionalAbilities)._temp_copyVars(this)

  init()
}


object Ghost {
  def apply(
             id: String,
             area: Option[Area],
             b2Body: Option[Body],
             standardAbilities: Map[String, Ability],
             additionalAbilities: Map[String, Ability]
           ): Ghost = {
    val creature0 = new Ghost(id, area, b2Body, standardAbilities, additionalAbilities)
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
