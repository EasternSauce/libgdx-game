package com.easternsauce.libgdxgame.creature

import com.badlogic.gdx.audio.{Music, Sound}
import com.easternsauce.libgdxgame.ability.{DashAbility, FistSlamAbility, MeteorCrashAbility, MeteorRainAbility}
import com.easternsauce.libgdxgame.creature.traits.{AbilityUsage, Boss}
import com.easternsauce.libgdxgame.system.Assets
import com.easternsauce.libgdxgame.util.EsDirection

class FireDemon(val id: String) extends Boss {

  override val creatureWidth = 7.5f
  override val creatureHeight = 7.5f

  val spriteWidth = 80
  val spriteHeight = 80

  override val maxLife = 5500f

  override val onGettingHitSound: Option[Sound] = Some(Assets.sound(Assets.roarSound))

  override val aggroDropDistance = 999f

  override val directionalSpeed: Float = 25f

  protected var meteorRainAbility: MeteorRainAbility = _
  protected var fistSlamAbility: FistSlamAbility = _
  protected var meteorCrashAbility: MeteorCrashAbility = _
  protected var dashAbility: DashAbility = _

  override val bossMusic: Option[Music] = Some(Assets.music(Assets.fireDemonMusic))

  override val name = "Magma Stalker"

  setupAnimation(
    regionName = "taurus",
    textureWidth = spriteWidth,
    textureHeight = spriteHeight,
    animationFrameCount = 4,
    frameDuration = 0.15f,
    neutralStanceFrame = 0,
    dirMap = Map(EsDirection.Left -> 1, EsDirection.Right -> 2, EsDirection.Up -> 3, EsDirection.Down -> 0)
  )

  initCreature()

  mass = 10000f

  meteorRainAbility = new MeteorRainAbility(this)
  fistSlamAbility = new FistSlamAbility(this)
  meteorCrashAbility = new MeteorCrashAbility(this)
  dashAbility = new DashAbility(this)
  abilityMap += (meteorRainAbility.id -> meteorRainAbility)
  abilityMap += (fistSlamAbility.id -> fistSlamAbility)
  abilityMap += (meteorCrashAbility.id -> meteorCrashAbility)
  abilityMap += (dashAbility.id -> dashAbility)

  thrustAttack.attackRange = 1.5f

  dropTable.addAll(
    List("ironSword" -> 0.3f, "poisonDagger" -> 0.3f, "steelArmor" -> 0.8f, "steelHelmet" -> 0.5f, "thiefRing" -> 1.0f)
  )

  abilityUsages.addAll(List("dash" -> AbilityUsage(weight = 70f, minimumDistance = 15f)))
  abilityUsages.addAll(List("meteorRain" -> AbilityUsage(weight = 30f, minimumDistance = 4f, lifeThreshold = 0.6f)))
  abilityUsages.addAll(List("fistSlam" -> AbilityUsage(weight = 30f, minimumDistance = 4f, maximumDistance = 10f)))
  abilityUsages.addAll(List("meteorCrash" -> AbilityUsage(weight = 30f, minimumDistance = 6f)))
}
