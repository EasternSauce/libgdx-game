package com.easternsauce.libgdxgame.ability

import com.badlogic.gdx.audio.Sound
import com.easternsauce.libgdxgame.creature.Creature
import com.easternsauce.libgdxgame.system.Assets

class ThrustAttack(val creature: Creature) extends MeleeAttack {

  override protected val baseChannelTime = 0.6f
  override protected val baseActiveTime = 0.275f
  private val numOfChannelFrames = 7
  private val numOfFrames = 11

  override var attackRange: Float = 0.9375f
  override protected var aimed: Boolean = false
  override protected var spriteWidth: Int = 64
  override protected var spriteHeight: Int = 32
  override protected var knockbackPower: Float = 20f
  override protected val cooldownTime: Float = 0.7f

  override protected val abilitySound: Option[Sound] = Some(Assets.sound(Assets.attackSound))

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
