package com.easternsauce.libgdxgame.ability.attack

import com.easternsauce.libgdxgame.creature.Creature
import com.easternsauce.libgdxgame.system.Assets

class ThrustAttack private (val creature: Creature) extends MeleeAttack {

  val id: String = "thrust"

  protected val baseChannelTime = 0.6f
  protected val baseActiveTime = 0.275f
  private val numOfChannelFrames = 7
  private val numOfFrames = 11

  var attackRange: Float = 0.9375f
  protected var aimed: Boolean = false
  protected var spriteWidth: Int = 64
  protected var spriteHeight: Int = 32
  protected var knockbackVelocity: Float = 20f
  protected val cooldownTime: Float = 0.7f

  activeSound = Some(Assets.sound(Assets.attackSound))
  activeSoundVolume = Some(0.1f)

  setupActiveAnimation(
    regionName = "trident_thrust",
    textureWidth = spriteWidth,
    textureHeight = spriteHeight,
    animationFrameCount = numOfFrames,
    frameDuration = baseActiveTime / numOfFrames
  )

  setupWindupAnimation(
    regionName = "trident_thrust_windup",
    textureWidth = spriteWidth,
    textureHeight = spriteHeight,
    animationFrameCount = numOfChannelFrames,
    frameDuration = baseChannelTime / numOfChannelFrames
  )
}

object ThrustAttack {
  def apply(abilityCreature: Creature): ThrustAttack = {
    new ThrustAttack(abilityCreature)
  }
}
