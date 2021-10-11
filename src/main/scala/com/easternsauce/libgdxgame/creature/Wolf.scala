package com.easternsauce.libgdxgame.creature

import com.badlogic.gdx.audio.Sound
import com.badlogic.gdx.physics.box2d.Body
import com.easternsauce.libgdxgame.ability.misc.Ability
import com.easternsauce.libgdxgame.ability.other.DashAbility
import com.easternsauce.libgdxgame.area.Area
import com.easternsauce.libgdxgame.creature.traits.{AbilityUsage, AnimationParams}
import com.easternsauce.libgdxgame.system.Assets
import com.easternsauce.libgdxgame.util.EsDirection

case class Wolf(override val id: String, override val area: Option[Area] = None, override val b2Body: Option[Body] = None)
    extends Enemy(id = id, area = area, b2Body = b2Body) {
  override type Self = Wolf

  override val creatureWidth = 2.85f
  override val creatureHeight = 2.85f

  override val maxLife = 110f

  override val onGettingHitSound: Option[Sound] = Some(Assets.sound(Assets.dogWhineSound))

  override val activeSound: Option[Sound] = Some(Assets.sound(Assets.dogBarkSound))

  override lazy val additionalAbilities: Map[String, Ability] =
    Map(DashAbility(id).asMapEntry)

  override val dropTable = Map(
    "ringmailGreaves" -> 0.1f,
    "leatherArmor" -> 0.05f,
    "hideGloves" -> 0.1f,
    "leatherHelmet" -> 0.1f,
    "healingPowder" -> 0.5f
  )

  override val abilityUsages: Map[String, AbilityUsage] =
    Map("dash" -> AbilityUsage(weight = 100f, minimumDistance = 8f))

  override val animationParams: AnimationParams = AnimationParams(
    regionName = "wolf2",
    textureWidth = 32,
    textureHeight = 34,
    animationFrameCount = 6,
    frameDuration = 0.1f,
    neutralStanceFrame = 1,
    dirMap = Map(EsDirection.Up -> 3, EsDirection.Down -> 0, EsDirection.Left -> 1, EsDirection.Right -> 2)
  )

  def copy(id: String = id, area: Option[Area] = area, b2Body: Option[Body] = b2Body): Self = Wolf(id = id, area = area, b2Body = b2Body)

  init()
}