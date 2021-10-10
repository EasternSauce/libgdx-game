package com.easternsauce.libgdxgame.creature

import com.badlogic.gdx.audio.Sound
import com.badlogic.gdx.physics.box2d.Body
import com.easternsauce.libgdxgame.ability.misc.Ability
import com.easternsauce.libgdxgame.area.Area
import com.easternsauce.libgdxgame.creature.traits.AnimationParams
import com.easternsauce.libgdxgame.system.Assets
import com.easternsauce.libgdxgame.util.EsDirection
import com.softwaremill.quicklens.ModifyPimp

case class Skeleton(
  override val id: String,
  override val area: Option[Area] = None,
  override val b2Body: Option[Body] = None,
  override val standardAbilities: Map[String, Ability] = Map(),
  override val additionalAbilities: Map[String, Ability] = Map()
) extends Enemy(
      id = id,
      area = area,
      b2Body = b2Body,
      standardAbilities = standardAbilities,
      additionalAbilities = additionalAbilities
    ) {
  override type Self = Skeleton

  override val creatureWidth = 2.85f
  override val creatureHeight = 2.85f

  override val maxLife = 160f

  override val onGettingHitSound: Option[Sound] = Some(Assets.sound(Assets.boneClickSound))

  override val activeSound: Option[Sound] = Some(Assets.sound(Assets.boneRattleSound))

  override val dropTable = Map(
    "ringmailGreaves" -> 0.1f,
    "leatherArmor" -> 0.05f,
    "hideGloves" -> 0.1f,
    "leatherHelmet" -> 0.1f,
    "woodenSword" -> 0.1f,
    "healingPowder" -> 0.5f
  )

  override val animationParams: AnimationParams = {
    AnimationParams(
      regionName = "skeleton",
      textureWidth = 64,
      textureHeight = 64,
      animationFrameCount = 9,
      frameDuration = 0.05f,
      neutralStanceFrame = 0,
      dirMap = Map(EsDirection.Up -> 0, EsDirection.Down -> 2, EsDirection.Left -> 1, EsDirection.Right -> 3)
    )
  }

  def copy(
    id: String = id,
    area: Option[Area] = area,
    b2Body: Option[Body] = b2Body,
    standardAbilities: Map[String, Ability] = standardAbilities,
    additionalAbilities: Map[String, Ability] = additionalAbilities
  ): Self =
    Skeleton(
      id = id,
      area = area,
      b2Body = b2Body,
      standardAbilities = standardAbilities,
      additionalAbilities = additionalAbilities
    )._temp_copyVars(this)

  init()
}


object Skeleton {
  def apply(
             id: String,
             area: Option[Area],
             b2Body: Option[Body],
             standardAbilities: Map[String, Ability],
             additionalAbilities: Map[String, Ability]
           ): Skeleton = {
    val creature0 = new Skeleton(id, area, b2Body, standardAbilities, additionalAbilities)
    val newStandardAbilities = if (standardAbilities.isEmpty) Skeleton.def
    val newAdditionalAbilities =

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