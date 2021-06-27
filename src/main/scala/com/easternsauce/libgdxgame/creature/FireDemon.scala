package com.easternsauce.libgdxgame.creature

import com.badlogic.gdx.audio.Sound
import com.easternsauce.libgdxgame.ability.{DashAbility, FistSlamAbility, MeteorCrashAbility, MeteorRainAbility}
import com.easternsauce.libgdxgame.creature.traits.{AbilityUsage, Boss}
import com.easternsauce.libgdxgame.system.Assets
import com.easternsauce.libgdxgame.util.EsDirection

class FireDemon(val id: String) extends Boss {

  override val creatureWidth = 7.5f
  override val creatureHeight = 7.5f

  val spriteWidth = 80
  val spriteHeight = 80

  override val maxLife = 4000f

  override val onGettingHitSound: Option[Sound] = Some(Assets.sound(Assets.roarSound))

  //override val scale: Float = 3.0f
  //override val hitbox = new Rectangle(0, 0, 80 * scale, 80 * scale)
  //override val baseSpeed: Float = 16f
  protected var meteorRainAbility: MeteorRainAbility = _
  protected var fistSlamAbility: FistSlamAbility = _
  protected var meteorCrashAbility: MeteorCrashAbility = _
  protected var dashAbility: DashAbility = _

//
//  loadSprites(Assets.fireDemonSpriteSheet, Map(Left -> 3, Right -> 1, Up -> 0, Down -> 2), 0)

//  maxHealthPoints = 4000f
//  healthPoints = maxHealthPoints

//  name = "Magma Stalker"

//  aggroDistance = 800f
//  attackDistance = 200f
//  walkUpDistance = 800f

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

//  override def onAggroed(): Unit = {
//    if (!bossBattleStarted) {
//      bossBattleStarted = true
//
//      bossMusic.setVolume(0.1f)
//      bossMusic.setLooping(true)
//      bossMusic.play()
//
//      GameSystem.hud.bossHealthBar.onBossBattleStart(this)
//      mobSpawnPoint.blockade.active = true
//      Assets.monsterGrowlSound.play(0.1f)
//    }
//  }

  abilityUsages.addAll(List("dash" -> AbilityUsage(70f, 30f)))
  abilityUsages.addAll(List("meteorRain" -> AbilityUsage(30f, 20f, 0.6f)))
  abilityUsages.addAll(List("fistSlam" -> AbilityUsage(30f, 8f)))
  abilityUsages.addAll(List("meteorCrash" -> AbilityUsage(30f, 20f)))
}
