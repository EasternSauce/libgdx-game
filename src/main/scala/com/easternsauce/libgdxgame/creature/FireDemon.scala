package com.easternsauce.libgdxgame.creature

import com.badlogic.gdx.audio.{Music, Sound}
import com.badlogic.gdx.physics.box2d.Body
import com.easternsauce.libgdxgame.ability.composed.{FistSlamAbility, MeteorCrashAbility, MeteorRainAbility}
import com.easternsauce.libgdxgame.ability.misc.Ability
import com.easternsauce.libgdxgame.ability.other.DashAbility
import com.easternsauce.libgdxgame.area.Area
import com.easternsauce.libgdxgame.creature.traits.{AbilityUsage, AnimationParams, Boss}
import com.easternsauce.libgdxgame.system.Assets
import com.easternsauce.libgdxgame.util.EsDirection

case class FireDemon(override val id: String, override val area: Option[Area] = None, override val b2Body: Option[Body] = None)
    extends Boss(id = id, area = area, b2Body = b2Body) {
  override type Self = FireDemon

  override val creatureWidth = 7.5f
  override val creatureHeight = 7.5f

  val spriteWidth = 80
  val spriteHeight = 80

  override val maxLife = 5500f

  override val onGettingHitSound: Option[Sound] = Some(Assets.sound(Assets.roarSound))

  override val aggroDropDistance = 999f

  override val directionalSpeed: Float = 25f

  override val bossMusic: Option[Music] = Some(Assets.music(Assets.fireDemonMusic))

  override val name = "Magma Stalker"

  override val dropTable =
    Map("ironSword" -> 0.3f, "poisonDagger" -> 0.3f, "steelArmor" -> 0.8f, "steelHelmet" -> 0.5f, "thiefRing" -> 1.0f)

  override lazy val additionalAbilities: Map[String, Ability] =
    Map(
      MeteorRainAbility(id).asMapEntry,
      FistSlamAbility(id).asMapEntry,
      MeteorCrashAbility(id).asMapEntry,
      DashAbility(id).asMapEntry
    )

  override val abilityUsages: Map[String, AbilityUsage] =
    Map(
      "dash" -> AbilityUsage(weight = 70f, minimumDistance = 15f),
      "meteorRain" -> AbilityUsage(weight = 30f, minimumDistance = 4f, lifeThreshold = 0.6f),
      "fistSlam" -> AbilityUsage(weight = 30f, minimumDistance = 4f, maximumDistance = 10f),
      "meteorCrash" -> AbilityUsage(weight = 30f, minimumDistance = 6f)
    )

  override val animationParams: AnimationParams = AnimationParams(
    regionName = "taurus",
    textureWidth = spriteWidth,
    textureHeight = spriteHeight,
    animationFrameCount = 4,
    frameDuration = 0.15f,
    neutralStanceFrame = 0,
    dirMap = Map(EsDirection.Left -> 1, EsDirection.Right -> 2, EsDirection.Up -> 3, EsDirection.Down -> 0)
  )

  mass = 10000f

  // TODO: how to get rid of casting?
  // TODO: refactor this before uncommenting!
  // abilityMap("thrust").asInstanceOf[ThrustAttack].attackRange = 1.5f

  override def copy(id: String = id, area: Option[Area] = area, b2Body: Option[Body] = b2Body): Self = FireDemon(id = id, area = area, b2Body = b2Body)

  init()
}
